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
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RiskRoshSummaryDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RoshRiskToSelfDto
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
            assessedOn = null
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
            assessedOn = LocalDateTime.of(2021, 6, 21, 15, 55, 4)
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
              risk = ResponseDto.YES,
              previous = ResponseDto.YES,
              current = ResponseDto.YES,
              currentConcernsText = "Suicide and/or Self-harm current concerns"
            ),
            custody = RiskDto(
              risk = ResponseDto.YES,
              previous = ResponseDto.YES,
              previousConcernsText = "Coping in custody / hostel setting previous concerns",
              current = ResponseDto.YES,
              currentConcernsText = "Coping in custody / hostel setting current concerns"
            ),
            hostelSetting = RiskDto(risk = ResponseDto.YES, previous = ResponseDto.NO, current = ResponseDto.NO),
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
              risk = ResponseDto.YES,
              previous = ResponseDto.YES,
              current = ResponseDto.YES,
              currentConcernsText = "Suicide and/or Self-harm current concerns"
            ),
            custody = RiskDto(
              risk = ResponseDto.YES,
              previous = ResponseDto.YES,
              previousConcernsText = "Coping in custody / hostel setting previous concerns",
              current = ResponseDto.YES,
              currentConcernsText = "Coping in custody / hostel setting current concerns"
            ),
            hostelSetting = RiskDto(risk = ResponseDto.YES, previous = ResponseDto.NO, current = ResponseDto.NO),
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
                risk = ResponseDto.YES,
                previous = ResponseDto.YES,
                current = ResponseDto.YES,
                currentConcernsText = "Suicide and/or Self-harm current concerns"
              ),
              custody = RiskDto(
                risk = ResponseDto.YES,
                previous = ResponseDto.YES,
                previousConcernsText = "Coping in custody / hostel setting previous concerns",
                current = ResponseDto.YES,
                currentConcernsText = "Coping in custody / hostel setting current concerns"
              ),
              hostelSetting = RiskDto(risk = ResponseDto.YES, previous = ResponseDto.NO, current = ResponseDto.NO),
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
              assessedOn = null
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
            assessedOn = null
          )
        )
      }
  }
}
