package com.expenses.app.server.expensesappserver.ui.database.entities

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column

object UdiEntityTable: IntIdTable("udis_commisions") {
    val userId: Column<String> = varchar("user_id", 50)
    val userUdis: Column<Double> = double("user_udis")
    val udiCommission: Column<Double> = double("udi_commission")
}

data class UdiCommission(
    val userId: String,
    val userUdis: Double,
    val UdiCommssion: Double
)

class UdiEntity(id: EntityID<Int>): IntEntity(id) {
    companion object: IntEntityClass<UdiEntity>(UdiEntityTable)

    var userId by UdiEntityTable.userId
    var userUdis by UdiEntityTable.userUdis
    var udiCommision by UdiEntityTable.udiCommission

    fun toUdiEntity() = UdiCommission(
        userId,
        userUdis,
        udiCommision
    )
}