package com.expenses.app.server.expensesappserver.repository

import com.expenses.app.server.expensesappserver.common.exceptions.EntityNotFoundException
import com.expenses.app.server.expensesappserver.ui.database.entities.RetirementEntity
import com.expenses.app.server.expensesappserver.ui.database.entities.RetirementRecord
import com.expenses.app.server.expensesappserver.ui.database.entities.rowToRetirementRecord
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Repository

@Repository
class UdiRepository {
    val crudTable = RetirementEntity

    fun insert(retirementRecord: RetirementRecord): EntityID<Long> {
        val result = transaction {
            addLogger(StdOutSqlLogger)
            crudTable
                .insertAndGetId {
                    it[userId] = retirementRecord.userId
                    it[purchaseTotal] = retirementRecord.purchaseTotal
                    it[dateOfPurchase] = retirementRecord.dateOfPurchase
                    it[udiValue] = retirementRecord.udiValue
                }
        }
        println("Data inserted")
        return result
    }

    fun findAll(id: Long): List<RetirementRecord> = crudTable.select{ RetirementEntity.userId eq id }.map { it.toRetirementRecord() }

    operator fun get(id: Long): RetirementRecord? =
        findOneById(id) ?: throw EntityNotFoundException("Record with id $id not found")

    fun findOneById(id: Long) =
        crudTable.select { RetirementEntity.id eq id }.limit(1).map { it.toRetirementRecord() }.firstOrNull()
}

private fun ResultRow.toRetirementRecord() = RetirementEntity.rowToRetirementRecord(this)