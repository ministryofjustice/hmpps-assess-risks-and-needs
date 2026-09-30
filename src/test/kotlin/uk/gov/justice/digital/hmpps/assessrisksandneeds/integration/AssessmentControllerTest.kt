package uk.gov.justice.digital.hmpps.assessrisksandneeds.integration

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.BasicAssessmentSummary
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.Timeline
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.AuditService
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.DEFAULT_TIMEFRAME_WEEKS
import java.time.LocalDateTime

@AutoConfigureWebTestClient(timeout = "360000000")
@DisplayName("Assessment Tests")
class AssessmentControllerTest : IntegrationTestBase() {

  @MockkBean
  private lateinit var auditService: AuditService

  private val crn = "X123456"

  private val incompleteCrn = "X934567"

  private val incompleteSanCrn = "X845678"

  @BeforeEach
  fun setup() {
    every { auditService.sendEvent(any(), any()) } returns Unit
  }

  @Test
  fun `get criminogenic needs by crn`() {
    val response = webTestClient.get().uri("/needs/crn/$crn")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isOk
      .expectBody<String>()
      .returnResult().responseBody

    assertJson(oasysNeedsJson, response)
  }

  @Test
  fun `get criminogenic needs by crn within timeframe`() {
    val timeframe = 70L
    val response = webTestClient.get().uri("/needs/crn/$crn/$timeframe")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isOk
      .expectBody<String>()
      .returnResult().responseBody

    assertJson(oasysNeedsJson, response)
  }

  @Test
  fun `get criminogenic needs by crn with timeframe query param matches the deprecated path variant`() {
    val timeframe = 70L
    val fromQueryParam = webTestClient.get().uri("/needs/crn/$crn?timeframe=$timeframe")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isOk
      .expectBody<String>()
      .returnResult().responseBody

    val fromPath = webTestClient.get().uri("/needs/crn/$crn/$timeframe")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isOk
      .expectBody<String>()
      .returnResult().responseBody

    assertJson(checkNotNull(fromQueryParam), checkNotNull(fromPath))
  }

  @Test
  fun `get criminogenic needs by crn with timeframe query param not found`() {
    val timeframe = 5L
    webTestClient.get().uri("/needs/crn/$crn?timeframe=$timeframe")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  fun `get criminogenic needs by crn without timeframe query param uses the default timeframe`() {
    val fromDefault = webTestClient.get().uri("/needs/crn/$crn")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isOk
      .expectBody<String>()
      .returnResult().responseBody

    val fromExplicitDefault = webTestClient.get().uri("/needs/crn/$crn?timeframe=$DEFAULT_TIMEFRAME_WEEKS")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isOk
      .expectBody<String>()
      .returnResult().responseBody

    assertJson(checkNotNull(fromDefault), checkNotNull(fromExplicitDefault))
  }

  @Test
  fun `get criminogenic needs by crn with invalid timeframe query param returns bad request`() {
    webTestClient.get().uri("/needs/crn/$crn?timeframe=notANumber")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isBadRequest
  }

  @Test
  fun `get criminogenic needs by crn for a SAN assessment`() {
    val response = webTestClient.get().uri("/needs/crn/X654321")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isOk
      .expectBody<String>()
      .returnResult().responseBody

    assertJson(sanNeedsJson, response)
  }

  @Test
  fun `get criminogenic needs by crn for an incomplete assessment`() {
    val response = webTestClient.get().uri("/needs/crn/$incompleteCrn?excludeIncomplete=false")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isOk
      .expectBody<String>()
      .returnResult().responseBody

    assertJson(incompleteOasysNeedsJson, response)
  }

  @Test
  fun `get criminogenic needs by crn for a SAN assessment that is not yet signed off`() {
    val response = webTestClient.get().uri("/needs/crn/$incompleteSanCrn?excludeIncomplete=false")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isOk
      .expectBody<String>()
      .returnResult().responseBody

    assertJson(incompleteSanNeedsJson, response)
  }

  @Test
  fun `get criminogenic needs by crn for a SAN assessment that is not yet signed off returns not found by default`() {
    webTestClient.get().uri("/needs/crn/$incompleteSanCrn")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  fun `get criminogenic needs by crn within timeframe for an assessment that is not yet signed off`() {
    val timeframe = 70L
    val response = webTestClient.get().uri("/needs/crn/$incompleteSanCrn/$timeframe?excludeIncomplete=false")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isOk
      .expectBody<String>()
      .returnResult().responseBody

    assertJson(incompleteSanNeedsJson, response)
  }

  // An assessment that is still open has no completedDate, so recency is measured from its
  // initiationDate instead. Including incomplete assessments must not disable the timeframe.
  @Test
  fun `get criminogenic needs by crn within timeframe excludes an unsigned assessment initiated too long ago`() {
    val timeframe = 5L
    webTestClient.get().uri("/needs/crn/$incompleteSanCrn/$timeframe?excludeIncomplete=false")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  fun `get criminogenic needs by crn for an incomplete assessment returns not found by default`() {
    webTestClient.get().uri("/needs/crn/$incompleteCrn")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  fun `get criminogenic needs returns not found`() {
    webTestClient.get().uri("/needs/crn/NOT_FOUND")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  fun `get criminogenic needs by crn within timeframe not found`() {
    val timeframe = 5L
    val needsDto = webTestClient.get().uri("/needs/crn/$crn/$timeframe")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  fun `get assessment offence details by crn`() {
    val response = webTestClient.get().uri("/assessments/crn/$crn/offence")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isOk
      .expectBody<String>()
      .returnResult().responseBody

    assertJson(assessmentOffenceJson, response)
  }

  @Test
  fun `get assessment offence details with no complete assessments`() {
    val response = webTestClient.get().uri("/assessments/crn/X654321/offence")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isOk
      .expectBody<String>()
      .returnResult().responseBody

    assertJson(assessmentOffenceNoCompleteJson, response)
  }

  @Test
  fun `get assessment offence details not found`() {
    webTestClient.get().uri("/assessments/crn/NOT_FOUND/offence")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  fun `should return forbidden when user cannot access crn`() {
    webTestClient.get().uri("/assessments/crn/FORBIDDEN/offence")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  fun `should return not found when Delius cannot find crn`() {
    val response = webTestClient.get().uri("/assessments/crn/USER_ACCESS_NOT_FOUND/offence")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isNotFound
      .expectBody<String>()
      .returnResult().responseBody

    assertJson("""{"status":404,"developerMessage":"No such offender for CRN: USER_ACCESS_NOT_FOUND"}""", response)
  }

  @Test
  fun `get sexually motivated offence details by crn`() {
    val response = webTestClient.get().uri("/assessments/crn/$crn/sexually-motivated-offence")
      .headers(setAuthorisation(roles = listOf("ROLE_ARNS__RISKS__RO")))
      .exchange()
      .expectStatus().isOk
      .expectBody<String>()
      .returnResult().responseBody

    assertJson("""{"everCommittedSexualOffence":true}""", response)
  }

  @Test
  fun `get sexually motivated offence details not found`() {
    webTestClient.get().uri("/assessments/crn/NOT_FOUND/sexually-motivated-offence")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isNotFound
  }

  @ParameterizedTest
  @MethodSource("timelineIdentifiers")
  fun `successfully returns the timeline based on crn or noms id`(
    identifierType: String,
    identifierValue: String,
    timeline: Timeline,
  ) {
    val response = webTestClient.get().uri("/assessments/timeline/$identifierType/$identifierValue")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isOk
      .expectBody<String>()
      .returnResult().responseBody

    assertJson(timelineJson, response)
  }

  @Test
  fun `get san signal`() {
    val response = webTestClient.get().uri("/san-indicator/crn/$crn")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isOk
      .expectBody<String>()
      .returnResult().responseBody

    assertJson("""{"crn":"X123456","sanIndicator":false}""", response)
  }

  @Test
  fun `get san signal within timeframe`() {
    val timeframe = 70L
    val response = webTestClient.get().uri("/san-indicator/crn/$crn/$timeframe")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isOk
      .expectBody<String>()
      .returnResult().responseBody

    assertJson("""{"crn":"X123456","sanIndicator":false}""", response)
  }

  @Test
  fun `get san signal within timeframe not found`() {
    val timeframe = 5L
    webTestClient.get().uri("/san-indicator/crn/$crn/$timeframe")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  fun `get san signal with timeframe query param matches the deprecated path variant`() {
    val timeframe = 70L
    val fromQueryParam = webTestClient.get().uri("/san-indicator/crn/$crn?timeframe=$timeframe")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isOk
      .expectBody<String>()
      .returnResult().responseBody

    val fromPath = webTestClient.get().uri("/san-indicator/crn/$crn/$timeframe")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isOk
      .expectBody<String>()
      .returnResult().responseBody

    assertJson(checkNotNull(fromQueryParam), checkNotNull(fromPath))
  }

  @Test
  fun `get san signal with timeframe query param not found`() {
    val timeframe = 5L
    webTestClient.get().uri("/san-indicator/crn/$crn?timeframe=$timeframe")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  fun `GET mapps endpoint returns 200 with complete assessment data`() {
    val response = webTestClient.get()
      .uri("/assessments/mapps/crn/X123456")
      .headers(setAuthorisation(roles = listOf("ROLE_ARNS__EXTERNAL_API_RO")))
      .exchange()
      .expectStatus().isOk
      .expectBody<String>()
      .returnResult().responseBody

    assertJson(mappsJson, response)
  }

  @Test
  fun `Returns all required MAPPS fields, if possible`() {
    val response = webTestClient.get()
      .uri("/assessments/mapps/crn/X123456")
      .headers(setAuthorisation(roles = listOf("ROLE_ARNS__EXTERNAL_API_RO")))
      .exchange()
      .expectStatus().isOk
      .expectBody<String>()
      .returnResult().responseBody

    assertJson(mappsJson, response)
  }

  @Test
  fun `Countersigner name is optional`() {
    val response = webTestClient.get()
      .uri("/assessments/mapps/crn/X123456")
      .headers(setAuthorisation(roles = listOf("ROLE_ARNS__EXTERNAL_API_RO")))
      .exchange()
      .expectStatus().isOk
      .expectBody<String>()
      .returnResult().responseBody

    assertJson(mappsJson, response)
  }

  @Test
  fun `Assessment without countersigner returns null for countersignerName`() {
    val response = webTestClient.get()
      .uri("/assessments/mapps/crn/X654321")
      .headers(setAuthorisation(roles = listOf("ROLE_ARNS__EXTERNAL_API_RO")))
      .exchange()
      .expectStatus().isOk
      .expectBody<String>()
      .returnResult().responseBody

    assertJson(mappsSanJson, response)
  }

  @Test
  fun `Returns all complete assessments sorted by date descending`() {
    val response = webTestClient.get()
      .uri("/assessments/mapps/crn/X123456")
      .headers(setAuthorisation(roles = listOf("ROLE_ARNS__EXTERNAL_API_RO")))
      .exchange()
      .expectStatus().isOk
      .expectBody<String>()
      .returnResult().responseBody

    assertJson(mappsJson, response)
  }

  @Test
  fun `MAPPS endpoint returns 404 when no assessment exists`() {
    val response = webTestClient.get()
      .uri("/assessments/mapps/crn/NOT_FOUND")
      .headers(setAuthorisation(roles = listOf("ROLE_ARNS__EXTERNAL_API_RO")))
      .exchange()
      .expectStatus().isNotFound
      .expectBody<String>()
      .returnResult().responseBody

    assertJson("""{"status":404}""", response)
  }

  @Test
  fun `Returns 403 without correct role ROLE_ARNS__EXTERNAL_API_RO`() {
    webTestClient.get()
      .uri("/assessments/mapps/crn/X123456")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  fun `Returns 401 without authentication`() {
    webTestClient.get()
      .uri("/assessments/mapps/crn/X123456")
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  fun `Endpoint accepts nomisId as identifier type`() {
    val response = webTestClient.get()
      .uri("/assessments/mapps/nomisId/A1234YZ")
      .headers(setAuthorisation(roles = listOf("ROLE_ARNS__EXTERNAL_API_RO")))
      .exchange()
      .expectStatus().isOk
      .expectBody<String>()
      .returnResult().responseBody

    assertJson(mappsJson, response)
  }

  @Test
  fun `Does not return OPEN or other incomplete statuses`() {
    val response = webTestClient.get()
      .uri("/assessments/mapps/crn/X123456")
      .headers(setAuthorisation(roles = listOf("ROLE_ARNS__EXTERNAL_API_RO")))
      .exchange()
      .expectStatus().isOk
      .expectBody<String>()
      .returnResult().responseBody

    assertJson(mappsJson, response)
  }

  @Test
  fun `Does not return STANDALONE assessment types`() {
    val response = webTestClient.get()
      .uri("/assessments/mapps/crn/X123456")
      .headers(setAuthorisation(roles = listOf("ROLE_ARNS__EXTERNAL_API_RO")))
      .exchange()
      .expectStatus().isOk
      .expectBody<String>()
      .returnResult().responseBody

    assertJson(mappsJson, response)
  }

  private val oasysNeedsJson = """
    {
      "identifiedNeeds": [
        {"section":"EDUCATION_TRAINING_AND_EMPLOYABILITY","name":"Education, Training and Employability","riskOfHarm":false,"riskOfReoffending":false,"score":3,"oasysThreshold":{"standard":3}},
        {"section":"RELATIONSHIPS","name":"Relationships","riskOfHarm":false,"riskOfReoffending":false,"score":3,"oasysThreshold":{"standard":2}},
        {"section":"LIFESTYLE_AND_ASSOCIATES","name":"Lifestyle and Associates","riskOfHarm":true,"riskOfReoffending":true,"score":3,"oasysThreshold":{"standard":2}},
        {"section":"ALCOHOL_MISUSE","name":"Alcohol Misuse","riskOfHarm":false,"riskOfReoffending":true,"score":4,"oasysThreshold":{"standard":4}},
        {"section":"THINKING_AND_BEHAVIOUR","name":"Thinking and Behaviour","riskOfHarm":true,"riskOfReoffending":true,"score":7,"oasysThreshold":{"standard":4}}
      ],
      "notIdentifiedNeeds": [
        {"section":"ACCOMMODATION","name":"Accommodation","riskOfHarm":false,"riskOfReoffending":false,"score":0,"oasysThreshold":{"standard":2}},
        {"section":"DRUG_MISUSE","name":"Drug Misuse","score":0,"oasysThreshold":{"standard":2}},
        {"section":"ATTITUDE","name":"Attitudes","riskOfHarm":false,"riskOfReoffending":false,"score":0,"oasysThreshold":{"standard":2}}
      ],
      "unansweredNeeds": [],
      "assessmentVersion":"OASYS",
      "assessedOn":"2024-12-19T16:57:25"
    }
  """.trimIndent()

  private val incompleteOasysNeedsJson = """
    {
      "identifiedNeeds": [],
      "notIdentifiedNeeds": [],
      "unansweredNeeds": [
        {"section":"ACCOMMODATION","name":"Accommodation","oasysThreshold":{}},
        {"section":"EDUCATION_TRAINING_AND_EMPLOYABILITY","name":"Education, Training and Employability","riskOfHarm":false,"riskOfReoffending":false,"oasysThreshold":{}},
        {"section":"RELATIONSHIPS","name":"Relationships","riskOfHarm":false,"riskOfReoffending":false,"oasysThreshold":{}},
        {"section":"LIFESTYLE_AND_ASSOCIATES","name":"Lifestyle and Associates","riskOfHarm":true,"riskOfReoffending":true,"oasysThreshold":{}},
        {"section":"DRUG_MISUSE","name":"Drug Misuse","oasysThreshold":{}},
        {"section":"ALCOHOL_MISUSE","name":"Alcohol Misuse","riskOfHarm":false,"riskOfReoffending":true,"oasysThreshold":{}},
        {"section":"THINKING_AND_BEHAVIOUR","name":"Thinking and Behaviour","oasysThreshold":{}},
        {"section":"ATTITUDE","name":"Attitudes","riskOfHarm":false,"riskOfReoffending":false,"oasysThreshold":{}}
      ],
      "assessmentVersion":"OASYS"
    }
  """.trimIndent()

  private val sanNeedsJson = """
    {
      "identifiedNeeds": [
        {"section":"PERSONAL_RELATIONSHIPS_AND_COMMUNITY","name":"Personal relationships and community","riskOfHarm":false,"riskOfReoffending":false,"score":3,"oasysThreshold":{"standard":2}},
        {"section":"THINKING_ATTITUDES_AND_BEHAVIOUR","name":"Thinking, behaviours and attitudes","riskOfHarm":false,"riskOfReoffending":false,"score":6,"oasysThreshold":{"standard":2}}
      ],
      "notIdentifiedNeeds": [
        {"section":"ACCOMMODATION","name":"Accommodation","riskOfHarm":false,"riskOfReoffending":false,"score":1,"oasysThreshold":{"standard":2}},
        {"section":"EMPLOYMENT_AND_EDUCATION","name":"Employment and education","riskOfHarm":false,"riskOfReoffending":false,"score":0,"oasysThreshold":{"standard":2}},
        {"section":"LIFESTYLE_AND_ASSOCIATES","name":"Lifestyle and associates","score":0,"oasysThreshold":{"standard":2}},
        {"section":"DRUG_USE","name":"Drug use","riskOfHarm":false,"riskOfReoffending":false,"score":0,"oasysThreshold":{"standard":2}},
        {"section":"ALCOHOL_USE","name":"Alcohol use","riskOfHarm":false,"riskOfReoffending":false,"score":0,"oasysThreshold":{"standard":2}}
      ],
      "unansweredNeeds": [],
      "assessmentVersion":"SAN",
      "assessedOn":"2024-12-20T10:00:00"
    }
  """.trimIndent()

  private val incompleteSanNeedsJson = """
    {
      "identifiedNeeds": [
        {"section":"PERSONAL_RELATIONSHIPS_AND_COMMUNITY","name":"Personal relationships and community","riskOfHarm":false,"riskOfReoffending":false,"score":3,"oasysThreshold":{"standard":2}},
        {"section":"THINKING_ATTITUDES_AND_BEHAVIOUR","name":"Thinking, behaviours and attitudes","riskOfHarm":false,"riskOfReoffending":false,"score":6,"oasysThreshold":{"standard":2}}
      ],
      "notIdentifiedNeeds": [
        {"section":"ACCOMMODATION","name":"Accommodation","riskOfHarm":false,"riskOfReoffending":false,"score":1,"oasysThreshold":{"standard":2}},
        {"section":"EMPLOYMENT_AND_EDUCATION","name":"Employment and education","riskOfHarm":false,"riskOfReoffending":false,"score":0,"oasysThreshold":{"standard":2}},
        {"section":"LIFESTYLE_AND_ASSOCIATES","name":"Lifestyle and associates","score":0,"oasysThreshold":{"standard":2}},
        {"section":"DRUG_USE","name":"Drug use","riskOfHarm":false,"riskOfReoffending":false,"score":0,"oasysThreshold":{"standard":2}},
        {"section":"ALCOHOL_USE","name":"Alcohol use","riskOfHarm":false,"riskOfReoffending":false,"score":0,"oasysThreshold":{"standard":2}}
      ],
      "unansweredNeeds": [],
      "assessmentVersion":"SAN"
    }
  """.trimIndent()

  private val assessmentOffenceJson = """
    {
      "crn":"X123456",
      "limitedAccessOffender":false,
      "assessments":[
        {"assessmentId":9630348,"assessmentType":"LAYER1","dateCompleted":"2022-04-27T12:46:39","initiationDate":"2022-04-27T12:42:25","assessmentStatus":"COMPLETE"},
        {"assessmentId":9632348,"assessmentType":"LAYER3","partcompStatus":"Unsigned","dateCompleted":"2022-06-09T15:13:18","initiationDate":"2022-05-31T10:37:05","assessmentStatus":"LOCKED_INCOMPLETE"},
        {"assessmentId":9634348,"assessmentType":"LAYER3","dateCompleted":"2022-06-09T15:16:21","initiationDate":"2022-06-09T15:13:55","assessmentStatus":"COMPLETE"},
        {"assessmentId":9635350,"assessmentType":"LAYER3","dateCompleted":"2022-06-10T18:23:20","initiationDate":"2022-06-10T18:22:02","assessmentStatus":"COMPLETE"},
        {
          "assessmentId":9635351,"assessmentType":"LAYER3","dateCompleted":"2022-07-21T15:43:12","initiationDate":"2022-06-10T18:23:51",
          "assessorSignedDate":"2022-07-21T15:43:12","assessmentStatus":"COMPLETE","superStatus":"COMPLETE",
          "offence":"TBA","disinhibitors":["Alcohol"],"patternOfOffending":"TBA","offenceInvolved":["Carrying or using a weapon"],
          "specificWeapon":"TBA","victimPerpetratorRelationship":"blah","victimOtherInfo":"mmmmmm","evidencedMotivations":["Sexual motivation"],
          "offenceDetails":[
            {"type":"CONCURRENT","offenceDate":"2021-11-01T00:00:00","offenceCode":"028","offenceSubCode":"00","offence":"Burglary in a dwelling","subOffence":"Burglary in a dwelling    [Use this code only if you are unable to determine which subcoded Offence applies]"},
            {"type":"CURRENT","offenceDate":"2021-12-25T00:00:00","offenceCode":"020","offenceSubCode":"05","offence":"Sexual assault on a female","subOffence":"Sexual assault on a female"}
          ],
          "victimDetails":[
            {"age":"26-49","gender":"Male","ethnicCategory":"White - Irish","victimRelation":"Stranger"},
            {"age":"50-64","gender":"Male","ethnicCategory":"Chinese or other ethnic group - Chinese TEST 080212","victimRelation":"Spouse/Partner - live in"}
          ],
          "laterWIPAssessmentExists":true,"latestWIPDate":"2022-07-21T15:43:58","laterSignLockAssessmentExists":false,
          "laterPartCompUnsignedAssessmentExists":false,"latestPartCompUnsignedDate":"2022-05-31T10:37:05",
          "laterPartCompSignedAssessmentExists":false,"laterCompleteAssessmentExists":false,"latestCompleteDate":"2022-07-21T15:43:12"
        },
        {"assessmentId":9639348,"assessmentType":"LAYER3","initiationDate":"2022-07-21T15:43:58","assessmentStatus":"OPEN"}
      ]
    }
  """.trimIndent()

  private val assessmentOffenceNoCompleteJson = """
    {
      "crn":"X654321",
      "limitedAccessOffender":false,
      "assessments":[
        {"assessmentId":2998,"assessmentType":"LAYER1","dateCompleted":"2011-02-07T17:09:07","initiationDate":"2011-02-01T15:37:09","assessmentStatus":"LOCKED_INCOMPLETE"},
        {"assessmentId":3432,"assessmentType":"LAYER1","initiationDate":"2011-02-07T17:10:17","assessmentStatus":"SIGNED"}
      ]
    }
  """.trimIndent()

  private val timelineJson = """
    {"timeline":[
      {"assessmentId":9630348,"initiationDate":"2023-12-17T16:57:25","completedDate":"2024-12-19T16:57:25","assessmentType":"LAYER3","status":"COMPLETE"},
      {"assessmentId":9632348,"initiationDate":"2022-05-31T10:37:05","completedDate":"2022-06-09T15:13:18","assessmentType":"LAYER3","status":"LOCKED_INCOMPLETE"},
      {"assessmentId":9634348,"initiationDate":"2022-06-09T15:13:55","completedDate":"2022-06-09T15:16:21","assessmentType":"LAYER3","status":"COMPLETE"},
      {"assessmentId":9635350,"initiationDate":"2022-06-10T18:22:02","completedDate":"2022-06-10T18:23:20","assessmentType":"LAYER3","status":"COMPLETE"},
      {"assessmentId":9635351,"initiationDate":"2022-06-10T18:23:51","completedDate":"2022-07-21T15:43:12","assessmentType":"LAYER3","status":"COMPLETE"},
      {"assessmentId":9639348,"initiationDate":"2022-07-21T15:43:58","completedDate":"2022-07-27T12:09:41","assessmentType":"LAYER3","status":"COMPLETE"},
      {"assessmentId":9641348,"initiationDate":"2022-07-27T12:10:58","assessmentType":"LAYER3","status":"OPEN"},
      {"assessmentId":6661348,"completedDate":"2003-07-27T12:10:58","assessmentType":"LAYER3","status":"COMPLETE"},
      {"assessmentId":6661347,"assessmentType":"LAYER3","status":"OPEN"}
    ]}
  """.trimIndent()

  private val mappsJson = """
    {"assessments":[
      {"assessmentId":9630348,"initiationDate":"2023-12-17T16:57:25","dateCompleted":"2024-12-19T16:57:25","assessmentType":"LAYER3","assessmentStatus":"COMPLETE","assessorName":"LevelTwo CentralSupport"},
      {"assessmentId":9639348,"initiationDate":"2022-07-21T15:43:58","dateCompleted":"2022-07-27T12:09:41","assessmentType":"LAYER3","assessmentStatus":"COMPLETE","assessorName":"Lisa Chen"},
      {"assessmentId":9635351,"initiationDate":"2022-06-10T18:23:51","dateCompleted":"2022-07-21T15:43:12","assessmentType":"LAYER3","assessmentStatus":"COMPLETE","assessorName":"David Garcia","countersignerName":"Emma Martinez"},
      {"assessmentId":9635350,"initiationDate":"2022-06-10T18:22:02","dateCompleted":"2022-06-10T18:23:20","assessmentType":"LAYER3","assessmentStatus":"COMPLETE","assessorName":"Sarah Williams","countersignerName":"Robert Brown"},
      {"assessmentId":9634348,"initiationDate":"2022-06-09T15:13:55","dateCompleted":"2022-06-09T15:16:21","assessmentType":"LAYER3","assessmentStatus":"COMPLETE","assessorName":"Mike Johnson"},
      {"assessmentId":6661348,"dateCompleted":"2003-07-27T12:10:58","assessmentType":"LAYER3","assessmentStatus":"COMPLETE","assessorName":"Mike Johnson","countersignerName":"John Smith"}
    ]}
  """.trimIndent()

  private val mappsSanJson = """
    {"assessments":[
      {"assessmentId":9700001,"initiationDate":"2024-12-17T16:57:25","dateCompleted":"2024-12-20T10:00:00","assessmentType":"LAYER3","assessmentStatus":"COMPLETE","assessorName":"Mike Johnson"}
    ]}
  """.trimIndent()

  private fun assertJson(expected: String, actual: String?) {
    JSONAssert.assertEquals(expected, checkNotNull(actual), JSONCompareMode.STRICT)
  }

  companion object {
    val timeline = Timeline(
      listOf(
        BasicAssessmentSummary(
          9630348,
          LocalDateTime.parse("2023-12-17T16:57:25"),
          LocalDateTime.of(2024, 12, 19, 16, 57, 25),
          "LAYER3",
          "COMPLETE",
        ),
        BasicAssessmentSummary(
          9632348,
          LocalDateTime.parse("2022-05-31T10:37:05"),
          LocalDateTime.parse("2022-06-09T15:13:18"),
          "LAYER3",
          "LOCKED_INCOMPLETE",
        ),
        BasicAssessmentSummary(
          9634348,
          LocalDateTime.parse("2022-06-09T15:13:55"),
          LocalDateTime.parse("2022-06-09T15:16:21"),
          "LAYER3",
          "COMPLETE",
        ),
        BasicAssessmentSummary(
          9635350,
          LocalDateTime.parse("2022-06-10T18:22:02"),
          LocalDateTime.parse("2022-06-10T18:23:20"),
          "LAYER3",
          "COMPLETE",
        ),
        BasicAssessmentSummary(
          9635351,
          LocalDateTime.parse("2022-06-10T18:23:51"),
          LocalDateTime.parse("2022-07-21T15:43:12"),
          "LAYER3",
          "COMPLETE",
        ),
        BasicAssessmentSummary(
          9639348,
          LocalDateTime.parse("2022-07-21T15:43:58"),
          LocalDateTime.parse("2022-07-27T12:09:41"),
          "LAYER3",
          "COMPLETE",
        ),
        BasicAssessmentSummary(
          9641348,
          LocalDateTime.parse("2022-07-27T12:10:58"),
          null,
          "LAYER3",
          "OPEN",
        ),
        BasicAssessmentSummary(
          6661348,
          null,
          LocalDateTime.parse("2003-07-27T12:10:58"),
          "LAYER3",
          "COMPLETE",
        ),
        BasicAssessmentSummary(
          6661347,
          null,
          null,
          "LAYER3",
          "OPEN",
        ),
      ),
    )

    @JvmStatic
    fun timelineIdentifiers() = listOf(
      Arguments.of("crn", "X123456", timeline),
      Arguments.of("nomisId", "A1234YZ", timeline),
    )
  }
}
