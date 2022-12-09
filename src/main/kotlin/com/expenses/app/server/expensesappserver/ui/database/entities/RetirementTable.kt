package com.expenses.app.server.expensesappserver.ui.database.entities

import com.fasterxml.jackson.annotation.JsonIgnore
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

data class RetirementRecord(
        var id: Int? = null,
        @JsonIgnore
    val userId: String,
        val purchaseTotal: Double,
        val udiValue: Double,
        val totalOfUdi: Double,
        val dateOfPurchase: LocalDateTime,
        val udiBonus: UdiBonus,
)

data class RetirementRecordPost(
    val purchaseTotal: Double,
    val dateOfPurchase: LocalDateTime,
    val udiValue: Double,
)

data class ResponseRetirementRecord(
    @JsonIgnore
    val id: Int?,
    val retirementRecord: RetirementRecord? = null,
    val udiConversions: UdiConversions? = null
)

data class UdiConversions(
    val udiConversion: Double,
    val udiCommissionConversion: Double,
)

data class UdiGlobalDetails(
        val userId: String,
        var totalExpend: Double = 0.0,
        var udisTotal: Double = 0.0,
        var udisConvertion: Double = 0.0,
        var rendimiento: Double = 0.0,
        var startDate: Long? = null,
        var endDate: Long? = null,
        var paymentDeadLine: Int? = null,
        val udiBonus: List<UdiBonus>
)

object RetirementTable : IntIdTable("udi") {
    val userId: Column<String> = varchar("user_id", 50)
    val udiCommission = reference("udi_commission", UdiEntityTable, onDelete = ReferenceOption.CASCADE)
    val purchaseTotal: Column<Double> = double("purchase_total")
    val totalOfUdi: Column<Double> = double("total_udi")
    val dateOfPurchase: Column<LocalDateTime> = datetime("date_purchase")
    val udiValue: Column<Double> = double("udi_value")
}

class RetirementRecordEntity(
    id: EntityID<Int>
) : IntEntity(id) {
    companion object : IntEntityClass<RetirementRecordEntity>(RetirementTable)

    var userId by RetirementTable.userId
    var udiCommission by UdiEntity referencedOn RetirementTable.udiCommission
    var purchaseTotal by RetirementTable.purchaseTotal
    var totalOfUdi by RetirementTable.totalOfUdi
    var dateOfPurchase by RetirementTable.dateOfPurchase
    var udiValue by RetirementTable.udiValue

    fun toRetirementRecord() = RetirementRecord(
        id.value,
        userId,
        purchaseTotal,
        udiValue,
        totalOfUdi,
        dateOfPurchase,
        udiCommission.toUdiEntity(),
    )
}
