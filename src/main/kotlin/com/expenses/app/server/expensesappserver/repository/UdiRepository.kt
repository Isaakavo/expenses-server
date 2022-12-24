package com.expenses.app.server.expensesappserver.repository

import com.expenses.app.server.expensesappserver.common.exceptions.EntityNotFoundException
import com.expenses.app.server.expensesappserver.common.responses.Status
import com.expenses.app.server.expensesappserver.security.AuthenticationFacade
import com.expenses.app.server.expensesappserver.ui.database.entities.udis.*
import com.expenses.app.server.expensesappserver.utils.loggedTransaction
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class UdiRepository(
    private val authenticationFacade: AuthenticationFacade
) {

    companion object {
        val logger: Logger = LoggerFactory.getLogger(UdiRepository::class.java)
    }

    val retirementRecordCrudTable = RetirementRecordEntity
    val udiEntityCrudTable = UdiEntity

    fun getUdiById(id: Int): ResponseRetirementRecord {
        val retirementRecord = findUdiById(id)
        val commission = retirementRecord.udiBonus
        val udiConversions =
            calculateCommissions(commission.monthlyBonus, commission.udiCommission, retirementRecord.udiValue)
        val response = ResponseRetirementRecord(
            retirementRecord.id,
            retirementRecord,
            udiConversions
        )
        logger.info("Returning udi By Id with id $id")
        return response
    }

    fun getAllUdi(): List<ResponseRetirementRecord>? {
        val userId = authenticationFacade.userId()
        val retirementData = loggedTransaction {
            retirementRecordCrudTable.find { RetirementTable.userId eq userId }
                .orderBy(RetirementTable.dateOfPurchase to SortOrder.DESC)
                .map { it.toRetirementRecord() }
        }

        if (retirementData.isEmpty()) return emptyList()

        val udiResponseList = mutableListOf<ResponseRetirementRecord>()
        retirementData.forEach { value ->
            val commissionData = value.udiBonus
            val udiConversion = calculateCommissions(
                commissionData.monthlyBonus,
                commissionData.udiCommission,
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
        logger.info("Returning all udis with size ${udiResponseList.size}")
        return udiResponseList.toList()
    }

    fun insertUdi(retirementRecord: RetirementRecordPost): ResponseRetirementRecord {
        val userIdName = authenticationFacade.userId()
        val mostRecentCommission = getMostRecentCommission()
        val totalOfUdiCalculation = retirementRecord.purchaseTotal / retirementRecord.udiValue
        val result = loggedTransaction {
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
            calculateCommissions(rawCommissionUdi.monthlyBonus, rawCommissionUdi.udiCommission, result.udiValue)
        val insertedValue = ResponseRetirementRecord(
            result.id,
            result,
            udiconversions
        )
        logger.info("Inserted udi")
        return insertedValue
    }

    fun updateUdi(id: Int, retirementRecordPost: RetirementRecordPost): ResponseRetirementRecord {
        val retirementRecord = findUdiById(id)
        val commission = retirementRecord.udiBonus
        loggedTransaction {
            retirementRecordCrudTable
                .table.update({ RetirementTable.id eq id }) {
                    it[RetirementTable.udiValue] = retirementRecordPost.udiValue
                    it[RetirementTable.dateOfPurchase] = retirementRecordPost.dateOfPurchase
                    it[RetirementTable.purchaseTotal] = retirementRecordPost.purchaseTotal
                    it[RetirementTable.totalOfUdi] = retirementRecordPost.purchaseTotal / retirementRecordPost.udiValue
                }
        }
        val udiconversions =
            calculateCommissions(commission.monthlyBonus, commission.udiCommission, retirementRecordPost.udiValue)
        val newRetirementRecord = findUdiById(id)
        val updatedValue = ResponseRetirementRecord(
            retirementRecord.id,
            newRetirementRecord,
            udiconversions
        )
        logger.info("Updated udi value with Id $id")
        return updatedValue
    }

    fun deleteUdi(id: Int): ResponseRetirementRecord {
        val singleRetirementRecord = findUdiById(id)
        loggedTransaction {
            retirementRecordCrudTable.table.deleteWhere { RetirementTable.id eq id }
        }
        val deletedUdi = ResponseRetirementRecord(singleRetirementRecord.id, singleRetirementRecord)
        logger.info("Deleted udi with Id $id")
        return deletedUdi
    }

    fun getCommissions(): List<UdiBonus> {
        val commissions = loggedTransaction {
            udiEntityCrudTable.find { UdiEntityTable.userId eq authenticationFacade.userId() }
                .map {
                    it.toUdiEntity()
                }
        }
        logger.info("Get commission")
        return commissions
    }

    fun insertCommission(udiBonusPost: UdiBonusPost): UdiBonus {
        val recentAddedCommission = loggedTransaction {
            val monthlyTotalBonusCal = udiBonusPost.monthlyBonus + udiBonusPost.udiCommission
            udiEntityCrudTable.new {
                userId = authenticationFacade.userId()
                monthlyBonus = udiBonusPost.monthlyBonus
                udiCommission = udiBonusPost.udiCommission
                yearlyBonus = udiBonusPost.yearlyBonus
                monthlyTotalBonus = monthlyTotalBonusCal
                dateAdded = udiBonusPost.dateAdded
            }
        }
        val commission = finCommissionById(recentAddedCommission.id.value)
        logger.info("Inserted new commission with value $udiBonusPost")
        return commission
    }


    fun updateCommission(udiBonusPost: UdiBonusPost, id: Int): UdiBonus {
        val updatedCommissionId = loggedTransaction {
            udiEntityCrudTable.table
                .update({
                    UdiEntityTable.userId eq authenticationFacade.userId() and (UdiEntityTable.id eq id)
                }) {
                    it[UdiEntityTable.udiCommission] = udiBonusPost.udiCommission
                    it[UdiEntityTable.monthlyBonus] = udiBonusPost.monthlyBonus
                    it[UdiEntityTable.yearlyBonus] = udiBonusPost.yearlyBonus
                    it[UdiEntityTable.monthlyTotalBonus] = udiBonusPost.monthlyTotalBonus
                }
        }
        val commission = finCommissionById(updatedCommissionId)
        logger.info("Updated commission with value $udiBonusPost")
        return commission
    }

    fun deleteCommission(id: Int): UdiBonus {
        val commission = finCommissionById(id)
        loggedTransaction {
            udiEntityCrudTable.table.deleteWhere { UdiEntityTable.id eq id }
        }
        logger.info("Deleted commission with Id $id")
        return commission
    }


    fun findUdiById(id: Int) = loggedTransaction {
        retirementRecordCrudTable.find { RetirementTable.id eq id and (RetirementTable.userId eq authenticationFacade.userId()) }
            .limit(1).firstOrNull()?.toRetirementRecord()
    } ?: throw EntityNotFoundException(
        status = Status.NO_DATA,
        customMessage = "This udi id doesnt exists",
        id = authenticationFacade.userId()
    )

    fun getGlobalDetails(udiValue: Double): UdiGlobalDetails {
        val userIdName = authenticationFacade.userId()
        val retirementList = getAllUdi()
        val udiBonus = getAllCommissions()
        var totalExpended = 0.0
        var totalUdis = 0.0

        retirementList?.map {
            totalExpended += it.retirementRecord?.purchaseTotal ?: 0.0
            totalUdis += it.retirementRecord?.totalOfUdi ?: 0.0
        }

        val totalConversion: Double = totalUdis * udiValue
        val rendimiento = totalConversion - totalExpended
        val result = UdiGlobalDetails(
            userId = userIdName,
            totalExpend = totalExpended,
            udisTotal = totalUdis,
            udisConvertion = totalConversion,
            rendimiento = rendimiento,
            startDate = null,
            endDate = null,
            paymentDeadLine = null,
            udiBonus = udiBonus
        )
        logger.info("Returned Global details values with udiValue $udiValue")
        return result
    }

    private fun getAllCommissions() = loggedTransaction {
        udiEntityCrudTable.find { UdiEntityTable.userId eq authenticationFacade.userId() }.map {
            it.toUdiEntity()
        }
    }

    private fun getMostRecentCommission() = loggedTransaction {
        udiEntityCrudTable.find { UdiEntityTable.userId eq authenticationFacade.userId() }
            .orderBy(UdiEntityTable.dateAdded to SortOrder.DESC).limit(1).firstOrNull()
    } ?: throw EntityNotFoundException(
        status = Status.NO_DATA,
        customMessage = "This user doesnt have a udi commission",
        id = authenticationFacade.userId()
    )

    private fun finCommissionById(id: Int) = loggedTransaction {
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


