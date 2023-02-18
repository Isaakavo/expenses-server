package com.expenses.app.server.expensesappserver.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource


@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
class SecurityConfig {

    @Bean
    fun authenticationManager(authenticationConfiguration: AuthenticationConfiguration): AuthenticationManager {
        return authenticationConfiguration.authenticationManager
    }


    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors()
            .and()
            .authorizeRequests { authorize ->
                authorize
                    .anyRequest().authenticated()
            }
            .oauth2ResourceServer { oauth2 ->
                oauth2
                    .jwt { jwt ->
                        jwt.jwtAuthenticationConverter(grantedAuthoritiesExtractor())
                    }
            }

        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOrigins = listOf("http://localhost:3000")
        configuration.allowedMethods = listOf("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS")
        configuration.allowedHeaders = listOf("Origin", "Content-Type", "X-Auth-Token", "Authorization")
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }

    private fun grantedAuthoritiesExtractor(): JwtAuthenticationConverter {
        val jwtAuthenticationConverter = JwtAuthenticationConverter()

        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter { jwt ->
            val list: List<String> = jwt.claims.getOrDefault("cognito:groups", emptyList<String>()) as List<String>

            return@setJwtGrantedAuthoritiesConverter list
                .map { obj: Any -> obj.toString() }
                .map { role -> SimpleGrantedAuthority(role) }
        }

        return jwtAuthenticationConverter
    }
}