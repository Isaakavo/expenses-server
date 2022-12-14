package com.expenses.app.server.expensesappserver.common.advice

import com.expenses.app.server.expensesappserver.common.exceptions.BadRequestException
import com.expenses.app.server.expensesappserver.common.exceptions.EntityNotFoundException
import com.expenses.app.server.expensesappserver.common.exceptions.UnauthorizedException
import com.expenses.app.server.expensesappserver.common.responses.BodyResponse
import com.expenses.app.server.expensesappserver.common.responses.ErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice


@RestControllerAdvice
class CustomErrorAdvice {
    @ExceptionHandler(EntityNotFoundException::class)
    fun handleNotFound(ex: EntityNotFoundException): ResponseEntity<Any> {
        return ResponseEntity(
            ErrorResponse(status = ex.status, BodyResponse(userId = ex.id, message = ex.customMessage)),
            HttpStatus.OK
        )
    }

    @ExceptionHandler(BadRequestException::class)
    fun handleBadRequest(ex: BadRequestException): ResponseEntity<Any> {
        return ResponseEntity(
            ErrorResponse(status = ex.status, BodyResponse(message = ex.customMessage)),
            HttpStatus.BAD_REQUEST
        )
    }

    @ExceptionHandler(UnauthorizedException::class)
    fun handleUnauthorizedException(ex: UnauthorizedException): ResponseEntity<Any> {
        return ResponseEntity(
            ErrorResponse(status = ex.status, BodyResponse(message = ex.customMessage)),
            HttpStatus.UNAUTHORIZED
        )
    }

    //TODO add a handler for deserialization errors
}