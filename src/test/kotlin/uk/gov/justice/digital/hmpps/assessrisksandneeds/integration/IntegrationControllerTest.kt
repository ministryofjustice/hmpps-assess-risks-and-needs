package uk.gov.justice.digital.hmpps.assessrisksandneeds.integration

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.http.HttpStatus
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.AuditService

@AutoConfigureWebTestClient(timeout = "360000000")
@DisplayName("Assessment Tests")
class IntegrationControllerTest : IntegrationTestBase() {

  @MockkBean
  private lateinit var auditService: AuditService

  private val crn = "X123456"
  private val incompleteCrn = "X934567"

  @BeforeEach
  fun setup() {
    every { auditService.sendEvent(any(), any()) } returns Unit
  }

  @Test
  fun `get rosh by crn`() {
    webTestClient.get().uri("/risks/rosh/$crn")
      .headers(setAuthorisation(roles = listOf("ROLE_ARNS__RISKS__RO")))
      .exchange()
      .expectStatus().isOk
      .expectBody<String>()
      .consumeWith {
        JSONAssert.assertEquals(
          """
            {
              "riskToSelf": {
                "suicide": {
                  "risk": "YES",
                  "previous": "YES",
                  "current": "YES",
                  "currentConcernsText": "Suicide and/or Self-harm current concerns"
                },
                "selfHarm": {
                  "risk": "DK"
                },
                "custody": {
                  "risk": "YES",
                  "previous": "YES",
                  "previousConcernsText": "Coping in custody / hostel setting previous concerns",
                  "current": "NA"
                },
                "hostelSetting": {
                  "risk": "YES",
                  "previous": "DK",
                  "current": "NO"
                },
                "vulnerability": {
                  "risk": "YES",
                  "previous": "YES",
                  "previousConcernsText": "Vulnerability previous concerns free text",
                  "current": "YES",
                  "currentConcernsText": "Vulnerability current concerns free text"
                }
              },
              "otherRisks": {
                "escapeOrAbscond": "YES",
                "controlIssuesDisruptiveBehaviour": "YES",
                "breachOfTrust": "DK",
                "riskToOtherPrisoners": "YES"
              },
              "summary": {
                "whoIsAtRisk": "whoisAtRisk",
                "natureOfRisk": "natureOfRisk",
                "riskImminence": "riskImminence",
                "riskIncreaseFactors": "riskIncreaseFactors",
                "riskMitigationFactors": "riskMitigationFactors",
                "analysisOfRiskFactors": "analysisOfRiskFactors",
                "riskInCommunity": {
                  "LOW": ["Children", "Known Adult"],
                  "MEDIUM": ["Public"],
                  "HIGH": ["Staff"]
                },
                "riskInCustody": {
                  "LOW": ["Children", "Public", "Known Adult"],
                  "HIGH": ["Prisoners"],
                  "VERY_HIGH": ["Staff"]
                },
                "overallRiskLevel": "VERY_HIGH"
              },
              "assessedOn": "2024-12-19T16:57:25"
            }
          """.trimIndent(),
          checkNotNull(it.responseBody),
          JSONCompareMode.STRICT,
        )
      }
  }

  @Test
  fun `get rosh by crn within timeframe`() {
    val timeframe = 60L
    webTestClient.get().uri("/risks/rosh/$crn/$timeframe")
      .headers(setAuthorisation(roles = listOf("ROLE_ARNS__RISKS__RO")))
      .exchange()
      .expectStatus().isOk
      .expectBody<String>()
      .consumeWith {
        JSONAssert.assertEquals(
          """
            {
              "riskToSelf": {
                "suicide": {
                  "risk": "YES",
                  "previous": "YES",
                  "current": "YES",
                  "currentConcernsText": "Suicide and/or Self-harm current concerns"
                },
                "selfHarm": {"risk": "DK"},
                "custody": {
                  "risk": "YES",
                  "previous": "YES",
                  "previousConcernsText": "Coping in custody / hostel setting previous concerns",
                  "current": "NA"
                },
                "hostelSetting": {"risk": "YES", "previous": "DK", "current": "NO"},
                "vulnerability": {
                  "risk": "YES",
                  "previous": "YES",
                  "previousConcernsText": "Vulnerability previous concerns free text",
                  "current": "YES",
                  "currentConcernsText": "Vulnerability current concerns free text"
                }
              },
              "otherRisks": {
                "escapeOrAbscond": "YES",
                "controlIssuesDisruptiveBehaviour": "YES",
                "breachOfTrust": "DK",
                "riskToOtherPrisoners": "YES"
              },
              "summary": {
                "whoIsAtRisk": "whoisAtRisk",
                "natureOfRisk": "natureOfRisk",
                "riskImminence": "riskImminence",
                "riskIncreaseFactors": "riskIncreaseFactors",
                "riskMitigationFactors": "riskMitigationFactors",
                "analysisOfRiskFactors": "analysisOfRiskFactors",
                "riskInCommunity": {
                  "LOW": ["Children", "Known Adult"],
                  "MEDIUM": ["Public"],
                  "HIGH": ["Staff"]
                },
                "riskInCustody": {
                  "LOW": ["Children", "Public", "Known Adult"],
                  "HIGH": ["Prisoners"],
                  "VERY_HIGH": ["Staff"]
                },
                "overallRiskLevel": "VERY_HIGH"
              },
              "assessedOn": "2024-12-19T16:57:25"
            }
          """.trimIndent(),
          checkNotNull(it.responseBody),
          JSONCompareMode.STRICT,
        )
      }
  }

  @Test
  fun `get rosh by crn with timeframe query param matches the deprecated path variant`() {
    val timeframe = 60L
    val fromQueryParam = webTestClient.get().uri("/risks/rosh/$crn?timeframe=$timeframe")
      .headers(setAuthorisation(roles = listOf("ROLE_ARNS__RISKS__RO")))
      .exchange()
      .expectStatus().isOk
      .expectBody<String>()
      .returnResult().responseBody

    val fromPath = webTestClient.get().uri("/risks/rosh/$crn/$timeframe")
      .headers(setAuthorisation(roles = listOf("ROLE_ARNS__RISKS__RO")))
      .exchange()
      .expectStatus().isOk
      .expectBody<String>()
      .returnResult().responseBody

    JSONAssert.assertEquals(
      checkNotNull(fromQueryParam),
      checkNotNull(fromPath),
      JSONCompareMode.STRICT,
    )
  }

  @Test
  fun `get rosh by crn with a timeframe query param that excludes all assessments returns no assessed date`() {
    val timeframe = 2L
    webTestClient.get().uri("/risks/rosh/$crn?timeframe=$timeframe")
      .headers(setAuthorisation(roles = listOf("ROLE_ARNS__RISKS__RO")))
      .exchange()
      .expectStatus().isOk
      .expectBody<String>()
      .consumeWith {
        JSONAssert.assertEquals(
          """{"riskToSelf":{},"otherRisks":{},"summary":{"riskInCommunity":{},"riskInCustody":{}}}""",
          checkNotNull(it.responseBody),
          JSONCompareMode.STRICT,
        )
      }
  }

  @Test
  fun `get criminogenic needs by crn`() {
    webTestClient.get().uri("/needs/$crn")
      .headers(setAuthorisation(roles = listOf("ROLE_ARNS__RISKS__RO")))
      .exchange()
      .expectStatus().isOk
      .expectBody<String>()
      .consumeWith {
        JSONAssert.assertEquals(
          """
            {
              "needs": [
                {"section":"EDUCATION_TRAINING_AND_EMPLOYABILITY","name":"Education, Training and Employability","needStatus":"IDENTIFIED_NEED","riskOfHarm":false,"riskOfReoffending":false,"score":3,"oasysThreshold":{"standard":3}},
                {"section":"RELATIONSHIPS","name":"Relationships","needStatus":"IDENTIFIED_NEED","riskOfHarm":false,"riskOfReoffending":false,"score":3,"oasysThreshold":{"standard":2}},
                {"section":"LIFESTYLE_AND_ASSOCIATES","name":"Lifestyle and Associates","needStatus":"IDENTIFIED_NEED","riskOfHarm":true,"riskOfReoffending":true,"score":3,"oasysThreshold":{"standard":2}},
                {"section":"ALCOHOL_MISUSE","name":"Alcohol Misuse","needStatus":"IDENTIFIED_NEED","riskOfHarm":false,"riskOfReoffending":true,"score":4,"oasysThreshold":{"standard":4}},
                {"section":"THINKING_AND_BEHAVIOUR","name":"Thinking and Behaviour","needStatus":"IDENTIFIED_NEED","riskOfHarm":true,"riskOfReoffending":true,"score":7,"oasysThreshold":{"standard":4}},
                {"section":"ACCOMMODATION","name":"Accommodation","needStatus":"NOT_IDENTIFIED_NEED","riskOfHarm":false,"riskOfReoffending":false,"score":0,"oasysThreshold":{"standard":2}},
                {"section":"DRUG_MISUSE","name":"Drug Misuse","needStatus":"NOT_IDENTIFIED_NEED","score":0,"oasysThreshold":{"standard":2}},
                {"section":"ATTITUDE","name":"Attitudes","needStatus":"NOT_IDENTIFIED_NEED","riskOfHarm":false,"riskOfReoffending":false,"score":0,"oasysThreshold":{"standard":2}},
                {"section":"FINANCE","name":"Finance","needStatus":"UNSCORED_NEED","riskOfHarm":false,"riskOfReoffending":false,"oasysThreshold":{}},
                {"section":"EMOTIONAL_WELLBEING","name":"Emotional Well-being","needStatus":"UNSCORED_NEED","riskOfHarm":false,"riskOfReoffending":false,"oasysThreshold":{}}
              ],
              "assessmentVersion":"OASYS",
              "assessedOn":"2024-12-19T16:57:25"
            }
          """.trimIndent(),
          checkNotNull(it.responseBody),
          JSONCompareMode.STRICT,
        )
      }
  }

  @Test
  fun `get criminogenic needs by crn for an incomplete assessment`() {
    webTestClient.get().uri("/needs/$incompleteCrn?excludeIncomplete=false")
      .headers(setAuthorisation(roles = listOf("ROLE_ARNS__RISKS__RO")))
      .exchange()
      .expectStatus().isOk
      .expectBody<String>()
      .consumeWith {
        JSONAssert.assertEquals(
          """
            {
              "needs": [
                {"section":"ACCOMMODATION","name":"Accommodation","needStatus":"UNANSWERED_NEED","oasysThreshold":{}},
                {"section":"EDUCATION_TRAINING_AND_EMPLOYABILITY","name":"Education, Training and Employability","needStatus":"UNANSWERED_NEED","riskOfHarm":false,"riskOfReoffending":false,"oasysThreshold":{}},
                {"section":"RELATIONSHIPS","name":"Relationships","needStatus":"UNANSWERED_NEED","riskOfHarm":false,"riskOfReoffending":false,"oasysThreshold":{}},
                {"section":"LIFESTYLE_AND_ASSOCIATES","name":"Lifestyle and Associates","needStatus":"UNANSWERED_NEED","riskOfHarm":true,"riskOfReoffending":true,"oasysThreshold":{}},
                {"section":"DRUG_MISUSE","name":"Drug Misuse","needStatus":"UNANSWERED_NEED","oasysThreshold":{}},
                {"section":"ALCOHOL_MISUSE","name":"Alcohol Misuse","needStatus":"UNANSWERED_NEED","riskOfHarm":false,"riskOfReoffending":true,"oasysThreshold":{}},
                {"section":"THINKING_AND_BEHAVIOUR","name":"Thinking and Behaviour","needStatus":"UNANSWERED_NEED","oasysThreshold":{}},
                {"section":"ATTITUDE","name":"Attitudes","needStatus":"UNANSWERED_NEED","riskOfHarm":false,"riskOfReoffending":false,"oasysThreshold":{}},
                {"section":"FINANCE","name":"Finance","needStatus":"UNSCORED_NEED","oasysThreshold":{}},
                {"section":"EMOTIONAL_WELLBEING","name":"Emotional Well-being","needStatus":"UNSCORED_NEED","oasysThreshold":{}}
              ],
              "assessmentVersion":"OASYS"
            }
          """.trimIndent(),
          checkNotNull(it.responseBody),
          JSONCompareMode.STRICT,
        )
      }
  }

  @Test
  fun `get criminogenic needs by crn within timeframe`() {
    val timeframe = 60L
    webTestClient.get().uri("/needs/$crn/$timeframe")
      .headers(setAuthorisation(roles = listOf("ROLE_ARNS__RISKS__RO")))
      .exchange()
      .expectStatus().isOk
      .expectBody<String>()
      .consumeWith {
        JSONAssert.assertEquals(
          """{"needs":[{"section":"EDUCATION_TRAINING_AND_EMPLOYABILITY","name":"Education, Training and Employability","needStatus":"IDENTIFIED_NEED","riskOfHarm":false,"riskOfReoffending":false,"score":3,"oasysThreshold":{"standard":3}},{"section":"RELATIONSHIPS","name":"Relationships","needStatus":"IDENTIFIED_NEED","riskOfHarm":false,"riskOfReoffending":false,"score":3,"oasysThreshold":{"standard":2}},{"section":"LIFESTYLE_AND_ASSOCIATES","name":"Lifestyle and Associates","needStatus":"IDENTIFIED_NEED","riskOfHarm":true,"riskOfReoffending":true,"score":3,"oasysThreshold":{"standard":2}},{"section":"ALCOHOL_MISUSE","name":"Alcohol Misuse","needStatus":"IDENTIFIED_NEED","riskOfHarm":false,"riskOfReoffending":true,"score":4,"oasysThreshold":{"standard":4}},{"section":"THINKING_AND_BEHAVIOUR","name":"Thinking and Behaviour","needStatus":"IDENTIFIED_NEED","riskOfHarm":true,"riskOfReoffending":true,"score":7,"oasysThreshold":{"standard":4}},{"section":"ACCOMMODATION","name":"Accommodation","needStatus":"NOT_IDENTIFIED_NEED","riskOfHarm":false,"riskOfReoffending":false,"score":0,"oasysThreshold":{"standard":2}},{"section":"DRUG_MISUSE","name":"Drug Misuse","needStatus":"NOT_IDENTIFIED_NEED","score":0,"oasysThreshold":{"standard":2}},{"section":"ATTITUDE","name":"Attitudes","needStatus":"NOT_IDENTIFIED_NEED","riskOfHarm":false,"riskOfReoffending":false,"score":0,"oasysThreshold":{"standard":2}},{"section":"FINANCE","name":"Finance","needStatus":"UNSCORED_NEED","riskOfHarm":false,"riskOfReoffending":false,"oasysThreshold":{}},{"section":"EMOTIONAL_WELLBEING","name":"Emotional Well-being","needStatus":"UNSCORED_NEED","riskOfHarm":false,"riskOfReoffending":false,"oasysThreshold":{}}],"assessmentVersion":"OASYS","assessedOn":"2024-12-19T16:57:25"}""",
          checkNotNull(it.responseBody),
          JSONCompareMode.STRICT,
        )
      }
  }

  @Test
  fun `get criminogenic needs by crn within timeframe not found`() {
    val timeframe = 2L
    webTestClient.get().uri("/needs/$crn/$timeframe")
      .headers(setAuthorisation(roles = listOf("ROLE_ARNS__RISKS__RO")))
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  fun `get criminogenic needs by crn with timeframe query param matches the deprecated path variant`() {
    val timeframe = 60L
    val fromQueryParam = webTestClient.get().uri("/needs/$crn?timeframe=$timeframe")
      .headers(setAuthorisation(roles = listOf("ROLE_ARNS__RISKS__RO")))
      .exchange()
      .expectStatus().isOk
      .expectBody<String>()
      .returnResult().responseBody

    val fromPath = webTestClient.get().uri("/needs/$crn/$timeframe")
      .headers(setAuthorisation(roles = listOf("ROLE_ARNS__RISKS__RO")))
      .exchange()
      .expectStatus().isOk
      .expectBody<String>()
      .returnResult().responseBody

    JSONAssert.assertEquals(
      checkNotNull(fromQueryParam),
      checkNotNull(fromPath),
      JSONCompareMode.STRICT,
    )
  }

  @Test
  fun `get criminogenic needs by crn with timeframe query param not found`() {
    val timeframe = 2L
    webTestClient.get().uri("/needs/$crn?timeframe=$timeframe")
      .headers(setAuthorisation(roles = listOf("ROLE_ARNS__RISKS__RO")))
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  fun `get criminogenic needs by crn for a SAN assessment`() {
    webTestClient.get().uri("/needs/X654321")
      .headers(setAuthorisation(roles = listOf("ROLE_ARNS__RISKS__RO")))
      .exchange()
      .expectStatus().isOk
      .expectBody<String>()
      .consumeWith {
        JSONAssert.assertEquals(
          """
            {
              "needs": [
                {"section":"PERSONAL_RELATIONSHIPS_AND_COMMUNITY","name":"Personal relationships and community","needStatus":"IDENTIFIED_NEED","riskOfHarm":false,"riskOfReoffending":false,"score":3,"oasysThreshold":{"standard":2}},
                {"section":"THINKING_ATTITUDES_AND_BEHAVIOUR","name":"Thinking, behaviours and attitudes","needStatus":"IDENTIFIED_NEED","riskOfHarm":false,"riskOfReoffending":false,"score":6,"oasysThreshold":{"standard":2}},
                {"section":"ACCOMMODATION","name":"Accommodation","needStatus":"NOT_IDENTIFIED_NEED","riskOfHarm":false,"riskOfReoffending":false,"score":1,"oasysThreshold":{"standard":2}},
                {"section":"EMPLOYMENT_AND_EDUCATION","name":"Employment and education","needStatus":"NOT_IDENTIFIED_NEED","riskOfHarm":false,"riskOfReoffending":false,"score":0,"oasysThreshold":{"standard":2}},
                {"section":"LIFESTYLE_AND_ASSOCIATES","name":"Lifestyle and associates","needStatus":"NOT_IDENTIFIED_NEED","score":0,"oasysThreshold":{"standard":2}},
                {"section":"DRUG_USE","name":"Drug use","needStatus":"NOT_IDENTIFIED_NEED","riskOfHarm":false,"riskOfReoffending":false,"score":0,"oasysThreshold":{"standard":2}},
                {"section":"ALCOHOL_USE","name":"Alcohol use","needStatus":"NOT_IDENTIFIED_NEED","riskOfHarm":false,"riskOfReoffending":false,"score":0,"oasysThreshold":{"standard":2}},
                {"section":"FINANCE","name":"Finance","needStatus":"UNSCORED_NEED","riskOfHarm":false,"riskOfReoffending":false,"oasysThreshold":{}},
                {"section":"HEALTH_AND_WELLBEING","name":"Health and wellbeing","needStatus":"UNSCORED_NEED","oasysThreshold":{}}
              ],
              "assessmentVersion":"SAN",
              "assessedOn":"2024-12-20T10:00:00"
            }
          """.trimIndent(),
          checkNotNull(it.responseBody),
          JSONCompareMode.STRICT,
        )
      }
  }

  @Test
  fun `get risk management plans by crn`() {
    webTestClient.get().uri("/risks/risk-management-plan/$crn")
      .headers(setAuthorisation(roles = listOf("ROLE_ARNS__RISKS__RO")))
      .exchange()
      .expectStatus().isOk
      .expectBody<String>()
      .consumeWith {
        JSONAssert.assertEquals(
          """
            {
              "crn": "X123456",
              "limitedAccessOffender": false,
              "riskManagementPlan": [
                {"assessmentId":667025,"dateCompleted":"2020-03-26T12:47:17","initiationDate":"2020-03-26T12:38:57","assessmentStatus":"COMPLETE","assessmentType":"LAYER3"},
                {"assessmentId":668025,"dateCompleted":"2020-03-26T13:00:00","initiationDate":"2020-03-26T12:50:34","assessmentStatus":"COMPLETE","assessmentType":"LAYER3"},
                {"assessmentId":673025,"dateCompleted":"2020-04-03T11:42:01","initiationDate":"2020-04-03T11:33:00","assessmentStatus":"COMPLETE","assessmentType":"LAYER3"},
                {"assessmentId":674025,"dateCompleted":"2020-11-02T14:49:39","partcompStatus":"Unsigned","initiationDate":"2020-06-25T13:04:56","assessmentStatus":"LOCKED_INCOMPLETE","assessmentType":"LAYER3"},
                {
                  "assessmentId":676026,
                  "dateCompleted":"2020-11-05T10:56:37",
                  "initiationDate":"2020-11-02T14:50:02",
                  "assessmentStatus":"COMPLETE",
                  "assessmentType":"LAYER3",
                  "superStatus":"COMPLETE",
                  "keyInformationCurrentSituation":"Key considerations",
                  "furtherConsiderationsCurrentSituation":"Kelvin Brown is currently in the community having received a Adjourned - Other Report on the 01/01/2010 for 12 months\r\rThe end of their sentence is currently unknown. \r\rThey have no areas linked to harm. \r\rKelvin Brown has been assessed as medium risk to the public.\r\rKelvin Brown will have contact with a child on the protection register or in local authority care.\rThey are quite motivated to address offending behaviour.",
                  "monitoringAndControl":"3. Added measures for specific risks. Include here all activity aimed at addressing victim perspective and contact.",
                  "interventionsAndTreatment":"5. Additional conditions/requirements to manage the specific risks.",
                  "victimSafetyPlanning":"7. Contingency",
                  "laterWIPAssessmentExists":false,
                  "latestWIPDate":"2022-07-21T15:43:58",
                  "laterSignLockAssessmentExists":false,
                  "laterPartCompUnsignedAssessmentExists":false,
                  "latestPartCompUnsignedDate":"2022-05-31T10:37:05",
                  "laterPartCompSignedAssessmentExists":false,
                  "laterCompleteAssessmentExists":false,
                  "latestCompleteDate":"2022-07-21T15:43:12"
                }
              ]
            }
          """.trimIndent(),
          checkNotNull(it.responseBody),
          JSONCompareMode.STRICT,
        )
      }
  }

  @Test
  fun `should return versioned risk data for valid crn`() {
    val identifierType = "crn"
    val identifierValue = "X123456"

    webTestClient.get()
      .uri("/risks/predictors/unsafe/all/$identifierType/$identifierValue")
      .header("Content-Type", "application/json")
      .headers(setAuthorisation(user = "assess-risks-needs", roles = listOf("ROLE_ARNS__RISKS__RO")))
      .exchange()
      .expectStatus().isEqualTo(HttpStatus.OK)
      .expectBody<String>()
      .consumeWith {
        JSONAssert.assertEquals(
          """
            [
              {
                "completedDate":"2022-06-10T18:23:20",
                "status":"COMPLETE",
                "assessmentType":"LAYER3",
                "outputVersion":"1",
                "output":{
                  "groupReconvictionScore":{"oneYear":3,"twoYears":5,"scoreLevel":"LOW"},
                  "violencePredictorScore":{"ovpStaticWeightedScore":14,"ovpDynamicWeightedScore":3,"ovpTotalWeightedScore":17,"oneYear":4,"twoYears":7,"ovpRisk":"LOW"},
                  "generalPredictorScore":{"ogpStaticWeightedScore":3,"ogpDynamicWeightedScore":7,"ogpTotalWeightedScore":10,"ogp1Year":4,"ogp2Year":8,"ogpRisk":"LOW"},
                  "riskOfSeriousRecidivismScore":{"percentageScore":50.1234,"staticOrDynamic":"DYNAMIC","source":"OASYS","algorithmVersion":"5","scoreLevel":"MEDIUM"},
                  "sexualPredictorScore":{"ospIndecentPercentageScore":2.81,"ospContactPercentageScore":1.07,"ospIndecentScoreLevel":"MEDIUM","ospContactScoreLevel":"MEDIUM"}
                }
              },
              {
                "completedDate":"2022-04-27T12:46:39",
                "status":"COMPLETE",
                "assessmentType":"LAYER1",
                "outputVersion":"1",
                "output":{
                  "groupReconvictionScore":{},
                  "violencePredictorScore":{},
                  "generalPredictorScore":{},
                  "riskOfSeriousRecidivismScore":{"percentageScore":0.32,"staticOrDynamic":"STATIC","source":"OASYS","algorithmVersion":"3","scoreLevel":"LOW"},
                  "sexualPredictorScore":{}
                }
              },
              {
                "completedDate":"2022-06-09T15:16:21",
                "status":"COMPLETE",
                "assessmentType":"LAYER3",
                "outputVersion":"1",
                "output":{
                  "groupReconvictionScore":{"oneYear":6,"twoYears":12,"scoreLevel":"LOW"},
                  "violencePredictorScore":{"ovpStaticWeightedScore":22,"ovpDynamicWeightedScore":13,"ovpTotalWeightedScore":35,"oneYear":12,"twoYears":21,"ovpRisk":"LOW"},
                  "generalPredictorScore":{"ogpStaticWeightedScore":7,"ogpDynamicWeightedScore":4,"ogpTotalWeightedScore":11,"ogp1Year":5,"ogp2Year":8,"ogpRisk":"LOW"},
                  "riskOfSeriousRecidivismScore":{"percentageScore":4.12,"staticOrDynamic":"DYNAMIC","source":"OASYS","algorithmVersion":"3","scoreLevel":"MEDIUM"},
                  "sexualPredictorScore":{"ospIndecentPercentageScore":2.81,"ospContactPercentageScore":1.07,"ospIndecentScoreLevel":"MEDIUM","ospContactScoreLevel":"MEDIUM"}
                }
              },
              {
                "completedDate":"2022-06-11T18:23:20",
                "status":"COMPLETE",
                "assessmentType":"LAYER3",
                "outputVersion":"2",
                "output":{
                  "allReoffendingPredictor":{"staticOrDynamic":"DYNAMIC","score":4.56,"band":"MEDIUM"},
                  "violentReoffendingPredictor":{"staticOrDynamic":"DYNAMIC","score":4.56,"band":"MEDIUM"},
                  "seriousViolentReoffendingPredictor":{"staticOrDynamic":"DYNAMIC","score":4.56,"band":"MEDIUM"},
                  "directContactSexualReoffendingPredictor":{"score":2.81,"band":"MEDIUM"},
                  "indirectImageContactSexualReoffendingPredictor":{"score":1.07,"band":"MEDIUM"},
                  "combinedSeriousReoffendingPredictor":{"algorithmVersion":"6","staticOrDynamic":"DYNAMIC","score":50.1234,"band":"MEDIUM"}
                }
              },
              {
                "completedDate":"2022-06-12T18:23:20",
                "status":"COMPLETE",
                "assessmentType":"LAYER3",
                "outputVersion":"2",
                "output":{
                  "allReoffendingPredictor":{"staticOrDynamic":"STATIC","score":1.23,"band":"LOW"},
                  "violentReoffendingPredictor":{"staticOrDynamic":"STATIC","score":1.23,"band":"LOW"},
                  "seriousViolentReoffendingPredictor":{"staticOrDynamic":"STATIC","score":1.23,"band":"LOW"},
                  "directContactSexualReoffendingPredictor":{"score":2.81,"band":"MEDIUM"},
                  "indirectImageContactSexualReoffendingPredictor":{"score":1.07,"band":"MEDIUM"},
                  "combinedSeriousReoffendingPredictor":{"algorithmVersion":"6","staticOrDynamic":"STATIC","score":1.23,"band":"LOW"}
                }
              }
            ]
          """.trimIndent(),
          checkNotNull(it.responseBody),
          JSONCompareMode.STRICT,
        )
      }
  }

  @Test
  fun `should return not found error for invalid crn for versioned risk scores`() {
    webTestClient.get().uri("/risks/predictors/unsafe/all/crn/X999999")
      .headers(setAuthorisation(roles = listOf("ROLE_ARNS__RISKS__RO")))
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  fun `should return 400 bad request for invalid identifier type for versioned risk scores`() {
    val identifierType = "INVALID_IDENTIFIER_TYPE"
    val identifierValue = "X234567"
    webTestClient.get().uri("/risks/predictors/unsafe/all/$identifierType/$identifierValue")
      .headers(setAuthorisation(roles = listOf("ROLE_ARNS__RISKS__RO")))
      .exchange()
      .expectStatus().isBadRequest
  }

  @Test
  fun `should return tier risk data for valid crn`() {
    webTestClient.get()
      .uri("/risks/predictors/unsafe/tier/crn/X234567")
      .header("Content-Type", "application/json")
      .headers(setAuthorisation(user = "assess-risks-needs", roles = listOf("ROLE_ARNS__RISKS__RO")))
      .exchange()
      .expectStatus().isEqualTo(HttpStatus.OK)
      .expectBody<String>()
      .consumeWith {
        JSONAssert.assertEquals(
          """
            {
              "completedDate":"2024-12-19T16:57:25",
              "assessmentType":"LAYER1",
              "outputVersion":"2",
              "output":{
                "allReoffendingPredictor":{"staticOrDynamic":"DYNAMIC","score":26.88,"band":"LOW"},
                "violentReoffendingPredictor":{},
                "seriousViolentReoffendingPredictor":{},
                "directContactSexualReoffendingPredictor":{"score":2.81,"band":"MEDIUM"},
                "indirectImageContactSexualReoffendingPredictor":{},
                "combinedSeriousReoffendingPredictor":{"algorithmVersion":"6","staticOrDynamic":"DYNAMIC","score":0.93,"band":"LOW"}
              }
            }
          """.trimIndent(),
          checkNotNull(it.responseBody),
          JSONCompareMode.STRICT,
        )
      }
  }

  @Test
  fun `should return not found error for invalid crn for tier risk scores`() {
    webTestClient.get().uri("/risks/predictors/unsafe/tier/crn/NOT_FOUND")
      .headers(setAuthorisation(roles = listOf("ROLE_ARNS__RISKS__RO")))
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  fun `should return 400 bad request for invalid identifier type for tier risk scores`() {
    webTestClient.get().uri("/risks/predictors/unsafe/tier/INVALID_IDENTIFIER_TYPE/X234567")
      .headers(setAuthorisation(roles = listOf("ROLE_ARNS__RISKS__RO")))
      .exchange()
      .expectStatus().isBadRequest
  }
}
