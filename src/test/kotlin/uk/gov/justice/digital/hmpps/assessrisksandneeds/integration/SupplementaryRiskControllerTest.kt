package uk.gov.justice.digital.hmpps.assessrisksandneeds.integration

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.http.HttpStatus
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.SqlConfig
import org.springframework.test.context.jdbc.SqlGroup
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.CreateSupplementaryRiskDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ErrorResponse
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RedactedOasysRiskDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.Source
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.SupplementaryRiskDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.AuditService
import java.time.LocalDateTime
import java.util.UUID

@AutoConfigureWebTestClient
@DisplayName("Supplementary Risk Tests")
@SqlGroup(
  Sql(
    scripts = ["classpath:supplementaryrisk/before-test.sql"],
    config = SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED),
  ),
  Sql(
    scripts = ["classpath:supplementaryrisk/after-test.sql"],
    config = SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED),
    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
  ),
)
class SupplementaryRiskControllerTest : IntegrationTestBase() {

  @MockkBean
  private lateinit var auditService: AuditService

  private val supplementaryRiskUuid = "2e020e78-a81c-407f-bc78-e5f284e237e5"
  private val invalidSupplementaryRiskUuid = "2e020e78-a80a-407f-bc78-e5f284e237e5"

  @BeforeEach
  fun setup() {
    every { auditService.sendEvent(any(), any()) } returns Unit
  }

  @Nested
  @DisplayName("Get by Supplementary Risk ID Security")
  inner class GetByIdSecurity {

    @Test
    fun `access forbidden when no authority`() {
      webTestClient.get().uri("/risks/supplementary/$supplementaryRiskUuid")
        .header("Content-Type", "application/json")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `access forbidden when no role`() {
      webTestClient.get().uri("/risks/supplementary/$supplementaryRiskUuid")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus().isForbidden
        .expectBody<ErrorResponse>()
        .consumeWith {
          assertThat(it.responseBody).isEqualTo(
            ErrorResponse(
              status = 403,
              developerMessage = "Access Denied",
            ),
          )
        }
    }

    @Test
    fun `not found when supplementary risk uuid doesn't exists`() {
      webTestClient.get().uri("/risks/supplementary/$invalidSupplementaryRiskUuid")
        .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
        .exchange()
        .expectStatus().isNotFound
        .expectBody<ErrorResponse>()
        .consumeWith {
          assertThat(it.responseBody).isEqualTo(
            ErrorResponse(
              status = 404,
              developerMessage = "Error retrieving Supplementary Risk for supplementaryRiskUuid: 2e020e78-a80a-407f-bc78-e5f284e237e5",
            ),
          )
        }
    }

    @Test
    fun `access allowed when role ROLE_PROBATION and scope supplied`() {
      webTestClient.get().uri("/risks/supplementary/$supplementaryRiskUuid")
        .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
        .exchange()
        .expectStatus().isOk
        .expectBody<SupplementaryRiskDto>()
        .consumeWith {
          assertThat(it.responseBody).isEqualTo(
            SupplementaryRiskDto(
              UUID.fromString(supplementaryRiskUuid),
              Source.INTERVENTION_REFERRAL,
              "182987872",
              "X123458",
              "Gary cooper",
              "delius",
              LocalDateTime.of(2019, 11, 14, 9, 0),
              null,
              "risk for children",
            ),
          )
        }
    }

    @Test
    fun `access allowed when role ROLE_CRS_PROVIDER and scope supplied`() {
      webTestClient.get().uri("/risks/supplementary/$supplementaryRiskUuid")
        .headers(setAuthorisation(roles = listOf("ROLE_CRS_PROVIDER")))
        .exchange()
        .expectStatus().isOk
        .expectBody<SupplementaryRiskDto>()
        .consumeWith {
          assertThat(it.responseBody).isEqualTo(
            SupplementaryRiskDto(
              UUID.fromString(supplementaryRiskUuid),
              Source.INTERVENTION_REFERRAL,
              "182987872",
              "X123458",
              "Gary cooper",
              "delius",
              LocalDateTime.of(2019, 11, 14, 9, 0),
              null,
              "risk for children",
            ),
          )
        }
    }
  }

  @Nested
  @DisplayName("Create new supplementary risk")
  inner class PostNewSupplementaryRisk {

    private val requestBody = CreateSupplementaryRiskDto(
      source = Source.INTERVENTION_REFERRAL,
      sourceId = "8e020e78-a81c-407f-bc78-e5f284e237e8",
      crn = "X123457",
      createdByUserType = "delius",
      createdDate = LocalDateTime.of(2019, 11, 14, 9, 7),
      redactedRisk = null,
      riskSummaryComments = "risk to others",
    )

    @Test
    fun `access forbidden when no authority`() {
      webTestClient.post().uri("/risks/supplementary")
        .header("Content-Type", "application/json")
        .bodyValue(requestBody)
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `access forbidden when no role`() {
      webTestClient.post().uri("/risks/supplementary")
        .header("Content-Type", "application/json")
        .headers(setAuthorisation())
        .bodyValue(requestBody)
        .exchange()
        .expectStatus().isForbidden
        .expectBody<ErrorResponse>()
        .consumeWith {
          assertThat(it.responseBody).isEqualTo(
            ErrorResponse(
              status = 403,
              developerMessage = "Access Denied",
            ),
          )
        }
    }

    @Test
    fun `access allowed when role ROLE_PROBATION and scope supplied`() {
      webTestClient.post().uri("/risks/supplementary")
        .header("Content-Type", "application/json")
        .headers(setAuthorisation(user = "Tom C", roles = listOf("ROLE_PROBATION")))
        .bodyValue(requestBody)
        .exchange()
        .expectBody<SupplementaryRiskDto>()
        .consumeWith {
          assertThat(it.responseBody).isEqualTo(
            SupplementaryRiskDto(
              supplementaryRiskId = it.responseBody?.supplementaryRiskId,
              Source.INTERVENTION_REFERRAL,
              "8e020e78-a81c-407f-bc78-e5f284e237e8",
              "X123457",
              "Tom C",
              "delius",
              LocalDateTime.of(2019, 11, 14, 9, 7),
              null,
              "risk to others",
            ),
          )
        }
    }

    @Test
    fun `unauthorized for role ROLE_CRS_PROVIDER and scope supplied`() {
      webTestClient.post().uri("/risks/supplementary")
        .header("Content-Type", "application/json")
        .headers(setAuthorisation(roles = listOf("ROLE_CRS_PROVIDER")))
        .bodyValue(requestBody)
        .exchange()
        .expectStatus().isForbidden
        .expectBody<ErrorResponse>()
        .consumeWith {
          assertThat(it.responseBody).isEqualTo(
            ErrorResponse(
              status = 403,
              developerMessage = "Access Denied",
            ),
          )
        }
    }

    @Test
    fun `409 returned when record for source already exists`() {
      val requestBody = CreateSupplementaryRiskDto(
        source = Source.INTERVENTION_REFERRAL,
        sourceId = "3e020e78-a81c-407f-bc78-e5f284e237e5",
        crn = "X123457",
        riskSummaryComments = "risk to others",
        createdByUserType = "delius",
      )

      webTestClient.post().uri("/risks/supplementary")
        .header("Content-Type", "application/json")
        .headers(setAuthorisation(user = "Gary C", roles = listOf("ROLE_PROBATION")))
        .bodyValue(requestBody)
        .exchange()
        .expectStatus().isEqualTo(HttpStatus.CONFLICT)
        .expectBody<SupplementaryRiskDto>()
        .consumeWith {
          assertThat(it.responseBody).isEqualTo(
            SupplementaryRiskDto(
              supplementaryRiskId = UUID.fromString("4e020e78-a81c-407f-bc78-e5f284e237e5"),
              source = Source.INTERVENTION_REFERRAL,
              sourceId = "3e020e78-a81c-407f-bc78-e5f284e237e5",
              crn = "X123457",
              createdByUser = "Gary C",
              createdByUserType = "delius",
              createdDate = LocalDateTime.of(2019, 11, 14, 9, 5),
              riskSummaryComments = "risk to self",
              redactedRisk = null,
            ),
          )
        }
    }

    @Test
    fun `create new supplementary risk with redacted data`() {
      val redactedRiskBody = requestBody.copy(
        redactedRisk = RedactedOasysRiskDto(
          riskWho = "Risk to person",
          riskWhen = "When risk is greatest",
          riskNature = "Nature is risk",
          concernsSelfHarm = "Self harm concerns",
          concernsSuicide = "Suicide concerns",
          concernsHostel = "Hostel concerns",
          concernsVulnerability = "Vulnerability concerns",
        ),
      )
      webTestClient.post().uri("/risks/supplementary")
        .header("Content-Type", "application/json")
        .headers(setAuthorisation(user = "Tom C", roles = listOf("ROLE_PROBATION")))
        .bodyValue(redactedRiskBody)
        .exchange()
        .expectBody<SupplementaryRiskDto>()
        .consumeWith {
          assertThat(it.responseBody).isEqualTo(
            SupplementaryRiskDto(
              supplementaryRiskId = it.responseBody?.supplementaryRiskId,
              Source.INTERVENTION_REFERRAL,
              "8e020e78-a81c-407f-bc78-e5f284e237e8",
              "X123457",
              "Tom C",
              "delius",
              LocalDateTime.of(2019, 11, 14, 9, 7),
              redactedRisk = RedactedOasysRiskDto(
                riskWho = "Risk to person",
                riskWhen = "When risk is greatest",
                riskNature = "Nature is risk",
                concernsSelfHarm = "Self harm concerns",
                concernsSuicide = "Suicide concerns",
                concernsHostel = "Hostel concerns",
                concernsVulnerability = "Vulnerability concerns",
              ),
              "risk to others",
            ),
          )
        }
    }
  }
}
