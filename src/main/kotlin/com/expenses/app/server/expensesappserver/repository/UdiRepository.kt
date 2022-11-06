package com.expenses.app.server.expensesappserver.repository

import com.expenses.app.server.expensesappserver.common.exceptions.EntityNotFoundException
import com.expenses.app.server.expensesappserver.common.responses.Status
import com.expenses.app.server.expensesappserver.ui.database.entities.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Repository

@Repository
class UdiRepository {
    val retirementRecordCrudTable = RetirementRecordEntity
    val udiEntityCrudTable = UdiEntity

    //TODO implement logic to check if the row belongs to the user id
    fun getUdiById(id: Long): ResponseRetirementRecord {
        val retirementRecord = findUdiById(id)
        val commissions = findCommissionById(retirementRecord.userId)
        val udiconversions =
            calculateCommissions(commissions.userUdis, commissions.UdiCommssion, retirementRecord.udiValue)
        return ResponseRetirementRecord(
            retirementRecord,
            commissions,
            udiconversions
        )
    }

    fun getAllUdi(userId: String): List<ResponseRetirementRecord>? {
        val commission = transaction { findCommissionById(userId) }
        val retirementData = transaction {
            addLogger(StdOutSqlLogger)
            retirementRecordCrudTable.find { RetirementTable.userId eq userId }.map { it.toRetirementRecord() }
        }
        val udiResponseList = mutableListOf<ResponseRetirementRecord>()
        retirementData.forEach { value ->
            val udiConversion = calculateCommissions(
                commission.userUdis,
                commission.UdiCommssion,
                value.udiValue
            )
            udiResponseList.add(
                ResponseRetirementRecord(
                    value,
                    commission, udiConversion
                )

            )
        }
        return udiResponseList.toList()
    }

    fun insertUdi(retirementRecord: RetirementRecordPost): ResponseRetirementRecord {
        val commissions = findCommissionById(retirementRecord.userId)
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
        val udiconversions =
            calculateCommissions(commissions.userUdis, commissions.UdiCommssion, result.udiValue)
        return ResponseRetirementRecord(
            result,
            commissions,
            udiconversions
        )
    }

    fun updateUdi(id: Long, retirementRecordPost: RetirementRecordPost): ResponseRetirementRecord {
        val retirementRecord = findUdiById(id)
        val commissions = findCommissionById(retirementRecord.userId)
        transaction {
            addLogger(StdOutSqlLogger)
            retirementRecordCrudTable
                .table.update({ RetirementTable.id eq id }) {
                    it[RetirementTable.udiValue] = retirementRecordPost.udiValue
                    it[RetirementTable.dateOfPurchase] = retirementRecordPost.dateOfPurchase
                    it[RetirementTable.purchaseTotal] = retirementRecordPost.purchaseTotal
                }
        }
        val udiconversions =
            calculateCommissions(commissions.userUdis, commissions.UdiCommssion, retirementRecordPost.udiValue)
        val newRetirementRecord = findUdiById(id)
        return ResponseRetirementRecord(
            newRetirementRecord,
            commissions,
            udiconversions
        )
    }

    fun deleteUdi(id: Long): ResponseRetirementRecord {
        val singleRetirementRecord = findUdiById(id)
        transaction {
            retirementRecordCrudTable.table.deleteWhere { RetirementTable.id eq id }
        }
        return ResponseRetirementRecord(singleRetirementRecord)
    }

    //TODO find a way to detect if the commission exist or not, and if not insert a new one
    fun insertUpdateCommission(udiCommissionPost: UdiCommissionPost): UdiCommission {
        when (findCommissionById(udiCommissionPost.userId)) {
            else -> {
                transaction {
                    udiEntityCrudTable.table.update({ UdiEntityTable.userId eq udiCommissionPost.userId }) {
                        it[UdiEntityTable.udiCommission] = udiCommissionPost.UdiCommssion
                        it[UdiEntityTable.userUdis] = udiCommissionPost.userUdis
                    }
                }
                return findCommissionById(udiCommissionPost.userId)
            }
        }
    }


    fun findUdiById(id: Long) = transaction {
        retirementRecordCrudTable.find { RetirementTable.id eq id }.limit(1).firstOrNull()?.toRetirementRecord()
    } ?: throw EntityNotFoundException(
        status = Status.NO_DATA,
        customMessage = "This udi id doesnt exists",
        id = id.toString()
    )

    fun findCommissionById(userId: String) =
        transaction {
            udiEntityCrudTable.find { UdiEntityTable.userId eq userId }.limit(1).firstOrNull()?.toUdiEntity()
        } ?: throw EntityNotFoundException(
            status = Status.NO_COMMISSION_DATA,
            customMessage = "No data for this user",
            id = userId
        )

//    fun findCommissionById(id: Int) =
//        transaction {
//            udiEntityCrudTable.find { UdiEntityTable.id eq id }.limit(1).firstOrNull()?.toUdiEntity()
//        }

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