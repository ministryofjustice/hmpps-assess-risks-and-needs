package uk.gov.justice.digital.hmpps.assessrisksandneeds.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AllRoshRiskDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ErrorResponse
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.OtherRoshRisksDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ResponseDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RiskDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RiskLevel
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RiskManagementPlansDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RiskRoshSummaryDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RoshRiskToSelfDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.ApiErrorResponse
import java.time.LocalDateTime

@AutoConfigureWebTestClient
@DisplayName("Risk Tests")
class RiskControllerTest : IntegrationTestBase() {

  private val crn = "X123456"

  @Test
  fun `get risk summary by crn for external provider`() {
    webTestClient.get().uri("/risks/crn/$crn/summary")
      .headers(setAuthorisation(roles = listOf("ROLE_CRS_PROVIDER")))
      .exchange()
      .expectStatus().isOk
      .expectBody<RiskRoshSummaryDto>()
      .consumeWith {
        assertThat(it.responseBody).isEqualTo(
          RiskRoshSummaryDto(
            riskInCommunity = mapOf(
              RiskLevel.LOW to listOf("Children", "Known Adult"),
              RiskLevel.MEDIUM to listOf("Public"),
              RiskLevel.HIGH to listOf("Staff")
            ),
            assessedOn = null,
            overallRiskLevel = RiskLevel.VERY_HIGH
          )
        )
      }
  }

  @Test
  fun `get risk summary by crn for probation practitioner`() {
    webTestClient.get().uri("/risks/crn/$crn/summary")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isOk
      .expectBody<RiskRoshSummaryDto>()
      .consumeWith {
        assertThat(it.responseBody).isEqualTo(
          RiskRoshSummaryDto(
            "whoisAtRisk",
            "natureOfRisk",
            "riskImminence",
            "riskIncreaseFactors",
            "riskMitigationFactors",
            mapOf(
              RiskLevel.LOW to listOf("Children", "Known Adult"),
              RiskLevel.MEDIUM to listOf("Public"),
              RiskLevel.HIGH to listOf("Staff")
            ),
            mapOf(
              RiskLevel.LOW to listOf("Children", "Public", "Known Adult"),
              RiskLevel.HIGH to listOf("Prisoners"),
              RiskLevel.VERY_HIGH to listOf("Staff")
            ),
            assessedOn = LocalDateTime.of(2021, 6, 21, 15, 55, 4),
            RiskLevel.VERY_HIGH
          )
        )
      }
  }

  @Test
  fun `get risk to self by crn for external provider`() {
    webTestClient.get().uri("/risks/crn/$crn/self")
      .headers(setAuthorisation(roles = listOf("ROLE_CRS_PROVIDER")))
      .exchange()
      .expectStatus().isOk
      .expectBody<RoshRiskToSelfDto>()
      .consumeWith {
        assertThat(it.responseBody).isEqualTo(
          RoshRiskToSelfDto(
            suicide = RiskDto(
              risk = ResponseDto.NO,
              previous = ResponseDto.YES,
              current = ResponseDto.YES,
              currentConcernsText = "Suicide and/or Self-harm current concerns"
            ),
            selfHarm = RiskDto(
              risk = ResponseDto.DK
            ),
            custody = RiskDto(
              risk = ResponseDto.YES,
              previous = ResponseDto.YES,
              previousConcernsText = "Coping in custody / hostel setting previous concerns",
              current = ResponseDto.NA
            ),
            hostelSetting = RiskDto(risk = ResponseDto.YES, previous = ResponseDto.DK, current = ResponseDto.NO),
            vulnerability = RiskDto(
              risk = ResponseDto.YES,
              previous = ResponseDto.YES,
              previousConcernsText = "Vulnerability previous concerns free text",
              current = ResponseDto.YES,
              currentConcernsText = "Vulnerability current concerns free text"
            ),
            assessedOn = LocalDateTime.of(2021, 6, 21, 15, 55, 4)
          )
        )
      }
  }

  @Test
  fun `get risk to self by crn for probation practitioner`() {
    webTestClient.get().uri("/risks/crn/$crn/self")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isOk
      .expectBody<RoshRiskToSelfDto>()
      .consumeWith {
        assertThat(it.responseBody).isEqualTo(
          RoshRiskToSelfDto(
            suicide = RiskDto(
              risk = ResponseDto.NO,
              previous = ResponseDto.YES,
              current = ResponseDto.YES,
              currentConcernsText = "Suicide and/or Self-harm current concerns"
            ),
            selfHarm = RiskDto(
              risk = ResponseDto.DK
            ),
            custody = RiskDto(
              risk = ResponseDto.YES,
              previous = ResponseDto.YES,
              previousConcernsText = "Coping in custody / hostel setting previous concerns",
              current = ResponseDto.NA
            ),
            hostelSetting = RiskDto(risk = ResponseDto.YES, previous = ResponseDto.DK, current = ResponseDto.NO),
            vulnerability = RiskDto(
              risk = ResponseDto.YES,
              previous = ResponseDto.YES,
              previousConcernsText = "Vulnerability previous concerns free text",
              current = ResponseDto.YES,
              currentConcernsText = "Vulnerability current concerns free text"
            ),
            assessedOn = LocalDateTime.of(2021, 6, 21, 15, 55, 4)
          )
        )
      }
  }

  @Test
  fun `get risk for unknown crn returns not found`() {
    webTestClient.get().uri("/risks/crn/RANDOMCRN")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isNotFound
      .expectBody<ErrorResponse>()
      .consumeWith {
        assertThat(it.responseBody).isEqualTo(
          ErrorResponse(
            status = 404,
            developerMessage = "Latest COMPLETE with types [LAYER_1, LAYER_3] type not found for crn, RANDOMCRN"
          )
        )
      }
  }

  @Test
  fun `get other risks by crn for external provider`() {
    webTestClient.get().uri("/risks/crn/$crn/other")
      .headers(setAuthorisation(roles = listOf("ROLE_CRS_PROVIDER")))
      .exchange()
      .expectStatus().isOk
      .expectBody<OtherRoshRisksDto>()
      .consumeWith {
        assertThat(it.responseBody).isEqualTo(
          OtherRoshRisksDto(
            null,
            null,
            null,
            null,
            null
          )
        )
      }
  }

  @Test
  fun `get other risk by crn for probation practitioner`() {
    webTestClient.get().uri("/risks/crn/$crn/other")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isOk
      .expectBody<OtherRoshRisksDto>()
      .consumeWith {
        assertThat(it.responseBody).isEqualTo(
          OtherRoshRisksDto(
            escapeOrAbscond = ResponseDto.YES,
            controlIssuesDisruptiveBehaviour = ResponseDto.YES,
            breachOfTrust = ResponseDto.DK,
            riskToOtherPrisoners = ResponseDto.YES,
            assessedOn = LocalDateTime.of(2021, 6, 21, 15, 55, 4)
          )
        )
      }
  }

  @Test
  fun `get all risks by crn for external provider`() {
    webTestClient.get().uri("/risks/crn/$crn")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isOk
      .expectBody<AllRoshRiskDto>()
      .consumeWith {
        assertThat(it.responseBody).isEqualTo(
          AllRoshRiskDto(
            RoshRiskToSelfDto(
              suicide = RiskDto(
                risk = ResponseDto.NO,
                previous = ResponseDto.YES,
                current = ResponseDto.YES,
                currentConcernsText = "Suicide and/or Self-harm current concerns"
              ),
              selfHarm = RiskDto(
                risk = ResponseDto.DK
              ),
              custody = RiskDto(
                risk = ResponseDto.YES,
                previous = ResponseDto.YES,
                previousConcernsText = "Coping in custody / hostel setting previous concerns",
                current = ResponseDto.NA
              ),
              hostelSetting = RiskDto(
                risk = ResponseDto.YES,
                previous = ResponseDto.DK,
                current = ResponseDto.NO
              ),
              vulnerability = RiskDto(
                risk = ResponseDto.YES,
                previous = ResponseDto.YES,
                previousConcernsText = "Vulnerability previous concerns free text",
                current = ResponseDto.YES,
                currentConcernsText = "Vulnerability current concerns free text"
              ),
              assessedOn = null
            ),
            OtherRoshRisksDto(
              ResponseDto.YES,
              ResponseDto.YES,
              ResponseDto.DK,
              ResponseDto.YES,
              assessedOn = null
            ),
            RiskRoshSummaryDto(
              "whoisAtRisk",
              "natureOfRisk",
              "riskImminence",
              "riskIncreaseFactors",
              "riskMitigationFactors",
              mapOf(
                RiskLevel.LOW to listOf("Children", "Known Adult"),
                RiskLevel.MEDIUM to listOf("Public"),
                RiskLevel.HIGH to listOf("Staff")
              ),
              mapOf(
                RiskLevel.LOW to listOf("Children", "Public", "Known Adult"),
                RiskLevel.HIGH to listOf("Prisoners"),
                RiskLevel.VERY_HIGH to listOf("Staff")
              ),
              assessedOn = null,
              overallRiskLevel = RiskLevel.VERY_HIGH
            ),
            assessedOn = LocalDateTime.of(2021, 6, 21, 15, 55, 4)
          )
        )
      }
  }

  @Test
  fun `get all risks with fulltext for risk to self by crn for external provider`() {
    webTestClient.get().uri("/risks/crn/$crn/fulltext")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isOk
      .expectBody<AllRoshRiskDto>()
      .consumeWith {
        assertThat(it.responseBody).isEqualTo(
          AllRoshRiskDto(
            RoshRiskToSelfDto(
              suicide = RiskDto(
                risk = ResponseDto.NO,
                previous = ResponseDto.YES,
                current = ResponseDto.YES,
                currentConcernsText = "Suicide and/or Self-harm current concerns"
              ),
              selfHarm = RiskDto(
                risk = ResponseDto.DK,
                currentConcernsText = "Suicide and/or Self-harm current concerns"
              ),
              custody = RiskDto(
                risk = ResponseDto.YES,
                previous = ResponseDto.YES,
                previousConcernsText = "Coping in custody / hostel setting previous concerns",
                current = ResponseDto.NA,
                currentConcernsText = "Coping in custody / hostel setting current concerns"
              ),
              hostelSetting = RiskDto(
                risk = ResponseDto.YES,
                previous = ResponseDto.DK,
                previousConcernsText = "Coping in custody / hostel setting previous concerns",
                current = ResponseDto.NO,
                currentConcernsText = "Coping in custody / hostel setting current concerns"
              ),
              vulnerability = RiskDto(
                risk = ResponseDto.YES,
                previous = ResponseDto.YES,
                previousConcernsText = "Vulnerability previous concerns free text",
                current = ResponseDto.YES,
                currentConcernsText = "Vulnerability current concerns free text"
              ),
              assessedOn = null
            ),
            OtherRoshRisksDto(
              ResponseDto.YES,
              ResponseDto.YES,
              ResponseDto.DK,
              ResponseDto.YES,
              assessedOn = null
            ),
            RiskRoshSummaryDto(
              "whoisAtRisk",
              "natureOfRisk",
              "riskImminence",
              "riskIncreaseFactors",
              "riskMitigationFactors",
              mapOf(
                RiskLevel.LOW to listOf("Children", "Known Adult"),
                RiskLevel.MEDIUM to listOf("Public"),
                RiskLevel.HIGH to listOf("Staff")
              ),
              mapOf(
                RiskLevel.LOW to listOf("Children", "Public", "Known Adult"),
                RiskLevel.HIGH to listOf("Prisoners"),
                RiskLevel.VERY_HIGH to listOf("Staff")
              ),
              assessedOn = null,
              overallRiskLevel = RiskLevel.VERY_HIGH
            ),
            assessedOn = LocalDateTime.of(2021, 6, 21, 15, 55, 4)
          )
        )
      }
  }

  @Test
  fun `allow null rosh scores`() {
    val crn = "X234567"
    webTestClient.get().uri("/risks/crn/$crn")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isOk
      .expectBody<AllRoshRiskDto>()
      .consumeWith {
        assertThat(it.responseBody.summary).isEqualTo(
          RiskRoshSummaryDto(
            "whoisAtRisk",
            "natureOfRisk",
            "riskImminence",
            "riskIncreaseFactors",
            "riskMitigationFactors",
            mapOf(
              RiskLevel.LOW to listOf("Known Adult"),
              RiskLevel.MEDIUM to listOf("Public"),
            ),
            mapOf(
              RiskLevel.LOW to listOf("Public", "Known Adult")
            ),
            assessedOn = null,
            RiskLevel.MEDIUM
          )
        )
      }
  }

  @Test
  fun `get risk management plans by crn`() {
    val riskManagementPlanDetails = webTestClient.get().uri("/risks/crn/$crn/risk-management-plan")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isOk
      .expectBody<RiskManagementPlansDto>()
      .returnResult().responseBody

    assertThat(riskManagementPlanDetails!!.riskManagementPlan).hasSize(5)
    with(riskManagementPlanDetails.riskManagementPlan[0]) {
      assertThat(this.assessmentId).isEqualTo(667025L)
      assertThat(this.initiationDate).isEqualTo(LocalDateTime.of(2020, 3, 26, 12, 38, 57))
      assertThat(this.dateCompleted).isEqualTo(LocalDateTime.of(2020, 3, 26, 12, 47, 17))
      assertThat(this.assessmentStatus).isEqualTo("COMPLETE")
      assertThat(this.assessmentType).isEqualTo("LAYER3")
      assertThat(this.partcompStatus).isNull()
      assertThat(this.keyInformationCurrentSituation).isEqualTo(null)
      assertThat(this.furtherConsiderationsCurrentSituation).isEqualTo(null)
      assertThat(this.supervision).isEqualTo(null)
      assertThat(this.monitoringAndControl).isEqualTo(null)
      assertThat(this.interventionsAndTreatment).isEqualTo(null)
      assertThat(this.victimSafetyPlanning).isEqualTo(null)
      assertThat(this.contingencyPlans).isEqualTo(null)
    }
    with(riskManagementPlanDetails.riskManagementPlan[3]) {
      assertThat(this.assessmentId).isEqualTo(674025L)
      assertThat(this.initiationDate).isEqualTo(LocalDateTime.of(2020, 6, 25, 13, 4, 56))
      assertThat(this.dateCompleted).isEqualTo(LocalDateTime.of(2020, 11, 2, 14, 49, 39))
      assertThat(this.assessmentStatus).isEqualTo("LOCKED_INCOMPLETE")
      assertThat(this.assessmentType).isEqualTo("LAYER3")
      assertThat(this.partcompStatus).isEqualTo("Unsigned")
      assertThat(this.keyInformationCurrentSituation).isEqualTo(null)
      assertThat(this.furtherConsiderationsCurrentSituation).isEqualTo(null)
      assertThat(this.supervision).isEqualTo(null)
      assertThat(this.monitoringAndControl).isEqualTo(null)
      assertThat(this.interventionsAndTreatment).isEqualTo(null)
      assertThat(this.victimSafetyPlanning).isEqualTo(null)
      assertThat(this.contingencyPlans).isEqualTo(null)
    }
    with(riskManagementPlanDetails.riskManagementPlan[4]) {
      assertThat(this.assessmentId).isEqualTo(676026L)
      assertThat(this.initiationDate).isEqualTo(LocalDateTime.of(2020, 11, 2, 14, 50, 2))
      assertThat(this.dateCompleted).isEqualTo(LocalDateTime.of(2020, 11, 5, 10, 56, 37))
      assertThat(this.assessmentStatus).isEqualTo("COMPLETE")
      assertThat(this.assessmentType).isEqualTo("LAYER3")
      assertThat(this.keyInformationCurrentSituation).isEqualTo("Key considerations")
      assertThat(this.furtherConsiderationsCurrentSituation).isEqualTo("Kelvin Brown is currently in the community having received a Adjourned - Other Report on the 01/01/2010 for 12 months\r\rThe end of their sentence is currently unknown. \r\rThey have no areas linked to harm. \r\rKelvin Brown has been assessed as medium risk to the public.\r\rKelvin Brown will have contact with a child on the protection register or in local authority care.\rThey are quite motivated to address offending behaviour.")
      assertThat(this.supervision).isEqualTo(null)
      assertThat(this.monitoringAndControl).isEqualTo("3. Added measures for specific risks. Include here all activity aimed at addressing victim perspective and contact.")
      assertThat(this.interventionsAndTreatment).isEqualTo("5. Additional conditions/requirements to manage the specific risks.")
      assertThat(this.victimSafetyPlanning).isEqualTo("7. Contingency")
      assertThat(this.contingencyPlans).isEqualTo(null)
      assertThat(this.laterWIPAssessmentExists).isEqualTo(false)
      assertThat(this.latestWIPDate).isEqualTo(LocalDateTime.of(2022, 7, 21, 15, 43, 58))
      assertThat(this.laterSignLockAssessmentExists).isEqualTo(false)
      assertThat(this.latestSignLockDate).isNull()
      assertThat(this.laterPartCompUnsignedAssessmentExists).isEqualTo(false)
      assertThat(this.latestPartCompUnsignedDate).isEqualTo(LocalDateTime.of(2022, 5, 31, 10, 37, 5))
      assertThat(this.laterPartCompSignedAssessmentExists).isEqualTo(false)
      assertThat(this.latestPartCompSignedDate).isNull()
      assertThat(this.laterCompleteAssessmentExists).isEqualTo(false)
      assertThat(this.latestCompleteDate).isEqualTo(LocalDateTime.of(2022, 7, 21, 15, 43, 12))
    }
  }

  @Test
  fun `should return forbidden when user cannot access crn`() {
    webTestClient.get().uri("/risks/crn/FORBIDDEN/risk-management-plan")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  fun `should return not found when Delius cannot find crn`() {
    val response = webTestClient.get().uri("/risks/crn/USER_ACCESS_NOT_FOUND/risk-management-plan")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isNotFound
      .expectBody<ApiErrorResponse>()
      .returnResult().responseBody

    assertThat(response.developerMessage).isEqualTo("No such offender for CRN: USER_ACCESS_NOT_FOUND")
  }

  @Test
  fun `should return not found when Delius cannot find user`() {
    val response = webTestClient.get().uri("/risks/crn/$crn/risk-management-plan")
      .headers(setAuthorisation(user = "USER_NOT_FOUND", roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isNotFound
      .expectBody<ApiErrorResponse>()
      .returnResult().responseBody

    assertThat(response.developerMessage).isEqualTo("No such user for username: USER_NOT_FOUND")
  }
}
