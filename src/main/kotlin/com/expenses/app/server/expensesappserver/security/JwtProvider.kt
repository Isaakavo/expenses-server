package com.expenses.app.server.expensesappserver.security

import com.nimbusds.jose.proc.SecurityContext
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component
import javax.servlet.http.HttpServletRequest

@Component
class JwtProvider {

    @Autowired
    var configurableJWTProcessor: ConfigurableJWTProcessor<SecurityContext>? = null

    //TODO check how to hide this from the repo
    @Value("https://cognito-idp.us-east-2.amazonaws.com/us-east-2_R894BlMpq")
    private var identityPoolUrl: String? = null

    companion object {
        private const val AUTHORIZATION = "Authorization"
        private const val BEARER = "Bearer "
        private const val USERNAME_FIELD = "username"
    }

    //TODO refactor to extract username and check how to implement this correctly
    fun authentication(request: HttpServletRequest): Authentication? {
        request.getHeader(AUTHORIZATION)?.let { token ->
            val claims = configurableJWTProcessor?.process(getToken(token), null)
            validateToken(claims)
            val username = getUsername(claims)
            if (username != null) {
                println(username)
                val authorities = listOf<GrantedAuthority>(SimpleGrantedAuthority("ROLE_USER"))
                return JwtAuthenticator(authorities, null, claims)
            }
        }
        return null
    }

    private fun getUsername(claims: JWTClaimsSet?): String? {
        if (claims != null) {
            return claims.getClaim(USERNAME_FIELD).toString()
        }
        return null
    }

    @Throws(Exception::class)
    private fun validateToken(claims: JWTClaimsSet?) {
        if (claims != null) {
            if (claims.issuer != identityPoolUrl) throw Exception("JWT not valid")
        }
    }

    private fun getToken(token: String): String {
        return if (token.startsWith(BEARER)) token.substring(BEARER.length) else token
    }
}