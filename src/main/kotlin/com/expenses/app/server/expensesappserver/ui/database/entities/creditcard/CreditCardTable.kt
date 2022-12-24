package com.expenses.app.server.expensesappserver.ui.database.entities.creditcard

import com.fasterxml.jackson.annotation.JsonIgnore
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

data class CreditCard(
    var id: Int? = null,
    @JsonIgnore
    val userId: String,
    val cutDate: LocalDateTime,
    val paymentDeadLine: LocalDateTime,
    val credit: Double,
    val remainingCredit: Double,
    val totalToPay: Double,
    val lastFourDigits: String
)

object CreditCardTable : IntIdTable("credit_card") {
    val userId: Column<String> = varchar("user_id", 50)
    val cutDate: Column<LocalDateTime> = datetime("cut_date")
    val paymentDeadLine: Column<LocalDateTime> = datetime("payment_deadline")
    val credit: Column<Double> = double("credit")
    val remainingCredit: Column<Double> = double("remaining_credit")
    val totalToPlay: Column<Double> = double("total_to_pay")
    val lastFourdigits: Column<String> = varchar("last_four_digits", 4)
}

class CreditCardEntity(
    id: EntityID<Int>
) : IntEntity(id) {
    companion object : IntEntityClass<CreditCardEntity>(CreditCardTable)

    var userId by CreditCardTable.userId
    var cutDate by CreditCardTable.cutDate
    var paymentDeadLine by CreditCardTable.paymentDeadLine
    var credit by CreditCardTable.credit
    var remainingCredit by CreditCardTable.remainingCredit
    var totalToPay by CreditCardTable.totalToPlay
    var lastFourDigits by CreditCardTable.lastFourdigits

    fun toCreditCard() = CreditCard(
        id.value,
        userId,
        cutDate,
        paymentDeadLine,
        credit,
        remainingCredit,
        totalToPay,
        lastFourDigits
    )
}