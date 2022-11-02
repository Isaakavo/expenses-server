package com.expenses.app.server.expensesappserver.repository

import com.expenses.app.server.expensesappserver.common.responses.BodyResponse
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
    fun getUdiById(id: Long): BodyResponse<ResponseRetirementRecord> {
        val retirementRecord = findUdiById(id) ?: return BodyResponse(message = "This record doesnt exists")
        val commissions = findCommissionById(retirementRecord.userId) ?: return BodyResponse( userId = retirementRecord.userId, message = "This user doesnt have commissions")
        val udiconversions =
            calculateCommissions(commissions.userUdis, commissions.UdiCommssion, retirementRecord.udiValue)
        return BodyResponse(
            userId = retirementRecord.userId,
            message = "Element found",
            data = listOf(
                ResponseRetirementRecord(
                    retirementRecord,
                    commissions,
                    udiconversions
                )
            )
        )
    }

    fun getAllUdi(userId: String): BodyResponse<ResponseRetirementRecord>? {
        val commission = transaction { findCommissionById(userId) }
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
                    value.udiValue
                )
                udiResponseList.add(
                    ResponseRetirementRecord(
                        value,
                        commission, udiConversion
                    )

                )
            }
            return BodyResponse(userId = userId, data = udiResponseList)
        }
        return null
    }

    fun insertUdi(retirementRecord: RetirementRecordPost): BodyResponse<ResponseRetirementRecord> {
        val commissions = findCommissionById(retirementRecord.userId)
            ?: return BodyResponse(
                userId = retirementRecord.userId,
                message = "Couldn't found a commission value for this user, please add a new one",
                data = emptyList()
            )
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
        return BodyResponse(
            userId = retirementRecord.userId,
            message = "Element inserted with id ${result.id}",
            data = listOf(
                ResponseRetirementRecord(
                    result,
                    commissions,
                    udiconversions
                )
            )
        )
    }

    fun updateUdi(id: Long, retirementRecordPost: RetirementRecordPost): BodyResponse<ResponseRetirementRecord> {
        val retirementRecord = findUdiById(id) ?: return BodyResponse(message = "This record doesnt exists")
        val commissions = findCommissionById(retirementRecord.userId)
        if (commissions != null) {
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
            return if (newRetirementRecord != null) BodyResponse(
                userId = retirementRecord.userId,
                message = "Element updated",
                data = listOf(
                    ResponseRetirementRecord(
                        newRetirementRecord,
                        commissions,
                        udiconversions
                    )
                )
            ) else BodyResponse("something went wrong")
        }
        return BodyResponse("something went wrong")
    }

    fun deleteUdi(id: Long): BodyResponse<ResponseRetirementRecord> {
        val singleRetirementRecord = transaction {
            retirementRecordCrudTable.find { RetirementTable.id eq id }.limit(1).firstOrNull()?.toRetirementRecord()
        }
        if (singleRetirementRecord != null) {
            transaction {
                retirementRecordCrudTable.table.deleteWhere { RetirementTable.id eq id }
            }
            return BodyResponse(userId = singleRetirementRecord.userId, message = "Element deleted")
        }

        return BodyResponse(message = "Element Doest exists")
    }

    fun insertUpdateCommission(udiCommissionPost: UdiCommissionPost): BodyResponse<UdiCommission>? {
        when (findCommissionById(udiCommissionPost.userId)) {
            null -> {
                val result = transaction {
                    udiEntityCrudTable.new {
                        userId = udiCommissionPost.userId
                        userUdis = udiCommissionPost.userUdis
                        udiCommision = udiCommissionPost.UdiCommssion
                    }
                }.toUdiEntity()
                return BodyResponse(userId = udiCommissionPost.userId, message = "Created new element", listOf(result))
            }

            else -> {
                transaction {
                    udiEntityCrudTable.table.update({ UdiEntityTable.userId eq udiCommissionPost.userId }) {
                        it[UdiEntityTable.udiCommission] = udiCommissionPost.UdiCommssion
                        it[UdiEntityTable.userUdis] = udiCommissionPost.userUdis
                    }
                }
                val commission = findCommissionById(udiCommissionPost.userId)
                return if (commission != null) BodyResponse(
                    userId = udiCommissionPost.userId,
                    message = "Updated element",
                    listOf(commission)
                )
                else BodyResponse(
                    userId = udiCommissionPost.userId,
                    message = "Couldt update the element"
                )
            }
        }
    }


    fun findUdiById(id: Long) = transaction {
        retirementRecordCrudTable.find { RetirementTable.id eq id }.limit(1).firstOrNull()?.toRetirementRecord()
    }

    fun findCommissionById(userId: String) =
        transaction {
            udiEntityCrudTable.find { UdiEntityTable.userId eq userId }.limit(1).firstOrNull()?.toUdiEntity()
        }

//    fun findCommissionById(id: Int) =
//        transaction {
//            udiEntityCrudTable.find { UdiEntityTable.id eq id }.limit(1).firstOrNull()?.toUdiEntity()
//        }



    fun getCommissionById(userId: String): BodyResponse<UdiCommission?>? {
        return BodyResponse(userId = userId, message = "Returning value", listOf(findCommissionById(userId)))
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