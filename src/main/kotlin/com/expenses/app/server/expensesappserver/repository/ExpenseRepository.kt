package com.expenses.app.server.expensesappserver.repository

import com.expenses.app.server.expensesappserver.security.AuthenticationFacade
import com.expenses.app.server.expensesappserver.ui.database.entities.expenses.*
import com.expenses.app.server.expensesappserver.ui.database.entities.tags.TagEntity
import com.expenses.app.server.expensesappserver.ui.database.entities.tags.Tags
import com.expenses.app.server.expensesappserver.ui.database.entities.tags.TagsTable
import com.expenses.app.server.expensesappserver.utils.loggedTransaction
import org.jetbrains.exposed.sql.SizedCollection
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class ExpenseRepository(
    private val authenticationFacade: AuthenticationFacade
) {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(ExpenseRepository::class.java)
    }

    val expenseCrudTable = ExpenseEntity
    val tagsCrudTable = TagEntity

    fun getAllExpenses(): List<Expenses> {
        val userIdName = authenticationFacade.userId()

        val expense = loggedTransaction {
            expenseCrudTable.find { ExpenseTable.userId eq userIdName }
                .toList().toExpense()
        }

        return expense
    }

    fun insertExpense(expenses: ExpensesPost) {
        val userIdName = authenticationFacade.userId()
        val tagsPost = expenses.tag

        val expense = loggedTransaction {
            expenseCrudTable.new {
                userId = userIdName
                concept = expenses.concept
                total = expenses.total
                dateAdded = expenses.dateAdded
                comments = expenses.comments
            }
        }

        // TODO check how to prevent adding the same tag twice
        val nonExistingTags = mutableListOf<Tags>()
            loggedTransaction {
            tagsPost.forEach { tag ->
                tagsCrudTable.find { TagsTable.tagName neq  tag.tagName }
                    .map { nonExistingTags.add(it.toTags() )}
            }
        }

        val tags = loggedTransaction {
            nonExistingTags.map { tag ->
                tagsCrudTable.new {
                    tagName = tag.tagName
                    dateAdded = tag.dateAdded
                }
            }
        }

        loggedTransaction {
            expense.tags = SizedCollection(tags)
        }
    }

    fun getAllTags(): List<Tags> = loggedTransaction {
        tagsCrudTable.all().toList().toTags()
    }

}