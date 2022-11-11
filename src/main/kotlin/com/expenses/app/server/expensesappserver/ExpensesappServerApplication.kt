package com.expenses.app.server.expensesappserver

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.jwk.source.RemoteJWKSet
import com.nimbusds.jose.proc.JWSVerificationKeySelector
import com.nimbusds.jose.proc.SecurityContext
import com.nimbusds.jose.util.DefaultResourceRetriever
import com.nimbusds.jose.util.ResourceRetriever
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor
import com.nimbusds.jwt.proc.DefaultJWTProcessor
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import java.net.MalformedURLException
import java.net.URL

@SpringBootApplication
class ExpensesappServerApplication {
    @Bean
    @Throws(MalformedURLException::class)
    fun configurableJWTProcessor(): ConfigurableJWTProcessor<SecurityContext> {
        val resourceRetriever: ResourceRetriever = DefaultResourceRetriever(2000, 2000)
        //TODO check how to hide this from the repo
        val jwkURL = URL("https://cognito-idp.us-east-2.amazonaws.com/us-east-2_R894BlMpq/.well-known/jwks.json")
        val jwkSource = RemoteJWKSet<SecurityContext>(jwkURL, resourceRetriever)
        val jwtProcessor = DefaultJWTProcessor<SecurityContext>()
        val keySelector = JWSVerificationKeySelector(JWSAlgorithm.RS256, jwkSource)
        jwtProcessor.jwsKeySelector = keySelector
        return jwtProcessor
    }
}

fun main(args: Array<String>) {
    runApplication<ExpensesappServerApplication>(*args)
}
