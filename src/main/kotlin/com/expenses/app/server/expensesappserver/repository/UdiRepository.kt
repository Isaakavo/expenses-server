package com.expenses.app.server.expensesappserver.repository

import com.expenses.app.server.expensesappserver.common.responses.BodyResponse
import com.expenses.app.server.expensesappserver.ui.database.entities.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Repository

@Repository
class UdiRepository {
    val retirementRecordCrudTable = RetirementRecordEntity
    val udiEntityCrudTable = UdiEntity

    fun insert(retirementRecord: RetirementRecordPost): BodyResponse<ResponseRetirementRecord>? {
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
            val udiconversions =
                calculateCommissions(commissions.userUdis, commissions.UdiCommssion, result.udiValue!!)
            println("Data inserted")
            return BodyResponse(
                userId = retirementRecord.userId,
                obj = listOf(
                    ResponseRetirementRecord(
                        result,
                        commissions,
                        udiconversions
                    )
                )
            )

        }
        return null
    }

    fun findOneById(userId: String) =
        udiEntityCrudTable.find { UdiEntityTable.userId eq userId }.limit(1).firstOrNull()?.toUdiEntity()

    fun getAllUdi(userId: String): BodyResponse<ResponseRetirementRecord>? {
        val commission = transaction { findOneById(userId) }
        if (commission != null) {
            val retirementData = transaction {
                addLogger(StdOutSqlLogger)
                retirementRecordCrudTable.find { RetirementTable.userId eq userId }.map { it.toRetirementRecord() }
            }
            val udiResponseList = mutableListOf<ResponseRetirementRecord>()
            retirementData.forEach { value ->
                val udiConversion = calculateCommissions(
                    commission.userUdis,
                    commission.UdiCommssion,
                    value.udiValue!!
                )
                udiResponseList.add(
                    ResponseRetirementRecord(
                        value,
                        commission, udiConversion
                    )

                )
            }
            return BodyResponse(userId = userId, obj = udiResponseList)
        }
        return null
    }

    //fun findAll(id: String): List<RetirementRecord> = crudTable.select{ RetirementEntity.userId eq id }.map { it.toRetirementRecord() }

    //operator fun get(id: Long): RetirementRecord? =
    //  findOneById(id) ?: throw EntityNotFoundException("Record with id $id not found")

    //fun findOneById(id: Long) =
    //  crudTable.select { RetirementEntity.id eq id }.limit(1).map { it.toRetirementRecord() }.firstOrNull()

    private fun calculateCommissions(userUdis: Double, udiCommission: Double, udiValue: Double): UdiConversions {
        return UdiConversions(
            udiConversion = userUdis * udiValue,
            udiCommissionConversion = udiValue * udiCommission
        )
    }
}

//private fun ResultRow.toRetirementRecord() = RetirementEntity.rowToRetirementRecord(this)