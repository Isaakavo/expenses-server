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
    var id: Long? = null,
    @JsonIgnore
    val userId: String,
    val purchaseTotal: Double,
    val dateOfPurchase: LocalDateTime,
    val udiValue: Double,
)

data class RetirementRecordPost(
    val purchaseTotal: Double,
    val dateOfPurchase: LocalDateTime,
    val udiValue: Double,
)

data class ResponseRetirementRecord(
    val retirementRecord: RetirementRecord? = null,
    val udiCommission: UdiCommission? = null,
    val udiConversions: UdiConversions? = null
)

data class UdiConversions(
    val udiConversion: Double,
    val udiCommissionConversion: Double
)

 class RetirementRecordEntity(
    id: EntityID<Long>
): LongEntity(id) {
     companion object: LongEntityClass<RetirementRecordEntity>(RetirementTable)

     var userId by RetirementTable.userId
     var purchaseTotal by RetirementTable.purchaseTotal
     var dateOfPurchase by RetirementTable.dateOfPurchase
     var udiValue by RetirementTable.udiValue

     fun toRetirementRecord() = RetirementRecord(
         id.value,
        userId,
         purchaseTotal,
         dateOfPurchase,
         udiValue
     )
 }
