package com.expenses.app.server.expensesappserver.repository

import com.expenses.app.server.expensesappserver.common.exceptions.EntityNotFoundException
import com.expenses.app.server.expensesappserver.common.responses.Status
import com.expenses.app.server.expensesappserver.security.AuthenticationFacade
import com.expenses.app.server.expensesappserver.ui.database.entities.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Repository

@Repository
class UdiRepository(
    private val authenticationFacade: AuthenticationFacade
) {
    val retirementRecordCrudTable = RetirementRecordEntity
    val udiEntityCrudTable = UdiEntity

    fun getUdiById(id: Int): ResponseRetirementRecord {
        val retirementRecord = findUdiById(id)
        val commission = retirementRecord.udiCommission
        val udiconversions =
            calculateCommissions(commission.userUdis, commission.UdiCommssion, retirementRecord.udiValue)
        return ResponseRetirementRecord(
            retirementRecord.id,
            retirementRecord,
            udiconversions
        )
    }

    fun getAllUdi(): List<ResponseRetirementRecord>? {
        val userId = authenticationFacade.userId()
        val retirementData = transaction {
            addLogger(StdOutSqlLogger)
            retirementRecordCrudTable.find { RetirementTable.userId eq userId }.map { it.toRetirementRecord() }
        }

        if (retirementData.isEmpty()) return emptyList()

        val udiResponseList = mutableListOf<ResponseRetirementRecord>()
        retirementData.forEach { value ->
            val commissionData = value.udiCommission
            val udiConversion = calculateCommissions(
                commissionData.userUdis,
                commissionData.UdiCommssion,
                value.udiValue
            )
            udiResponseList.add(
                ResponseRetirementRecord(
                    value.id,
                    value,
                    udiConversion
                )

            )
        }
        return udiResponseList.toList()
    }

    fun insertUdi(retirementRecord: RetirementRecordPost): ResponseRetirementRecord {
        val userIdName = authenticationFacade.userId()
        val mostRecentCommission = getMostRecentCommission()
        val totalOfUdiCalculation = retirementRecord.purchaseTotal / retirementRecord.udiValue
        val result = transaction {
            addLogger(StdOutSqlLogger)
            retirementRecordCrudTable
                .new {
                    userId = userIdName
                    udiCommission = mostRecentCommission
                    purchaseTotal = retirementRecord.purchaseTotal
                    totalOfUdi = totalOfUdiCalculation
                    dateOfPurchase = retirementRecord.dateOfPurchase
                    udiValue = retirementRecord.udiValue
                }.toRetirementRecord()
        }

        val rawCommissionUdi = mostRecentCommission.toUdiEntity()
        val udiconversions =
            calculateCommissions(rawCommissionUdi.userUdis, rawCommissionUdi.UdiCommssion, result.udiValue)
        return ResponseRetirementRecord(
            result.id,
            result,
            udiconversions
        )
    }

    fun updateUdi(id: Int, retirementRecordPost: RetirementRecordPost): ResponseRetirementRecord {
        val retirementRecord = findUdiById(id)
        val commission = retirementRecord.udiCommission
        val totalOfUdiCalculation = retirementRecord.purchaseTotal / retirementRecord.udiValue
        transaction {
            addLogger(StdOutSqlLogger)
            retirementRecordCrudTable
                .table.update({ RetirementTable.id eq id }) {
                    it[RetirementTable.udiValue] = retirementRecordPost.udiValue
                    it[RetirementTable.dateOfPurchase] = retirementRecordPost.dateOfPurchase
                    it[RetirementTable.purchaseTotal] = retirementRecordPost.purchaseTotal
                    it[RetirementTable.totalOfUdi] = totalOfUdiCalculation
                }
        }
        val udiconversions =
            calculateCommissions(commission.userUdis, commission.UdiCommssion, retirementRecordPost.udiValue)
        val newRetirementRecord = findUdiById(id)
        return ResponseRetirementRecord(
            retirementRecord.id,
            newRetirementRecord,
            udiconversions
        )
    }

    fun deleteUdi(id: Int): ResponseRetirementRecord {
        val singleRetirementRecord = findUdiById(id)
        transaction {
            retirementRecordCrudTable.table.deleteWhere { RetirementTable.id eq id }
        }
        return ResponseRetirementRecord(singleRetirementRecord.id, singleRetirementRecord)
    }

    fun getCommissions(): List<UdiCommission> {
        val commissions = transaction {
            udiEntityCrudTable.find { UdiEntityTable.userId eq authenticationFacade.userId() }
                .map {
                    it.toUdiEntity()
                }
        }

        return commissions
    }

    fun insertCommission(udiCommissionPost: UdiCommissionPost): UdiCommission {
        val recentAddedCommission = transaction {
            udiEntityCrudTable.new {
                userId = authenticationFacade.userId()
                userUdis = udiCommissionPost.userUdis
                udiCommision = udiCommissionPost.UdiCommssion
                dateAdded = udiCommissionPost.dateAdded
            }
        }
        return finCommissionById(recentAddedCommission.id.value)
    }


    fun updateCommission(udiCommissionPost: UdiCommissionPost): UdiCommission {
        val updatedCommissionId = transaction {
            udiEntityCrudTable.table.update({ UdiEntityTable.userId eq authenticationFacade.userId() and (UdiEntityTable.id eq udiCommissionPost.id) }) {
                it[UdiEntityTable.udiCommission] = udiCommissionPost.UdiCommssion
                it[UdiEntityTable.userUdis] = udiCommissionPost.userUdis
            }
        }
        return finCommissionById(updatedCommissionId)
    }


    fun findUdiById(id: Int) = transaction {
        retirementRecordCrudTable.find { RetirementTable.id eq id and (RetirementTable.userId eq authenticationFacade.userId()) }
            .limit(1).firstOrNull()?.toRetirementRecord()
    } ?: throw EntityNotFoundException(
        status = Status.NO_DATA,
        customMessage = "This udi id doesnt exists",
        id = authenticationFacade.userId()
    )

    private fun getMostRecentCommission() = transaction {
        udiEntityCrudTable.find { UdiEntityTable.userId eq authenticationFacade.userId() }
            .orderBy(UdiEntityTable.dateAdded to SortOrder.DESC).limit(1).firstOrNull()
    } ?: throw EntityNotFoundException(
        status = Status.NO_DATA,
        customMessage = "This user doesnt have a udi commission",
        id = authenticationFacade.userId()
    )

    private fun finCommissionById(id: Int) = transaction {
        udiEntityCrudTable.find { UdiEntityTable.userId eq authenticationFacade.userId() and (UdiEntityTable.id eq id) }
            .limit(1).firstOrNull()
            ?.toUdiEntity()
    } ?: throw EntityNotFoundException(
        status = Status.NO_DATA,
        customMessage = "This user doesnt have a udi commission",
        id = authenticationFacade.userId()
    )

    private fun calculateCommissions(userUdis: Double, udiCommission: Double, udiValue: Double): UdiConversions {
        return UdiConversions(
            udiConversion = userUdis * udiValue,
            udiCommissionConversion = udiValue * udiCommission,
        )
    }
}