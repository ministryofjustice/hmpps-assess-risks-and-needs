package uk.gov.justice.digital.hmpps.assessrisksandneeds.integration

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.SqlConfig
import org.springframework.test.context.jdbc.SqlGroup
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.Source
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.SupplementaryRiskDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.UserType
import java.time.LocalDateTime
import java.util.UUID

@AutoConfigureWebTestClient
@DisplayName("Supplementary Risk Tests")
@SqlGroup(
  Sql(
    scripts = ["classpath:supplementaryrisk/before-test.sql"],
    config = SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED)
  ),
  Sql(
    scripts = ["classpath:supplementaryrisk/after-test.sql"],
    config = SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED),
    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
  )
)
class SupplementaryRiskControllerTest : IntegrationTestBase() {

  private val supplementaryRiskUuid = "2e020e78-a81c-407f-bc78-e5f284e237e5"
  private val crn = "X123456"
  private val sourceType = Source.INTERVENTION_REFERRAL
  private val sourceId = "7e020e78-a81c-407f-bc78-e5f284e237e9"

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
    }

    @Test
    fun `access forbidden when no read scope`() {
      webTestClient.get().uri("/risks/supplementary/$supplementaryRiskUuid")
        .headers(setAuthorisation(roles = listOf("ROLE_RISK_SUMMARY"), scopes = listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `access allowed when role and scope supplied`() {
      webTestClient.get().uri("/risks/supplementary/$supplementaryRiskUuid")
        .headers(setAuthorisation(roles = listOf("ROLE_RISK_SUMMARY"), scopes = listOf("read")))
        .exchange()
        .expectStatus().isOk
        .expectBody<SupplementaryRiskDto>()
        .consumeWith {
          Assertions.assertThat(it.responseBody).isEqualTo(
            SupplementaryRiskDto(
              UUID.fromString(supplementaryRiskUuid),
              Source.INTERVENTION_REFERRAL,
              "182987872",
              "",
              "Gary cooper",
              UserType.DELIUS,
              LocalDateTime.of(2019, 11, 14, 9, 0),
              "risk for children"
            )
          )
        }
    }
  }

  @Nested
  @DisplayName("Get by CRN Security")
  inner class GetByCrnSecurity {

    @Test
    fun `access forbidden when no authority`() {
      webTestClient.get().uri("/risks/supplementary/crn/$crn")
        .header("Content-Type", "application/json")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `access forbidden when no role`() {
      webTestClient.get().uri("/risks/supplementary/crn/$crn")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `access forbidden when no read scope`() {
      webTestClient.get().uri("/risks/supplementary/crn/$crn")
        .headers(setAuthorisation(roles = listOf("ROLE_RISK_SUMMARY"), scopes = listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `get all risks by crn`() {
      webTestClient.get().uri("/risks/supplementary/crn/$crn")
        .headers(setAuthorisation(roles = listOf("ROLE_RISK_SUMMARY"), scopes = listOf("read")))
        .exchange()
        .expectStatus().isOk
        .expectBody<List<SupplementaryRiskDto>>()
        .consumeWith {
          Assertions.assertThat(it.responseBody).contains(
            SupplementaryRiskDto(
              UUID.fromString("5e020e78-a81c-407f-bc78-e5f284e237e5"),
              Source.INTERVENTION_REFERRAL,
              "7e020e78-a81c-407f-bc78-e5f284e237e5",
              "X123456",
              "Gary C",
              UserType.INTERVENTIONS_PROVIDER,
              LocalDateTime.of(2019, 11, 14, 9, 6),
              "risk to self"
            ),
            SupplementaryRiskDto(
              UUID.fromString("6e020e78-a81c-407f-bc78-e5f284e237e5"),
              Source.INTERVENTION_REFERRAL,
              "7e020e78-a81c-407f-bc78-e5f284e237e5",
              "X123456",
              "Gary C",
              UserType.INTERVENTIONS_PROVIDER,
              LocalDateTime.of(2019, 11, 14, 9, 6),
              "risk to self"
            )
          )
        }
    }
  }

  @Nested
  @DisplayName("Get by Source Risk ID Security")
  inner class GetBySourceSecurity {

    @Test
    fun `access forbidden when no authority`() {
      webTestClient.get().uri("/risks/supplementary/$sourceType/$sourceId")
        .header("Content-Type", "application/json")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `access forbidden when no role`() {
      webTestClient.get().uri("/risks/supplementary/$sourceType/$sourceId")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `access forbidden when no read scope`() {
      webTestClient.get().uri("/risks/supplementary/$sourceType/$sourceId")
        .headers(setAuthorisation(roles = listOf("ROLE_RISK_SUMMARY"), scopes = listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `get by source type and source Id`() {
      webTestClient.get().uri("/risks/supplementary/$sourceType/$sourceId")
        .headers(setAuthorisation(roles = listOf("ROLE_RISK_SUMMARY"), scopes = listOf("read")))
        .exchange()
        .expectBody<SupplementaryRiskDto>()
        .consumeWith {
          Assertions.assertThat(it.responseBody).isEqualTo(
            SupplementaryRiskDto(
              UUID.fromString("6e020e78-a81c-407f-bc78-e5f284e237e5"),
              Source.INTERVENTION_REFERRAL,
              "7e020e78-a81c-407f-bc78-e5f284e237e9",
              "X123456",
              "Gary C",
              UserType.INTERVENTIONS_PROVIDER,
              LocalDateTime.of(2019, 11, 14, 9, 6),
              "risk to self"
            )
          )
        }
    }
  }

  @Nested
  @DisplayName("Craete new supplementary risk")
  inner class PostNewSupplementaryRisk {

    private val requestBody = SupplementaryRiskDto(source = Source.INTERVENTION_REFERRAL, sourceId = "1234", crn = crn, riskSummaryComments = "Comments")

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
    }

    @Test
    fun `access forbidden when no read scope`() {
      webTestClient.post().uri("/risks/supplementary")
        .header("Content-Type", "application/json")
        .headers(setAuthorisation(roles = listOf("ROLE_RISK_SUMMARY"), scopes = listOf()))
        .bodyValue(requestBody)
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `access allowed when role and scope supplied`() {
      webTestClient.post().uri("/risks/supplementary")
        .header("Content-Type", "application/json")
        .headers(setAuthorisation(roles = listOf("ROLE_RISK_SUMMARY"), scopes = listOf("write")))
        .bodyValue(requestBody)
        .exchange()
        .expectStatus().is5xxServerError
    }
  }
}
