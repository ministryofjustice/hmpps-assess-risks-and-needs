package uk.gov.justice.digital.hmpps.assessrisksandneeds.integration

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.Source
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.SupplementaryRiskDto
import java.util.UUID

@AutoConfigureWebTestClient
@DisplayName("Supplementary Risk Tests")
class SupplementaryRisk : IntegrationTestBase() {

  private val supplementaryRiskUuid = UUID.randomUUID()
  private val crn = "X123456"
  private val sourceType = Source.INTERVENTION_REFERRAL
  private val sourceId = UUID.randomUUID()

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
        .expectStatus().is5xxServerError
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
    fun `access allowed when role and scope supplied`() {
      webTestClient.get().uri("/risks/supplementary/crn/$crn")
        .headers(setAuthorisation(roles = listOf("ROLE_RISK_SUMMARY"), scopes = listOf("read")))
        .exchange()
        .expectStatus().is5xxServerError
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
    fun `access allowed when role and scope supplied`() {
      webTestClient.get().uri("/risks/supplementary/$sourceType/$sourceId")
        .headers(setAuthorisation(roles = listOf("ROLE_RISK_SUMMARY"), scopes = listOf("read")))
        .exchange()
        .expectStatus().is5xxServerError
    }
  }

  @Nested
  @DisplayName("Craete new supplementary risk")
  inner class PostNewSupplementaryRisk {

    private val requestBody = SupplementaryRiskDto(source = Source.INTERVENTION_REFERRAL, sourceId = "1234", crn = crn)

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
