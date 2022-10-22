package com.expenses.app.server.expensesappserver.common.responses

data class ApiResponse(val status: Status, val message: String) {

    enum class Status {
        FAIL, SUCCESS
    }
}