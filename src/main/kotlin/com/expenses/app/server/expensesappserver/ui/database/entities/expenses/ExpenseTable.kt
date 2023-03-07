package com.expenses.app.server.expensesappserver.ui.database.entities.expenses

import com.expenses.app.server.expensesappserver.ui.database.entities.tags.TagEntity
import com.expenses.app.server.expensesappserver.ui.database.entities.tags.Tags
import com.expenses.app.server.expensesappserver.ui.database.entities.tags.TagsResponse
import com.expenses.app.server.expensesappserver.ui.database.entities.tags.TagsTable
import com.expenses.app.server.expensesappserver.utils.formatDate
import com.fasterxml.jackson.annotation.JsonIgnore
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

data class Expenses(
    val id: Int? = null,
    @JsonIgnore
    val userId: String,
    val concept: String,
    val total: Double,
    val dateAdded: String,
    val tag: List<TagsResponse>,
    val comments: String? = null
)

data class ExpensesPost(
    val concept: String,
    val total: Double,
    val dateAdded: LocalDateTime,
    val tag: List<Tags>,
    val comments: String? = null
)

object ExpensesTags : Table() {
    val expense = reference("expense", ExpenseTable)
    val tag = reference("tag", TagsTable, onDelete = ReferenceOption.NO_ACTION)
    override val primaryKey = PrimaryKey(expense, tag, name = "PK_ExpensesTags")
}

object ExpenseTable : IntIdTable("expense") {
    val userId: Column<String> = varchar("user_id", 50)
    val concept: Column<String> = varchar("concept", 50)
    val total: Column<Double> = double("total")
    val dateAdded: Column<LocalDateTime> = datetime("date_added")
    val comments: Column<String?> = varchar("comments", 200).nullable()
}

class ExpenseEntity(
    id: EntityID<Int>
) : IntEntity(id) {
    companion object : IntEntityClass<ExpenseEntity>(ExpenseTable)

    var userId by ExpenseTable.userId
    var concept by ExpenseTable.concept
    var total by ExpenseTable.total
    var dateAdded by ExpenseTable.dateAdded
    var tags by TagEntity via ExpensesTags
    var comments by ExpenseTable.comments

    fun toExpense() = Expenses(
        id.value,
        userId,
        concept,
        total,
        formatDate(dateAdded),
        tags.toList().toTags(),
        comments
    )
}

fun List<TagEntity>.toTags(): List<TagsResponse> = this.map {
    it.toTags()
}
