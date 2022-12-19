package com.expenses.app.server.expensesappserver.security

import com.nimbusds.jose.proc.SecurityContext
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class JwtTokenFilter: OncePerRequestFilter() {

    @Autowired
    var configurableJWTProcessor: ConfigurableJWTProcessor<SecurityContext>? = null

    @Value("\${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private var identityPoolUrl: String? = null

    companion object {
        private const val AUTHORIZATION = "Authorization"
        private const val BEARER = "Bearer "
        private const val USERNAME_FIELD = "username"
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        //TODO investigate more about SecurityContextHolder
        request.getHeader(AUTHORIZATION)?.let { token ->
            val claims = configurableJWTProcessor?.process(getToken(token), null)
            validateToken(claims)
            val username = getUsername(claims)
            if (username != null) {
                val auth = UsernamePasswordAuthenticationToken(username, null)
                auth.details = WebAuthenticationDetailsSource().buildDetails(request)
                SecurityContextHolder.getContext().authentication = auth
            }
        }
        filterChain.doFilter(request, response)
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