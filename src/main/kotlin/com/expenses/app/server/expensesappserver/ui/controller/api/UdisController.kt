package com.expenses.app.server.expensesappserver.ui.controller.api

import com.expenses.app.server.expensesappserver.common.responses.ApiResponse
import com.expenses.app.server.expensesappserver.common.responses.ApiResponse.Status.SUCCESS
import com.expenses.app.server.expensesappserver.common.responses.ApiResponse.Status.FAIL
import com.expenses.app.server.expensesappserver.repository.UdiRepository
import com.expenses.app.server.expensesappserver.ui.database.entities.RetirementRecord
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/api")
class UdisController(
    private val udiRepository: UdiRepository
) {

    @Autowired
    private lateinit var mapper: ObjectMapper

    @GetMapping("/today-udi", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getUdiForToday() {
    }

    @GetMapping("/udis", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getAllUdi(request: HttpServletRequest, @RequestBody body: String): ResponseEntity<ApiResponse> {
        val data = mapper.readValue<RetirementRecord>(body, RetirementRecord::class.java)
        val dataForUser = udiRepository.getAllUdi(data.userId)
        if (dataForUser != null) {
            return ResponseEntity(
                ApiResponse(
                    SUCCESS,
                    "Result for userId ${data.userId}",
                    dataForUser
                ), HttpStatus.OK
            )
        }
        return ResponseEntity(
            ApiResponse(FAIL, "No data for this user", null),
            HttpStatus.NOT_FOUND
        )
    }

    @PostMapping("/udis", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun insertUdi(request: HttpServletRequest, @RequestBody body: String): ResponseEntity<ApiResponse> {
        val data = mapper.readValue<RetirementRecord>(body, RetirementRecord::class.java)
        if (data != null) {
            val retirementRecord = udiRepository.insert(data)
            return ResponseEntity(
                ApiResponse(
                    SUCCESS,
                    "Element inserted with id ${retirementRecord?.obj?.get(0)?.retirementRecord?.id?.value}",
                    retirementRecord
                ), HttpStatus.CREATED
            )
        }
        return ResponseEntity(
            ApiResponse(FAIL, "Couldn't insert data", null),
            HttpStatus.BAD_REQUEST
        )
    }
}