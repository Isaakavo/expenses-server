package com.expenses.app.server.expensesappserver.repository

import com.expenses.app.server.expensesappserver.common.exceptions.EntityNotFoundException
import com.expenses.app.server.expensesappserver.common.exceptions.UnauthorizedException
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

    fun getUdiById(id: Long, userId: String): ResponseRetirementRecord {
        val retirementRecord = findUdiById(id)
        validateOwnership(retirementRecord.userId, userId)
        val commissions = validatedCommissionById(retirementRecord.userId)
        val udiconversions =
            calculateCommissions(commissions.userUdis, commissions.UdiCommssion, retirementRecord.udiValue)
        return ResponseRetirementRecord(
            retirementRecord,
            commissions,
            udiconversions
        )
    }

    fun getAllUdi(userId: String): List<ResponseRetirementRecord>? {
        val commission = validatedCommissionById(userId)
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
        val commissions = validatedCommissionById(retirementRecord.userId)
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
        val commissions = validatedCommissionById(retirementRecord.userId)
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

    fun insertCommission(udiCommissionPost: UdiCommissionPost): UdiCommission {
        transaction {
            udiEntityCrudTable.new {
                userId = udiCommissionPost.userId
                userUdis = udiCommissionPost.userUdis
                udiCommision = udiCommissionPost.UdiCommssion
            }
        }
        return validatedCommissionById(udiCommissionPost.userId)
    }


    fun updateCommission(udiCommissionPost: UdiCommissionPost): UdiCommission {
        return when (findCommissionById(udiCommissionPost.userId)) {
            null -> {
                insertCommission(udiCommissionPost)
            }

            else -> {
                transaction {
                    udiEntityCrudTable.table.update({ UdiEntityTable.userId eq udiCommissionPost.userId }) {
                        it[UdiEntityTable.udiCommission] = udiCommissionPost.UdiCommssion
                        it[UdiEntityTable.userUdis] = udiCommissionPost.userUdis
                    }
                }
                validatedCommissionById(udiCommissionPost.userId)
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

    fun validatedCommissionById(userId: String) = findCommissionById(userId) ?: throw EntityNotFoundException(
        status = Status.NO_COMMISSION_DATA,
        customMessage = "No data for this user",
        id = userId
    )

    fun findCommissionById(userId: String) =
        transaction {
            udiEntityCrudTable.find { UdiEntityTable.userId eq userId }.limit(1).firstOrNull()?.toUdiEntity()
        }

    private fun validateOwnership(userId: String, bodyUserId: String) {
        if (userId != bodyUserId) throw UnauthorizedException(
            status = Status.UNAUTHORIZED,
            customMessage = "The user doesnt own this data"
        )
    }

    private fun calculateCommissions(userUdis: Double, udiCommission: Double, udiValue: Double): UdiConversions {
        return UdiConversions(
            udiConversion = userUdis * udiValue,
            udiCommissionConversion = udiValue * udiCommission
        )
    }
}

//private fun ResultRow.toRetirementRecord() = RetirementEntity.rowToRetirementRecord(this)