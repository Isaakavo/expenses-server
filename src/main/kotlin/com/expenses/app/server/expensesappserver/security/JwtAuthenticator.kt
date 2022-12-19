package com.expenses.app.server.expensesappserver.security

import com.nimbusds.jwt.JWTClaimsSet
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority

//TODO check how to implement this to get the username
class JwtAuthenticator(
    authorities: Collection<GrantedAuthority?>?, principal: Any?, claims: JWTClaimsSet?
): AbstractAuthenticationToken(authorities) {

    override fun getCredentials(): Any {
        return emptyList<String>()
    }

    override fun getPrincipal(): Any {
        return emptyList<String>()
    }

}