package uk.gov.justice.digital.hmpps.assessrisksandneeds.integration

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.assessrisksandneeds.JwtAuthHelper
import uk.gov.justice.digital.hmpps.assessrisksandneeds.testutils.CommunityApiMockServer
import uk.gov.justice.digital.hmpps.assessrisksandneeds.testutils.OasysApiMockServer
import java.time.Duration

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test", "oasys-rsr")
abstract class IntegrationTestBase {

  @Suppress("SpringJavaInjectionPointsAutowiringInspection")
  @Autowired
  lateinit var webTestClient: WebTestClient

  @Autowired
  internal lateinit var jwtHelper: JwtAuthHelper

  companion object {
    internal val communityApiMockServer = CommunityApiMockServer()
    internal val oasysApiMockServer = OasysApiMockServer()

    @BeforeAll
    @JvmStatic
    fun startMocks() {
      communityApiMockServer.start()
      communityApiMockServer.stubGetUserAccess()

      oasysApiMockServer.start()
      oasysApiMockServer.stubGetAssessmentOffenceByCrn()
      oasysApiMockServer.stubGetAssessmentTimeline()
      oasysApiMockServer.stubGetRiskManagementPlansByCrn()
      oasysApiMockServer.stubGetRiskPredictorScores()
      oasysApiMockServer.stubGetTierSections()
      oasysApiMockServer.stubGetRoshRisksByCrn()
      oasysApiMockServer.stubGetSummaryIndicators()
    }

    @AfterAll
    @JvmStatic
    fun stopMocks() {
      communityApiMockServer.stop()
      oasysApiMockServer.stop()
    }
  }
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
