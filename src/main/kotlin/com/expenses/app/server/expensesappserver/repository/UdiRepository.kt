package com.expenses.app.server.expensesappserver.repository

import com.expenses.app.server.expensesappserver.ui.database.entities.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Repository

@Repository
class UdiRepository {
    val retirementRecordCrudTable = RetirementRecordEntity
    val udiEntityCrudTable = UdiEntity

    fun insert(retirementRecord: RetirementRecord): ResponseRetirementRecord? {
        //TODO Remove this check when endpoint to add commissions is added
        val commissionsCheck = transaction { findOneById(retirementRecord.userId) }
        if (commissionsCheck == null) {
            transaction {
                udiEntityCrudTable.new {
                    userId = retirementRecord.userId
                    userUdis = 437.12
                    udiCommision = 26.17
                }
            }
            println("Created commission value")
        }
        val result = transaction {
            addLogger(StdOutSqlLogger)
            retirementRecordCrudTable
                .new {
                    userId = retirementRecord.userId
                    purchaseTotal = retirementRecord.purchaseTotal
                    dateOfPurchase = retirementRecord.dateOfPurchase
                    udiValue = retirementRecord.udiValue
                }
        }.toRetirementRecord()
        val commissions = transaction { findOneById(retirementRecord.userId) }
        if (commissions != null) {
            val udiconversions = UdiConversions(
                commissions.userUdis * result.udiValue,
                result.udiValue * commissions.UdiCommssion
            )
            println("Data inserted")
            return ResponseRetirementRecord(
                result,
                commissions,
                udiconversions
            )
        }
        return null
    }

    fun findOneById(userId: String) =
        udiEntityCrudTable.find { UdiEntityTable.userId eq userId }.limit(1).firstOrNull()?.toUdiEntity()

    //fun findAll(id: String): List<RetirementRecord> = crudTable.select{ RetirementEntity.userId eq id }.map { it.toRetirementRecord() }

    //operator fun get(id: Long): RetirementRecord? =
    //  findOneById(id) ?: throw EntityNotFoundException("Record with id $id not found")

    //fun findOneById(id: Long) =
    //  crudTable.select { RetirementEntity.id eq id }.limit(1).map { it.toRetirementRecord() }.firstOrNull()
}

//private fun ResultRow.toRetirementRecord() = RetirementEntity.rowToRetirementRecord(this)