package com.expenses.app.server.expensesappserver.common.responses

import com.expenses.app.server.expensesappserver.ui.database.entities.ResponseRetirementRecord

data class ApiResponse(val status: Status, val message: String, val body: BodyResponse<ResponseRetirementRecord>?) {

    enum class Status {
        FAIL, SUCCESS
    }
}

data class BodyResponse<out T>(
    val obj: List<T>,
)