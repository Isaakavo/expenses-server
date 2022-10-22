package com.expenses.app.server.expensesappserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ExpensesappServerApplication

fun main(args: Array<String>) {
    runApplication<ExpensesappServerApplication>(*args)
}
