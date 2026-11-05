package uk.gov.justice.digital.hmpps.assessrisksandneeds.integration.pact

import au.com.dius.pact.provider.junit5.HttpTestTarget
import au.com.dius.pact.provider.junit5.PactVerificationContext
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider
import au.com.dius.pact.provider.junitsupport.Provider
import au.com.dius.pact.provider.junitsupport.loader.PactBroker
import au.com.dius.pact.provider.junitsupport.State
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.TestTemplate
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import uk.gov.justice.digital.hmpps.assessrisksandneeds.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.AuditService
import org.apache.hc.core5.http.HttpRequest
import java.time.Duration

@Provider("hmpps-assess-risks-and-needs")
@PactBroker(url = $$"${pactbroker.url}")
@AutoConfigureWebTestClient(timeout = "360000000")
@DisplayName("Assessment Pact Tests")
class AssessmentControllerPactTest : IntegrationTestBase() {

  @MockkBean
  private lateinit var auditService: AuditService

  private val crn = "X99999"

  @BeforeEach
  fun beforeEach(context: PactVerificationContext) {
    context.target = HttpTestTarget("localhost", port)
  }

  @BeforeEach
  fun setup() {
    every { auditService.sendEvent(any(), any()) } returns Unit
  }

  @TestTemplate
  @ExtendWith(PactVerificationInvocationContextProvider::class)
  fun pactVerificationTestTemplate(context: PactVerificationContext, request: HttpRequest) {
    val token = jwtHelper.createJwt(
      subject = "assess-risks-needs",
      expiryTime = Duration.ofHours(1L),
      roles = listOf("ROLE_PROBATION"),
    )
    request.setHeader("Authorization", "Bearer $token")

    context.verifyInteraction()
  }

  @State("I have identified needs")
  fun iHaveIdentifiedNeeds(): Map<String, String> {
    return mapOf("/needs/crn/${crn}" to "needs/crn/$crn")
  }
}