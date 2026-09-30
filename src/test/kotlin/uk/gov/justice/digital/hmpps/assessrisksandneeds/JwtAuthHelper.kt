package uk.gov.justice.digital.hmpps.assessrisksandneeds

import io.jsonwebtoken.Jwts
import org.springframework.context.annotation.Bean
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.stereotype.Component
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPublicKey
import java.time.Duration
import java.util.Date
import java.util.UUID

@Component
class JwtAuthHelper {
  private val keyPair: KeyPair

  init {
    val gen = KeyPairGenerator.getInstance("RSA")
    gen.initialize(2048)
    keyPair = gen.generateKeyPair()
  }

  @Bean
  fun jwtDecoder(): JwtDecoder = NimbusJwtDecoder.withPublicKey(keyPair.public as RSAPublicKey).build()

  fun createJwt(
    subject: String,
    scope: List<String>? = listOf(),
    roles: List<String>? = listOf(),
    expiryTime: Duration = Duration.ofHours(1),
    jwtId: String = UUID.randomUUID().toString(),
  ): String = Jwts.builder()
    .id(jwtId)
    .subject(subject)
    .claim("user_name", subject)
    .claim("client_id", "hmpps-assess-risks-and-needs")
    .apply {
      if (!roles.isNullOrEmpty()) claim("authorities", roles)
      if (!scope.isNullOrEmpty()) claim("scope", scope)
    }
    .expiration(Date(System.currentTimeMillis() + expiryTime.toMillis()))
    .signWith(keyPair.private, Jwts.SIG.RS256)
    .compact()
}
