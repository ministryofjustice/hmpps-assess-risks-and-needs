package uk.gov.justice.digital.hmpps.assessrisksandneeds.integration

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.assessrisksandneeds.HmppsAssessRisksAndNeeds
import uk.gov.justice.digital.hmpps.assessrisksandneeds.JwtAuthHelper
import uk.gov.justice.digital.hmpps.assessrisksandneeds.config.TestClockConfiguration
import java.time.Duration

@ContextConfiguration
@SpringBootTest(
  classes = [HmppsAssessRisksAndNeeds::class, TestClockConfiguration::class],
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
@ActiveProfiles("test")
abstract class IntegrationTestBase {
  @Autowired
  lateinit var webTestClient: WebTestClient

  @Autowired
  internal lateinit var jwtHelper: JwtAuthHelper

  init {
    SecurityContextHolder.getContext().authentication = TestingAuthenticationToken("user", "pw")
    // Resolves an issue where Wiremock keeps previous sockets open from other tests causing connection resets
    System.setProperty("http.keepAlive", "false")
  }

  internal fun setAuthorisation(user: String = "assess-risks-needs", roles: List<String> = listOf()): (HttpHeaders) -> Unit {
    val token = jwtHelper.createJwt(
      subject = user,
      expiryTime = Duration.ofHours(1L),
      roles = roles,
    )
    return { it.set(HttpHeaders.AUTHORIZATION, "Bearer $token") }
  }
}
