package com.expenses.app.server.expensesappserver.common.exceptions

import com.expenses.app.server.expensesappserver.common.responses.Status
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import java.lang.RuntimeException

data class EntityNotFoundException(val status: Status, val customMessage: String, val id: String): RuntimeException(customMessage)

class BadRequestException(val status: Status, val customMessage: String): RuntimeException(customMessage)

@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
data class UnauthorizedException(val status: Status, val customMessage: String): RuntimeException(customMessage)