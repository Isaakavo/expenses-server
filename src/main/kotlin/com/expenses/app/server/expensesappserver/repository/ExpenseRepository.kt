package com.expenses.app.server.expensesappserver.repository

import com.expenses.app.server.expensesappserver.common.exceptions.BadRequestException
import com.expenses.app.server.expensesappserver.common.exceptions.EntityNotFoundException
import com.expenses.app.server.expensesappserver.common.responses.Status
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
        const val MAX_TAG_REQUEST = 10
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

    fun insertExpense(expenses: ExpensesPost): Expenses {
        val userIdName = authenticationFacade.userId()
        val tagsPost = expenses.tag

        // We only accept 10 tags max per request
        if (tagsPost.size > MAX_TAG_REQUEST) throw BadRequestException(
            Status.BAD_REQUEST,
            "Only $MAX_TAG_REQUEST tags are allowed"
        )

        var insertedExpense: Expenses? = null
        loggedTransaction {
            // Check if some tags already exists
            tagsPost.forEach { tag ->
                val internTag = tagsCrudTable.find { TagsTable.tagName eq tag.tagName }.firstOrNull()
                // Only insert into the table tags that doesn't exist
                if (internTag == null) {
                    tagsCrudTable.new {
                        dateAdded = tag.dateAdded
                        tagName = tag.tagName
                    }
                }
            }
            // Get all the tags that come from the request
            val tagsArr = mutableListOf<TagEntity>()
            tagsPost.map {
                val internTag = tagsCrudTable.find {
                    TagsTable.tagName eq it.tagName
                }.first()
                tagsArr.add(internTag)
            }
            val expense = expenseCrudTable.new {
                userId = userIdName
                concept = expenses.concept
                total = expenses.total
                dateAdded = expenses.dateAdded
                comments = expenses.comments
            }

            expense.tags = SizedCollection(tagsArr)
            insertedExpense = expense.toExpense()
        }

        return insertedExpense ?: throw EntityNotFoundException(
            status = Status.NO_DATA,
            customMessage = "Something went wrong",
            id = authenticationFacade.userId()
        )
    }

    fun getAllTags(): List<Tags> = loggedTransaction {
        tagsCrudTable.all().toList().toTags()
    }

}