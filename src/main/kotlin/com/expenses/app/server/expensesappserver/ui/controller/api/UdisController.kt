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

    @Autowired
    private lateinit var mapper: ObjectMapper

    @GetMapping("/today-udi", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getUdiForToday() {
    }

    @GetMapping("/udis", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getAllUdi(request: HttpServletRequest): List<ResponseRetirementRecord>? = udiRepository.getAllUdi()


    @GetMapping("/udis/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getUdiById(@PathVariable(value = "id") id: Int): ResponseRetirementRecord = udiRepository.getUdiById(id = id)


    @PostMapping("/udis", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun insertUdi(request: HttpServletRequest, @RequestBody body: String): ResponseRetirementRecord {
        val data = mapper.readValue<RetirementRecordPost>(body, RetirementRecordPost::class.java)
        return udiRepository.insertUdi(data)
    }

    @PutMapping("/udis/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun updateUdi(@PathVariable(value = "id") id: Int, @RequestBody body: String): ResponseRetirementRecord {
        val data = mapper.readValue<RetirementRecordPost>(body, RetirementRecordPost::class.java)
        return udiRepository.updateUdi(id, data)
    }

    @DeleteMapping("/udis/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun deleteUdi(@PathVariable(value = "id") id: Int): ResponseRetirementRecord = udiRepository.deleteUdi(id)


    @GetMapping("/udis/commission", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getCommission(): List<UdiBonus> = udiRepository.getCommissions()


    @PostMapping("/udis/commission", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun insertCommission(@RequestBody body: String): UdiBonus {
        val data = mapper.readValue<UdiBonusPost>(body, UdiBonusPost::class.java)
        return udiRepository.insertCommission(data)
    }

    @PutMapping("/udis/commission", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun updateCommission(@RequestBody body: String): UdiBonus {
        val data = mapper.readValue<UdiBonusPost>(body, UdiBonusPost::class.java)
        return udiRepository.updateCommission(data)
    }

    @GetMapping("/udis/global", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getGlobalDetails(@RequestParam(value = "udiValue") udiValue: Double): UdiGlobalDetails = udiRepository.getGlobalDetails(udiValue)
}