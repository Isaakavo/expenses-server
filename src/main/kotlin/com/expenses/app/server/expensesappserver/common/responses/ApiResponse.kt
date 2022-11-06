package com.expenses.app.server.expensesappserver.common.responses

enum class Status {
    FAIL, SUCCESS, NO_DATA, NO_COMMISSION_DATA
}

data class ApiResponse(val status: Status, val body: BodyResponse<Any?>?)

data class ErrorResponse(val status: Status, val body: BodyResponse<Any>?)

data class BodyResponse<out T>(
    val userId: String? = null,
    val message: String = "",
    val size: Int = 0,
    val data: List<T> = emptyList(),
)