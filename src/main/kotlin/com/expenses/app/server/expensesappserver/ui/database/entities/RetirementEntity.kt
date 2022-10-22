package com.expenses.app.server.expensesappserver.ui.database.entities

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime


object RetirementEntity : LongIdTable("udis_table") {
    val userId: Column<Long> = long("user_id")
    val purchaseTotal: Column<Double> = double("purchase_total")
    val dateOfPurchase: Column<LocalDateTime> = datetime("date_purchase")
    val udiValue: Column<Double> = double("udi_value")
}

data class RetirementRecord(
    val id: Long,
    val userId: Long,
    val purchaseTotal: Double,
    val dateOfPurchase: LocalDateTime,
    val udiValue: Double
)

fun RetirementEntity.rowToRetirementRecord(row: ResultRow): RetirementRecord = RetirementRecord(
    id = row[RetirementEntity.id].value,
    userId = row[userId],
    purchaseTotal = row[purchaseTotal],
    dateOfPurchase = row[dateOfPurchase],
    udiValue = row[udiValue]
    )