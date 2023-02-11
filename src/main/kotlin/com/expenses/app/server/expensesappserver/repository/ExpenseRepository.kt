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
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
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
    val expensesTagsTable = ExpensesTags

    fun getAllExpenses(size: Int, offset: Long): List<Expenses> {
        val userIdName = authenticationFacade.userId()

        val expense = loggedTransaction {
            expenseCrudTable.find { ExpenseTable.userId eq userIdName }
                .orderBy(ExpenseTable.dateAdded to SortOrder.DESC)
                .limit(size, offset = offset)
                .map { it.toExpense() }
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
            tagsPost.forEach { tag ->
                tagsCrudTable.table.insertIgnore {
                    it[TagsTable.dateAdded] = tag.dateAdded
                    it[TagsTable.tagName] = tag.tagName
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
                tags = SizedCollection(tagsArr)
            }

            insertedExpense = expense.toExpense()
        }

        return insertedExpense ?: throw EntityNotFoundException(
            status = Status.NO_DATA,
            customMessage = "Something went wrong",
            id = authenticationFacade.userId()
        )
    }

    fun deleteExpense(id: Int): Expenses {
        val expenseToDelete = getExpenseById(id)
        loggedTransaction {
            expensesTagsTable.deleteWhere { ExpensesTags.expense eq id }
            expenseCrudTable.table.deleteWhere { ExpenseTable.id eq id }
        }
        logger.info("Deleted expense with Id $id")
        return expenseToDelete
    }

    fun getAllTags(): List<Tags> = loggedTransaction {
        tagsCrudTable.all().toList().toTags()
    }

    fun getExpenseById(id: Int) = loggedTransaction {
        expenseCrudTable.find { ExpenseTable.id eq id and (ExpenseTable.userId eq authenticationFacade.userId()) }
            .limit(1).firstOrNull()?.toExpense()
    } ?: throw EntityNotFoundException(
        status = Status.NO_DATA,
        customMessage = "This expense id doesn't exists",
        id = authenticationFacade.userId()
    )

}
