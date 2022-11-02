package com.expenses.app.server.expensesappserver.ui.controller.api

import com.expenses.app.server.expensesappserver.common.responses.ApiResponse
import com.expenses.app.server.expensesappserver.common.responses.Status.SUCCESS
import com.expenses.app.server.expensesappserver.common.responses.Status.FAIL
import com.expenses.app.server.expensesappserver.common.responses.ApiResponseCommission
import com.expenses.app.server.expensesappserver.repository.UdiRepository
import com.expenses.app.server.expensesappserver.ui.database.entities.RetirementRecordGet
import com.expenses.app.server.expensesappserver.ui.database.entities.RetirementRecordPost
import com.expenses.app.server.expensesappserver.ui.database.entities.UdiCommissionPost
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
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
        val data = mapper.readValue<RetirementRecordGet>(body, RetirementRecordGet::class.java)
        val dataForUser = udiRepository.getAllUdi(data.userId)
        if (dataForUser != null) {
            return ResponseEntity(
                ApiResponse(
                    SUCCESS,
                    dataForUser
                ), HttpStatus.OK
            )
        }

        return ResponseEntity(
            ApiResponse(FAIL, null),
            HttpStatus.NOT_FOUND
        )
    }

    @GetMapping("/udis/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getUdiById(@PathVariable(value = "id") id: Long): ResponseEntity<ApiResponse> {
        return ResponseEntity(ApiResponse(SUCCESS,udiRepository.getUdiById(id)), HttpStatus.OK)
    }

    @PostMapping("/udis", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun insertUdi(request: HttpServletRequest, @RequestBody body: String): ResponseEntity<ApiResponse> {
        val data = mapper.readValue<RetirementRecordPost>(body, RetirementRecordPost::class.java)
        if (data != null) {
            val retirementRecord = udiRepository.insertUdi(data)
            return ResponseEntity(
                ApiResponse(
                    SUCCESS,
                    retirementRecord
                ), HttpStatus.CREATED
            )
        }
        return ResponseEntity(
            ApiResponse(FAIL, null),
            HttpStatus.BAD_REQUEST
        )
    }

    @PutMapping("/udis/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun updateUdi(@PathVariable(value = "id") id: Long, @RequestBody body: String): ResponseEntity<ApiResponse> {
        val data = mapper.readValue<RetirementRecordPost>(body, RetirementRecordPost::class.java)
        val response = udiRepository.updateUdi(id, data)

        return if(response.userId != null) ResponseEntity(ApiResponse(SUCCESS, response), HttpStatus.OK)
        else ResponseEntity(ApiResponse(FAIL, response), HttpStatus.NOT_FOUND)
    }

    @DeleteMapping("/udis/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun deleteUdi(@PathVariable(value = "id") id: Long): ResponseEntity<ApiResponse>{
        val result = udiRepository.deleteUdi(id)

        return if (result.userId == null) ResponseEntity(ApiResponse(FAIL, result), HttpStatus.NOT_FOUND)
        else ResponseEntity(ApiResponse(SUCCESS, result), HttpStatus.OK)
    }

    @GetMapping("/udis/commission", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getCommission(@RequestBody body: String): ResponseEntity<ApiResponseCommission> {
        val data = mapper.readValue<RetirementRecordGet>(body, RetirementRecordGet::class.java)
        return ResponseEntity(
            ApiResponseCommission(SUCCESS, udiRepository.getCommissionById(data.userId)),
            HttpStatus.OK
        )
    }

    @PostMapping("/udis/commission", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun insertCommission(@RequestBody body: String): ResponseEntity<ApiResponseCommission> {
        val data = mapper.readValue<UdiCommissionPost>(body, UdiCommissionPost::class.java)
        val insertedData = udiRepository.insertUpdateCommission(data)
        return ResponseEntity(ApiResponseCommission(SUCCESS, insertedData), HttpStatus.CREATED)
    }
}