package com.expenses.app.server.expensesappserver.common.responses

import com.expenses.app.server.expensesappserver.ui.database.entities.ResponseRetirementRecord
import com.expenses.app.server.expensesappserver.ui.database.entities.UdiCommission


enum class Status {
    FAIL, SUCCESS
}

data class ApiResponse(val status: Status, val body: BodyResponse<ResponseRetirementRecord>?)

data class ApiResponseCommission(val status: Status, val body: BodyResponse<UdiCommission?>?)

data class BodyResponse<out T>(
    val userId: String? = null,
    val message: String = "",
    val data: List<T> = emptyList(),
)