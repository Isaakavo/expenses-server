package com.expenses.app.server.expensesappserver.ui.database.entities

import com.fasterxml.jackson.annotation.JsonIgnore
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

data class UdiCommission(
    @JsonIgnore
    val userId: String?,
    val userUdis: Double,
    val UdiCommssion: Double,
    val dateAdded: LocalDateTime
)

data class UdiCommissionPost(
    val userUdis: Double,
    val UdiCommssion: Double,
    val dateAdded: LocalDateTime
)

object UdiEntityTable: IntIdTable("udi_commissions") {
    val userId: Column<String> = varchar("user_id", 50)
    val userUdis: Column<Double> = double("user_udis")
    val udiCommission: Column<Double> = double("udi_commission")
    val dateAdded: Column<LocalDateTime> = datetime("date_added")
}

class UdiEntity(id: EntityID<Int>): IntEntity(id) {
    companion object: IntEntityClass<UdiEntity>(UdiEntityTable)

    var userId by UdiEntityTable.userId
    var userUdis by UdiEntityTable.userUdis
    var udiCommision by UdiEntityTable.udiCommission
    var dateAdded by UdiEntityTable.dateAdded

    fun toUdiEntity() = UdiCommission(
        userId,
        userUdis,
        udiCommision,
        dateAdded
    )
}