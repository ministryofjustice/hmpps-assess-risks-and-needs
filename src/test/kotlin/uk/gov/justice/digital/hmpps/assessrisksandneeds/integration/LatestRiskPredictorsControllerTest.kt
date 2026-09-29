package uk.gov.justice.digital.hmpps.assessrisksandneeds.integration

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.http.HttpStatus
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.AuditService

@AutoConfigureWebTestClient(timeout = "360000000")
class LatestRiskPredictorsControllerTest : IntegrationTestBase() {

  @MockkBean
  private lateinit var auditService: AuditService

  @BeforeEach
  fun setup() {
    every { auditService.sendEvent(any(), any()) } returns Unit
  }

  @Test
  fun `should return versioned risk data for valid crn`() {
    val identifierType = "crn"
    val identifierValue = "X123456"

    val response = webTestClient.get()
      .uri("/risks/predictors/all/$identifierType/$identifierValue")
      .header("Content-Type", "application/json")
      .headers(setAuthorisation(user = "assess-risks-needs", roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isEqualTo(HttpStatus.OK)
      .expectBody<String>()
      .returnResult().responseBody

    assertJson(allPredictorsJson, response)
  }

  @Test
  fun `should include standalone assessments when requested`() {
    val identifierType = "crn"
    val identifierValue = "X123456"

    val response = webTestClient.get()
      .uri("/risks/predictors/all/$identifierType/$identifierValue?includeStandaloneAssessments=true")
      .header("Content-Type", "application/json")
      .headers(setAuthorisation(user = "assess-risks-needs", roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isEqualTo(HttpStatus.OK)
      .expectBody<String>()
      .returnResult().responseBody

    assertJson(allPredictorsWithStandaloneJson, response)
  }

  @Test
  fun `should return not found error for invalid crn for versioned risk scores`() {
    webTestClient.get().uri("/risks/predictors/all/crn/X999999")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  fun `should return 400 bad request for invalid identifier type for versioned risk scores`() {
    val identifierType = "INVALID_IDENTIFIER_TYPE"
    val identifierValue = "X234567"
    webTestClient.get().uri("/risks/predictors/all/$identifierType/$identifierValue")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isBadRequest
  }

  @Test
  fun `should return forbidden when user has insufficient privileges to access crn for versioned risk scores`() {
    webTestClient.get().uri("/risks/predictors/all/crn/FORBIDDEN")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  fun `should return not found when Delius cannot find crn for versioned risk scores`() {
    val response = webTestClient.get().uri("/risks/predictors/all/crn/USER_ACCESS_NOT_FOUND")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isNotFound
      .expectBody<String>()
      .returnResult().responseBody

    assertJson("""{"status":404,"developerMessage":"No such offender for CRN: USER_ACCESS_NOT_FOUND"}""", response)
  }

  @Test
  fun `should return legacy risk data for legacy assessment ID`() {
    val id = "1000001"

    val response = webTestClient.get()
      .uri("/assessments/id/$id/risk/predictors/all")
      .header("Content-Type", "application/json")
      .headers(setAuthorisation(user = "assess-risks-needs", roles = listOf("ROLE_ARNS__RISKS__RO")))
      .exchange()
      .expectStatus().isEqualTo(HttpStatus.OK)
      .expectBody<String>()
      .returnResult().responseBody

    assertJson(legacyRiskJson, response)
  }

  @Test
  fun `should return OGRS4 risk data for OGRS4 assessment ID`() {
    val id = "1000002"

    val response = webTestClient.get()
      .uri("/assessments/id/$id/risk/predictors/all")
      .header("Content-Type", "application/json")
      .headers(setAuthorisation(user = "assess-risks-needs", roles = listOf("ROLE_ARNS__RISKS__RO")))
      .exchange()
      .expectStatus().isEqualTo(HttpStatus.OK)
      .expectBody<String>()
      .returnResult().responseBody

    assertJson(ogrs4RiskJson, response)
  }

  @Test
  fun `should return OGRS4 risk data for OGRS4 assessment ID and deserialize rsrScoreLevel`() {
    val id = "1000004"

    val response = webTestClient.get()
      .uri("/assessments/id/$id/risk/predictors/all")
      .header("Content-Type", "application/json")
      .headers(setAuthorisation(user = "assess-risks-needs", roles = listOf("ROLE_ARNS__RISKS__RO")))
      .exchange()
      .expectStatus().isEqualTo(HttpStatus.OK)
      .expectBody<String>()
      .returnResult().responseBody

    assertJson(ogrs4RiskJson, response)
  }

  @Test
  fun `should return 404 when risk data cannot be found for assessment ID`() {
    val id = "1000003"

    webTestClient.get()
      .uri("/assessments/id/$id/risk/predictors/all")
      .header("Content-Type", "application/json")
      .headers(setAuthorisation(user = "assess-risks-needs", roles = listOf("ROLE_ARNS__RISKS__RO")))
      .exchange()
      .expectStatus().isNotFound
  }

  private fun assertJson(expected: String, actual: String?) {
    JSONAssert.assertEquals(expected, checkNotNull(actual), JSONCompareMode.STRICT)
  }

  private val legacyRiskJson = """
    {"outputVersion":"1","output":{"groupReconvictionScore":{"oneYear":3,"twoYears":5,"scoreLevel":"LOW"},"violencePredictorScore":{"ovpStaticWeightedScore":14,"ovpDynamicWeightedScore":3,"ovpTotalWeightedScore":17,"oneYear":4,"twoYears":7,"ovpRisk":"LOW"},"generalPredictorScore":{"ogpStaticWeightedScore":3,"ogpDynamicWeightedScore":7,"ogpTotalWeightedScore":10,"ogp1Year":4,"ogp2Year":8,"ogpRisk":"LOW"},"riskOfSeriousRecidivismScore":{"percentageScore":50.1234,"staticOrDynamic":"DYNAMIC","source":"OASYS","algorithmVersion":"5","scoreLevel":"MEDIUM"},"sexualPredictorScore":{"ospIndecentPercentageScore":2.81,"ospContactPercentageScore":1.07,"ospIndecentScoreLevel":"MEDIUM","ospContactScoreLevel":"MEDIUM"}}}
  """.trimIndent()

  private val ogrs4RiskJson = """
    {"outputVersion":"2","output":{"allReoffendingPredictor":{"staticOrDynamic":"STATIC","score":1.23,"band":"LOW"},"violentReoffendingPredictor":{"staticOrDynamic":"STATIC","score":1.23,"band":"LOW"},"seriousViolentReoffendingPredictor":{"staticOrDynamic":"STATIC","score":1.23,"band":"LOW"},"directContactSexualReoffendingPredictor":{"score":2.81,"band":"MEDIUM"},"indirectImageContactSexualReoffendingPredictor":{"score":1.07,"band":"MEDIUM"},"combinedSeriousReoffendingPredictor":{"algorithmVersion":"6","staticOrDynamic":"STATIC","score":1.23,"band":"LOW"}}}
  """.trimIndent()

  private val allPredictorsJson = """
    [
      {"completedDate":"2022-06-10T18:23:20","status":"COMPLETE","assessmentType":"LAYER3","outputVersion":"1","output":{"groupReconvictionScore":{"oneYear":3,"twoYears":5,"scoreLevel":"LOW"},"violencePredictorScore":{"ovpStaticWeightedScore":14,"ovpDynamicWeightedScore":3,"ovpTotalWeightedScore":17,"oneYear":4,"twoYears":7,"ovpRisk":"LOW"},"generalPredictorScore":{"ogpStaticWeightedScore":3,"ogpDynamicWeightedScore":7,"ogpTotalWeightedScore":10,"ogp1Year":4,"ogp2Year":8,"ogpRisk":"LOW"},"riskOfSeriousRecidivismScore":{"percentageScore":50.1234,"staticOrDynamic":"DYNAMIC","source":"OASYS","algorithmVersion":"5","scoreLevel":"MEDIUM"},"sexualPredictorScore":{"ospIndecentPercentageScore":2.81,"ospContactPercentageScore":1.07,"ospIndecentScoreLevel":"MEDIUM","ospContactScoreLevel":"MEDIUM"}}},
      {"completedDate":"2022-04-27T12:46:39","status":"COMPLETE","assessmentType":"LAYER1","outputVersion":"1","output":{"groupReconvictionScore":{},"violencePredictorScore":{},"generalPredictorScore":{},"riskOfSeriousRecidivismScore":{"percentageScore":0.32,"staticOrDynamic":"STATIC","source":"OASYS","algorithmVersion":"3","scoreLevel":"LOW"},"sexualPredictorScore":{}}},
      {"completedDate":"2022-06-09T15:16:21","status":"COMPLETE","assessmentType":"LAYER3","outputVersion":"1","output":{"groupReconvictionScore":{"oneYear":6,"twoYears":12,"scoreLevel":"LOW"},"violencePredictorScore":{"ovpStaticWeightedScore":22,"ovpDynamicWeightedScore":13,"ovpTotalWeightedScore":35,"oneYear":12,"twoYears":21,"ovpRisk":"LOW"},"generalPredictorScore":{"ogpStaticWeightedScore":7,"ogpDynamicWeightedScore":4,"ogpTotalWeightedScore":11,"ogp1Year":5,"ogp2Year":8,"ogpRisk":"LOW"},"riskOfSeriousRecidivismScore":{"percentageScore":4.12,"staticOrDynamic":"DYNAMIC","source":"OASYS","algorithmVersion":"3","scoreLevel":"MEDIUM"},"sexualPredictorScore":{"ospIndecentPercentageScore":2.81,"ospContactPercentageScore":1.07,"ospIndecentScoreLevel":"MEDIUM","ospContactScoreLevel":"MEDIUM"}}},
      {"completedDate":"2022-06-11T18:23:20","status":"COMPLETE","assessmentType":"LAYER3","outputVersion":"2","output":{"allReoffendingPredictor":{"staticOrDynamic":"DYNAMIC","score":4.56,"band":"MEDIUM"},"violentReoffendingPredictor":{"staticOrDynamic":"DYNAMIC","score":4.56,"band":"MEDIUM"},"seriousViolentReoffendingPredictor":{"staticOrDynamic":"DYNAMIC","score":4.56,"band":"MEDIUM"},"directContactSexualReoffendingPredictor":{"score":2.81,"band":"MEDIUM"},"indirectImageContactSexualReoffendingPredictor":{"score":1.07,"band":"MEDIUM"},"combinedSeriousReoffendingPredictor":{"algorithmVersion":"6","staticOrDynamic":"DYNAMIC","score":50.1234,"band":"MEDIUM"}}},
      {"completedDate":"2022-06-12T18:23:20","status":"COMPLETE","assessmentType":"LAYER3","outputVersion":"2","output":{"allReoffendingPredictor":{"staticOrDynamic":"STATIC","score":1.23,"band":"LOW"},"violentReoffendingPredictor":{"staticOrDynamic":"STATIC","score":1.23,"band":"LOW"},"seriousViolentReoffendingPredictor":{"staticOrDynamic":"STATIC","score":1.23,"band":"LOW"},"directContactSexualReoffendingPredictor":{"score":2.81,"band":"MEDIUM"},"indirectImageContactSexualReoffendingPredictor":{"score":1.07,"band":"MEDIUM"},"combinedSeriousReoffendingPredictor":{"algorithmVersion":"6","staticOrDynamic":"STATIC","score":1.23,"band":"LOW"}}}
    ]
  """.trimIndent()

  private val allPredictorsWithStandaloneJson = """
    [
      {"completedDate":"2022-06-10T18:23:20","status":"COMPLETE","assessmentType":"LAYER3","outputVersion":"1","output":{"groupReconvictionScore":{"oneYear":3,"twoYears":5,"scoreLevel":"LOW"},"violencePredictorScore":{"ovpStaticWeightedScore":14,"ovpDynamicWeightedScore":3,"ovpTotalWeightedScore":17,"oneYear":4,"twoYears":7,"ovpRisk":"LOW"},"generalPredictorScore":{"ogpStaticWeightedScore":3,"ogpDynamicWeightedScore":7,"ogpTotalWeightedScore":10,"ogp1Year":4,"ogp2Year":8,"ogpRisk":"LOW"},"riskOfSeriousRecidivismScore":{"percentageScore":50.1234,"staticOrDynamic":"DYNAMIC","source":"OASYS","algorithmVersion":"5","scoreLevel":"MEDIUM"},"sexualPredictorScore":{"ospIndecentPercentageScore":2.81,"ospContactPercentageScore":1.07,"ospIndecentScoreLevel":"MEDIUM","ospContactScoreLevel":"MEDIUM"}}},
      {"completedDate":"2022-04-27T12:46:39","status":"COMPLETE","assessmentType":"LAYER1","outputVersion":"1","output":{"groupReconvictionScore":{},"violencePredictorScore":{},"generalPredictorScore":{},"riskOfSeriousRecidivismScore":{"percentageScore":0.32,"staticOrDynamic":"STATIC","source":"OASYS","algorithmVersion":"3","scoreLevel":"LOW"},"sexualPredictorScore":{}}},
      {"completedDate":"2022-06-09T15:16:21","status":"COMPLETE","assessmentType":"LAYER3","outputVersion":"1","output":{"groupReconvictionScore":{"oneYear":6,"twoYears":12,"scoreLevel":"LOW"},"violencePredictorScore":{"ovpStaticWeightedScore":22,"ovpDynamicWeightedScore":13,"ovpTotalWeightedScore":35,"oneYear":12,"twoYears":21,"ovpRisk":"LOW"},"generalPredictorScore":{"ogpStaticWeightedScore":7,"ogpDynamicWeightedScore":4,"ogpTotalWeightedScore":11,"ogp1Year":5,"ogp2Year":8,"ogpRisk":"LOW"},"riskOfSeriousRecidivismScore":{"percentageScore":4.12,"staticOrDynamic":"DYNAMIC","source":"OASYS","algorithmVersion":"3","scoreLevel":"MEDIUM"},"sexualPredictorScore":{"ospIndecentPercentageScore":2.81,"ospContactPercentageScore":1.07,"ospIndecentScoreLevel":"MEDIUM","ospContactScoreLevel":"MEDIUM"}}},
      {"completedDate":"2022-06-11T18:23:20","status":"COMPLETE","assessmentType":"LAYER3","outputVersion":"2","output":{"allReoffendingPredictor":{"staticOrDynamic":"DYNAMIC","score":4.56,"band":"MEDIUM"},"violentReoffendingPredictor":{"staticOrDynamic":"DYNAMIC","score":4.56,"band":"MEDIUM"},"seriousViolentReoffendingPredictor":{"staticOrDynamic":"DYNAMIC","score":4.56,"band":"MEDIUM"},"directContactSexualReoffendingPredictor":{"score":2.81,"band":"MEDIUM"},"indirectImageContactSexualReoffendingPredictor":{"score":1.07,"band":"MEDIUM"},"combinedSeriousReoffendingPredictor":{"algorithmVersion":"6","staticOrDynamic":"DYNAMIC","score":50.1234,"band":"MEDIUM"}}},
      {"completedDate":"2022-06-12T18:23:20","status":"COMPLETE","assessmentType":"LAYER3","outputVersion":"2","output":{"allReoffendingPredictor":{"staticOrDynamic":"STATIC","score":1.23,"band":"LOW"},"violentReoffendingPredictor":{"staticOrDynamic":"STATIC","score":1.23,"band":"LOW"},"seriousViolentReoffendingPredictor":{"staticOrDynamic":"STATIC","score":1.23,"band":"LOW"},"directContactSexualReoffendingPredictor":{"score":2.81,"band":"MEDIUM"},"indirectImageContactSexualReoffendingPredictor":{"score":1.07,"band":"MEDIUM"},"combinedSeriousReoffendingPredictor":{"algorithmVersion":"6","staticOrDynamic":"STATIC","score":1.23,"band":"LOW"}}},
      {"completedDate":"2026-07-27T15:40:41","status":"COMPLETE","assessmentType":"STANDALONE","outputVersion":"2","output":{"allReoffendingPredictor":{"staticOrDynamic":"STATIC","score":65.12,"band":"MEDIUM"},"violentReoffendingPredictor":{"staticOrDynamic":"STATIC","score":35.04,"band":"MEDIUM"},"seriousViolentReoffendingPredictor":{"staticOrDynamic":"STATIC","score":1.79,"band":"MEDIUM"},"directContactSexualReoffendingPredictor":{"score":0,"band":"NOT_APPLICABLE"},"indirectImageContactSexualReoffendingPredictor":{"score":0,"band":"NOT_APPLICABLE"},"combinedSeriousReoffendingPredictor":{"algorithmVersion":"6","staticOrDynamic":"STATIC","score":1.79,"band":"MEDIUM"}}}
    ]
  """.trimIndent()
}
