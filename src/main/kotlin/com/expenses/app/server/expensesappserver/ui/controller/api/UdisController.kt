package com.expenses.app.server.expensesappserver.ui.controller.api

import com.expenses.app.server.expensesappserver.repository.UdiRepository
import com.expenses.app.server.expensesappserver.ui.database.entities.RetirementRecord
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import javax.servlet.http.HttpServletRequest

@RestController
class UdisController(
    private val udiRepository: UdiRepository
) {

    @Autowired
    private lateinit var mapper: ObjectMapper

    @GetMapping("/today-udi", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getUdiForToday() {
    }

    @GetMapping("/get-all-udis", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getAllUdi() {

    }

    @PostMapping("/insert-udi")
    fun insertUdi(request: HttpServletRequest, @RequestBody body: String) {
        mapper.registerKotlinModule()
        val data = mapper.readValue<RetirementRecord>(body, RetirementRecord::class.java)
        if (data != null) {
            udiRepository.insert(data)
        }
    }
}