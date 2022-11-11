package com.expenses.app.server.expensesappserver.security

import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Component
class AuthenticationFacade: IAuthenticationFacade {
    override fun getAuthentication(): Authentication {
        return SecurityContextHolder.getContext().authentication
    }

    override fun userId(): String {
        return getAuthentication().name
    }
}