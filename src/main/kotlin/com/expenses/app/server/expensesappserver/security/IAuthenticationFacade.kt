package com.expenses.app.server.expensesappserver.security

import org.springframework.security.core.Authentication

interface IAuthenticationFacade {
    fun getAuthentication(): Authentication
    fun userId(): String
}