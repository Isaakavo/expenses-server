package com.expenses.app.server.expensesappserver.ui.controller.api

import com.expenses.app.server.expensesappserver.repository.ExpenseRepository
import com.expenses.app.server.expensesappserver.ui.database.entities.expenses.Expenses
import com.expenses.app.server.expensesappserver.ui.database.entities.expenses.ExpensesPost
import com.expenses.app.server.expensesappserver.ui.database.entities.tags.Tags
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api")
class ExpensesController(
    private val expensesRepository: ExpenseRepository
) {
    @Autowired
    private lateinit var mapper: ObjectMapper

    @GetMapping("/expenses", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getAllExpenses(): List<Expenses> = expensesRepository.getAllExpenses()

    @PostMapping("/expenses", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun insertExpense(@RequestBody body: String) {
        val data = mapper.readValue<ExpensesPost>(body, ExpensesPost::class.java)
        expensesRepository.insertExpense(data)
    }

    @GetMapping("/tags", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getAllTags(): List<Tags> = expensesRepository.getAllTags()
}