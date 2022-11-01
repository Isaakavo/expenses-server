package com.expenses.app.server.expensesappserver.ui.database.entities

import com.fasterxml.jackson.annotation.JsonIgnore
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime


object RetirementTable : LongIdTable("udis_table") {
    val userId: Column<String> = varchar("user_id", 50)
    val purchaseTotal: Column<Double> = double("purchase_total")
    val dateOfPurchase: Column<LocalDateTime> = datetime("date_purchase")
    val udiValue: Column<Double> = double("udi_value")
}

data class RetirementRecord(
    @JsonIgnore
    var id: EntityID<Long>?,
    val userId: String,
    val purchaseTotal: Double?,
    val dateOfPurchase: LocalDateTime?,
    val udiValue: Double?,
)

data class ResponseRetirementRecord(
    val retirementRecord: RetirementRecord,
    val udiCommission: UdiCommission,
    val udiConversions: UdiConversions
)

data class UdiConversions(
    val udiConversion: Double,
    val udiCommissionConversion: Double
)

 class RetirementRecordEntity(
    id: EntityID<Long>
): LongEntity(id) {
     companion object: LongEntityClass<RetirementRecordEntity>(RetirementTable)

     //var id by RetirementEntity.id
     var userId by RetirementTable.userId
     var purchaseTotal by RetirementTable.purchaseTotal
     var dateOfPurchase by RetirementTable.dateOfPurchase
     var udiValue by RetirementTable.udiValue

     fun toRetirementRecord() = RetirementRecord(
         id,
        userId,
         purchaseTotal,
         dateOfPurchase,
         udiValue
     )
 }

//fun RetirementEntity.rowToRetirementRecord(row: ResultRow): RetirementRecord = RetirementRecord(
//    //id = row[RetirementEntity.id].value,
//    userId = row[userId],
//    purchaseTotal = row[purchaseTotal],
//    dateOfPurchase = row[dateOfPurchase],
//    udiValue = row[udiValue]
//    )