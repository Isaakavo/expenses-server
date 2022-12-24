package com.expenses.app.server.expensesappserver.ui.database.entities.expenses

import com.fasterxml.jackson.annotation.JsonIgnore
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime


data class Income(
    var id: Int? = null,
    @JsonIgnore
    val userId: String,
    val income: Double,
    val dateAdded: LocalDateTime,
    val month: Int,
    val remaining: Double
)

object IncomeTable: IntIdTable("income" ) {
    val userId: Column<String> = varchar("user_id", 50)
    val income: Column<Double> = double("income")
    val dateAdded: Column<LocalDateTime> = datetime("date_added")
    // This variable will be used with LocalDateTime.month, which returns a int indicating the month
    val month: Column<Int> = integer("month")
    val remaining: Column<Double> = double("remaining")
}

class IncomeEntity(
    id: EntityID<Int>
): IntEntity(id) {
    companion object: IntEntityClass<IncomeEntity>(IncomeTable)

    var userId by IncomeTable.userId
    var income by IncomeTable.income
    var dateAdded by IncomeTable.dateAdded
    var month by IncomeTable.month
    var remaining by IncomeTable.remaining

    fun toIncome() =  Income(
        id.value,
        userId,
        income,
        dateAdded,
        month,
        remaining
    )
}