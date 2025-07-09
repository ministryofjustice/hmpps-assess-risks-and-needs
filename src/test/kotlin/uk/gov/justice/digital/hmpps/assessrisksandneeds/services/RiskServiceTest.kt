package uk.gov.justice.digital.hmpps.assessrisksandneeds.services

import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.MDC
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AllRoshRiskDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.CaseAccess
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.PersonIdentifier
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RiskRoshSummaryDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.config.RequestData
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.CommunityApiRestClient
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.OasysApiRestClient

@ExtendWith(MockKExtension::class)
@DisplayName("Risk Service Tests")
class RiskServiceTest {

  private val oasysApiClient: OasysApiRestClient = mockk()
  private val auditService: AuditService = mockk()
  private val communityClient: CommunityApiRestClient = mockk()
  private val riskService = RiskService(oasysApiClient, communityClient, auditService)

  @BeforeEach
  fun setup() {
    MDC.put(RequestData.USER_NAME_HEADER, "User name")
    every { auditService.sendEvent(any(), any()) } returns Unit
    every { communityClient.verifyUserAccess(any(), any()) } answers {
      CaseAccess(
        it.invocation.args[0] as String,
        userExcluded = false,
        userRestricted = false,
        null,
        null,
      )
    }
  }

  @Test
  fun `sends an audit event when getting fulltext RoSH risk`() {
    val crn = "CRN123"

    every {
      oasysApiClient.getRoshDetailForLatestCompletedAssessment(PersonIdentifier(PersonIdentifier.Type.CRN, crn), 55)
    } returns AllRoshRiskDto.empty

    riskService.getFulltextRoshRisksByCrn(crn)
    verify(exactly = 1) { auditService.sendEvent(EventType.ACCESSED_ROSH_RISKS_FULLTEXT, mapOf("crn" to crn)) }
  }

  @Test
  fun `sends an audit event when getting RoSH risk summary`() {
    val crn = "CRN123"

    every {
      oasysApiClient.getRoshSummary(PersonIdentifier(PersonIdentifier.Type.CRN, crn), 55)
    } returns RiskRoshSummaryDto()

    riskService.getRoshRiskSummaryByCrn(crn)
    verify(exactly = 1) { auditService.sendEvent(EventType.ACCESSED_ROSH_RISKS_SUMMARY, mapOf("crn" to crn)) }
  }
}
