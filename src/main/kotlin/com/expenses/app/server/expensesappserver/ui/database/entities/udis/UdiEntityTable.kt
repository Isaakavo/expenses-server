package com.expenses.app.server.expensesappserver.ui.database.entities.udis

import com.fasterxml.jackson.annotation.JsonIgnore
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

data class UdiBonus(
        val id: Int,
        @JsonIgnore
        val userId: String?,
        val monthlyBonus: Double,
        val udiCommission: Double,
        val yearlyBonus: Double,
        var monthlyTotalBonus: Double,
        val dateAdded: LocalDateTime
)

data class UdiBonusPost(
        val id: Int,
        val monthlyBonus: Double,
        val udiCommission: Double,
        var yearlyBonus: Double,
        var monthlyTotalBonus: Double,
        val dateAdded: LocalDateTime
) {

    init {
        this.yearlyBonus = (this.monthlyBonus + this.udiCommission) * 12
        this.monthlyTotalBonus = this.monthlyBonus + this.udiCommission
    }
}

object UdiEntityTable : IntIdTable("udi_commissions") {
    val userId: Column<String> = varchar("user_id", 50)
    val monthlyBonus: Column<Double> = double("monthly_bonus")
    val udiCommission: Column<Double> = double("udi_commission")
    val yearlyBonus: Column<Double> = double("yarly_bonus")
    val monthlyTotalBonus: Column<Double> = double("monthly_total_bonus")
    val dateAdded: Column<LocalDateTime> = datetime("date_added")
}

class UdiEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<UdiEntity>(UdiEntityTable)

    var userId by UdiEntityTable.userId
    var monthlyBonus by UdiEntityTable.monthlyBonus
    var udiCommission by UdiEntityTable.udiCommission
    var yearlyBonus by UdiEntityTable.yearlyBonus
    var monthlyTotalBonus by UdiEntityTable.monthlyTotalBonus
    var dateAdded by UdiEntityTable.dateAdded

    fun toUdiEntity() = UdiBonus(
            id.value,
            userId,
            monthlyBonus,
            udiCommission,
            yearlyBonus,
            monthlyTotalBonus,
            dateAdded
    )
}