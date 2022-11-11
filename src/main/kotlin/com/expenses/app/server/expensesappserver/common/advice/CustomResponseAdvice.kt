package com.expenses.app.server.expensesappserver.common.advice

import com.expenses.app.server.expensesappserver.common.responses.ApiResponse
import com.expenses.app.server.expensesappserver.common.responses.BodyResponse
import com.expenses.app.server.expensesappserver.common.responses.Status
import com.expenses.app.server.expensesappserver.ui.database.entities.ResponseRetirementRecord
import com.expenses.app.server.expensesappserver.ui.database.entities.UdiCommission
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
                val objType = body[0]
                if (objType is ResponseRetirementRecord) {
                    response.setStatusCode(HttpStatus.OK)
                    return ApiResponse(
                        Status.SUCCESS,
                        BodyResponse(
                            userId = objType.udiCommission?.userId,
                            message = "",
                            size = body.size,
                            data = body
                        )
                    )

                }
            }
            is ResponseRetirementRecord -> {
                //response.setStatusCode(HttpStatus.OK)
                return ApiResponse(
                    Status.SUCCESS,
                    BodyResponse(userId = body.udiCommission?.userId, message = "", size = 1, data = listOf(body))
                )
            }
            is UdiCommission -> {
                return ApiResponse(
                    Status.SUCCESS,
                    BodyResponse(userId = body.userId, message = "", size = 1, listOf(body))
                )
            }
        }
        return body
    }
}


