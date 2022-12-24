package com.expenses.app.server.expensesappserver.utils

import org.jetbrains.exposed.sql.SqlLogger
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.statements.StatementContext
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory


object SafeSqlLogger : SqlLogger {

    private val logger: Logger = LoggerFactory.getLogger(SafeSqlLogger::class.java)

    override fun log(context: StatementContext, transaction: Transaction) {
        logger.debug(context.sql(TransactionManager.current()))
    }
}

fun <T> loggedTransaction(statement: Transaction.() -> T): T = transaction {
    addLogger(SafeSqlLogger)
    statement()
}