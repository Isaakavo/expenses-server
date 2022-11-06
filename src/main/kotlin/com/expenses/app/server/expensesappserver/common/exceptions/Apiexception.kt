package com.expenses.app.server.expensesappserver.common.exceptions

import com.expenses.app.server.expensesappserver.common.responses.Status
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import java.lang.RuntimeException

@ResponseStatus(value = HttpStatus.NOT_FOUND)
data class EntityNotFoundException(val status: Status, val customMessage: String, val id: String): RuntimeException(customMessage)

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
class BadRequestException(message: String): RuntimeException(message)