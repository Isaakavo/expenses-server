package com.expenses.app.server.expensesappserver.common.advice

import com.expenses.app.server.expensesappserver.common.responses.ApiResponse
import com.expenses.app.server.expensesappserver.common.responses.BodyResponse
import com.expenses.app.server.expensesappserver.common.responses.Status
import com.expenses.app.server.expensesappserver.ui.database.entities.ResponseRetirementRecord
import com.expenses.app.server.expensesappserver.ui.database.entities.UdiCommission
import com.expenses.app.server.expensesappserver.ui.database.entities.UdiGlobalDetails
import org.springframework.core.MethodParameter
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice

@RestControllerAdvice
class CustomResponseAdvice : ResponseBodyAdvice<Any> {

    override fun supports(returnType: MethodParameter, converterType: Class<out HttpMessageConverter<*>>): Boolean {
        return true
    }

    override fun beforeBodyWrite(
        body: Any?,
        returnType: MethodParameter,
        selectedContentType: MediaType,
        selectedConverterType: Class<out HttpMessageConverter<*>>,
        request: ServerHttpRequest,
        response: ServerHttpResponse
    ): Any? {

        when (body) {
            is List<*> -> {
                if (body.isEmpty()) return ApiResponse(
                    Status.NO_DATA,
                    BodyResponse(
                        message = "No data for this user",
                        size = 0,
                    )
                )
                when (val objType = body[0]) {
                    is ResponseRetirementRecord -> {
                        response.setStatusCode(HttpStatus.OK)
                        return ApiResponse(
                                Status.SUCCESS,
                                BodyResponse(
                                        userId = objType.retirementRecord?.userId,
                                        message = "",
                                        size = body.size,
                                        data = body
                                )
                        )
                    }
                    is UdiCommission -> {
                        response.setStatusCode(HttpStatus.OK)
                        return ApiResponse(
                                Status.SUCCESS,
                                BodyResponse(
                                        userId = objType.userId,
                                        message = "",
                                        size = body.size,
                                        data = body
                                )
                        )
                    }
                }
            }
            is ResponseRetirementRecord -> {
                return ApiResponse(
                    Status.SUCCESS,
                    BodyResponse(userId = body.retirementRecord?.userId, message = "", size = 1, data = listOf(body))
                )
            }
            is UdiCommission -> {
                return ApiResponse(
                    Status.SUCCESS,
                    BodyResponse(userId = body.userId, message = "", size = 1, listOf(body))
                )
            }

            is UdiGlobalDetails -> {
                response.setStatusCode(HttpStatus.OK)
                return ApiResponse(
                        Status.SUCCESS,
                        BodyResponse(
                                userId = body.userId,
                                message = "",
                                size = 1,
                                data = listOf(body)
                        )
                )
            }
        }
        return body
    }
}


