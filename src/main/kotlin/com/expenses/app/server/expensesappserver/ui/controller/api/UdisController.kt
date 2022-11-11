package com.expenses.app.server.expensesappserver.ui.controller.api

import com.expenses.app.server.expensesappserver.repository.UdiRepository
import com.expenses.app.server.expensesappserver.ui.database.entities.*
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/api")
class UdisController(
    private val udiRepository: UdiRepository
) {

    //TODO check if I can use the bearer token to extract user id instead of passing in the request
    @Autowired
    private lateinit var mapper: ObjectMapper

    @GetMapping("/today-udi", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getUdiForToday() {
    }

    @GetMapping("/udis", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getAllUdi(request: HttpServletRequest): List<ResponseRetirementRecord>? {
        //val data = mapper.readValue<RetirementRecordGet>(body, RetirementRecordGet::class.java)
        return udiRepository.getAllUdi()
    }

    @GetMapping("/udis/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getUdiById(@PathVariable(value = "id") id: Long, @RequestBody body: String): ResponseRetirementRecord {
        val userId = mapper.readValue(body, RetirementRecordGet::class.java)
        return udiRepository.getUdiById(id = id, userId = userId.userId)
    }

    @PostMapping("/udis", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun insertUdi(request: HttpServletRequest, @RequestBody body: String): ResponseRetirementRecord {
        val data = mapper.readValue<RetirementRecordPost>(body, RetirementRecordPost::class.java)
        return udiRepository.insertUdi(data)
    }

    @PutMapping("/udis/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun updateUdi(@PathVariable(value = "id") id: Long, @RequestBody body: String): ResponseRetirementRecord {
        val data = mapper.readValue<RetirementRecordPost>(body, RetirementRecordPost::class.java)
        return udiRepository.updateUdi(id, data)
    }

    @DeleteMapping("/udis/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun deleteUdi(@PathVariable(value = "id") id: Long): ResponseRetirementRecord {
        return udiRepository.deleteUdi(id)
    }

    @GetMapping("/udis/commission", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getCommission(@RequestBody body: String): UdiCommission {
        val data = mapper.readValue<RetirementRecordGet>(body, RetirementRecordGet::class.java)
        return udiRepository.validatedCommissionById(data.userId)
    }

    @PostMapping("/udis/commission", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun insertCommission(@RequestBody body: String): UdiCommission {
        val data = mapper.readValue<UdiCommissionPost>(body, UdiCommissionPost::class.java)
        return udiRepository.updateCommission(data)
    }

    @PutMapping("/udis/commission", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun updateCommission(@RequestBody body: String): UdiCommission {
        val data = mapper.readValue<UdiCommissionPost>(body, UdiCommissionPost::class.java)
        return udiRepository.updateCommission(data)
    }
}