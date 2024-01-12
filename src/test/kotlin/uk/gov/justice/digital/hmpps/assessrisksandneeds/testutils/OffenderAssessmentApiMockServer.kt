package uk.gov.justice.digital.hmpps.assessrisksandneeds.testutils

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.http.HttpHeader
import com.github.tomakehurst.wiremock.http.HttpHeaders

class OffenderAssessmentApiMockServer : WireMockServer(9004) {
  private val crn = "X123456"

  fun stubGetRSRPredictorScoring() {
    stubFor(
      WireMock.post(
        WireMock.urlEqualTo(
          "/offenders/risks/predictors/RSR",
        ),
      ).withRequestBody(
        WireMock.equalToJson(
          "{ " +
            "\"algorithmVersion\" : null," +
            "\"gender\" : \"M\"," +
            "\"dob\" : [ 2001, 1, 1 ]," +
            "\"assessmentDate\" : [ 2021, 1, 1, 0, 0 ]," +
            "\"currentOffence\" : {" +
            "    \"offenceCode\" : \"138\"," +
            "    \"offenceSubcode\" : \"00\"" +
            "}," +
            "\"dateOfFirstSanction\" : [ 2020, 1, 1 ]," +
            "\"ageAtFirstSanction\" : 19," +
            "\"totalOffences\" : 10," +
            "\"totalViolentOffences\" : 8," +
            "\"dateOfCurrentConviction\" : [ 2020, 12, 18 ]," +
            "\"hasAnySexualOffences\" : true," +
            "\"isCurrentSexualOffence\" : true," +
            "\"isCurrentOffenceVictimStranger\" : true," +
            "\"mostRecentSexualOffenceDate\" : [ 2020, 12, 11 ]," +
            "\"totalSexualOffencesInvolvingAnAdult\" : 5," +
            "\"totalSexualOffencesInvolvingAChild\" : 3," +
            "\"totalSexualOffencesInvolvingChildImages\" : 2," +
            "\"totalNonContactSexualOffences\" : 2," +
            "\"earliestReleaseDate\" : [ 2021, 11, 1 ]," +
            "\"dynamicScoringOffences\" : {" +
            "\"hasCompletedInterview\" : true," +
            "\"hasSuitableAccommodation\" : null," +
            "\"employment\": 0," +
            "\"currentRelationshipWithPartner\" : 2," +
            "\"evidenceOfDomesticViolence\" : true," +
            "\"isPerpetrator\" : true," +
            "\"alcoholUseIssues\" : 2," +
            "\"bingeDrinkingIssues\" : 2," +
            "\"impulsivityIssues\" : 1," +
            "\"temperControlIssues\" : 2," +
            "\"proCriminalAttitudes\" : 1," +
            "\"previousOffences\" : {" +
            "\"murderAttempt\" : true," +
            "\"wounding\" : true," +
            "\"aggravatedBurglary\" : true," +
            "\"arson\" : true," +
            "\"criminalDamage\" : true," +
            "\"kidnapping\" : true," +
            "\"firearmPossession\" : true," +
            "\"robbery\" : true," +
            "\"offencesWithWeapon\" : true" +
            "}," +
            "\"currentOffences\" : {" +
            "\"firearmPossession\" : true, " +
            "\"offencesWithWeapon\" : true " +
            "}" +
            "}" +
            "}",
          true,
          true,
        ),
      )
        .willReturn(
          WireMock.aResponse()
            .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/json")))
            .withBody(
              "{" +
                "    \"algorithmVersion\": 3," +
                "    \"rsrScore\": 11.34," +
                "    \"rsrBand\": \"High\"," +
                "    \"scoreType\": \"Static\"," +
                "    \"validRsrScore\": \"Y\"," +
                "    \"ospcScore\": 0," +
                "    \"ospcBand\": \"Not Applicable\"," +
                "    \"validOspcScore\": \"A\"," +
                "    \"ospiScore\": 0," +
                "    \"ospiBand\": \"Not Applicable\"," +
                "    \"validOspiScore\": \"A\"," +
                "    \"snsvScore\": 0.31," +
                "    \"errorCount\": 0," +
                "    \"calculationDateAndTime\": \"2021-07-30 16:10:02\"" +
                "}",
            ),
        ),
    )
  }

  fun stubGetLatestNeedsByCrn() {
    stubFor(
      WireMock.get(
        WireMock.urlEqualTo(
          "/assessments/crn/$crn/needs/latest?period=YEAR&periodUnits=1",
        ),
      )
        .willReturn(
          WireMock.aResponse()
            .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/json")))
            .withBody(needsJson),
        ),
    )
    stubFor(
      WireMock.get(
        WireMock.urlEqualTo(
          "/assessments/crn/NOT_FOUND/needs/latest?period=YEAR&periodUnits=1",
        ),
      )
        .willReturn(
          WireMock.aResponse()
            .withBody(crnNotFoundJson)
            .withStatus(404)
            .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/json"))),
        ),
    )

    stubFor(
      WireMock.get(
        WireMock.urlEqualTo(
          "/assessments/crn/NOT_CURRENT/needs/latest?period=YEAR&periodUnits=1",
        ),
      )
        .willReturn(
          WireMock.aResponse()
            .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/json")))
            .withBody(notCurrentNeedsJson)
            .withStatus(404),
        ),
    )

    stubFor(
      WireMock.get(
        WireMock.urlEqualTo(
          "/assessments/crn/NO_NEEDS/needs/latest?period=YEAR&periodUnits=1",
        ),
      )
        .willReturn(
          WireMock.aResponse()
            .withBody(emptyNeedsJson)
            .withStatus(404)
            .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/json"))),
        ),
    )
  }

  companion object {
    val crnNotFoundJson =
      """{ "status": 404 , "developerMessage": "Latest COMPLETE with types [LAYER_1, LAYER_3] type not found for crn, RANDOMCRN" }""".trimIndent()

    val assessmentJson =

      """{
    "assessmentId": 9479348,
    "sections": {
        "ROSH": [
            {
                "refQuestionCode": "R2.1",
                "questionText": "Is the offender, now or on release, likely to live with, or have frequent contact with, any child who is on the child protection register or is being looked after by the local authority.",
                "refAnswerCode": "YES",
                "staticText": "Yes"
            },
            {
                "refQuestionCode": "R2.2",
                "questionText": "Are there any concerns in relation to children",
                "refAnswerCode": "YES",
                "staticText": "Yes"
            },
            {
                "refQuestionCode": "R2.2.1",
                "questionText": "Should contact be made with social services",
                "refAnswerCode": "YES",
                "staticText": "Yes"
            },
            {
                "refQuestionCode": "R3.1",
                "questionText": "Risk of suicide",
                "refAnswerCode": "NO",
                "staticText": "No"
            },
            {
                "refQuestionCode": "R3.2",
                "questionText": "Risk of self-harm",
                "refAnswerCode": "DK",
                "staticText": "Don't Know"
            },
            {
                "refQuestionCode": "R3.3",
                "questionText": "Coping in custody / hostel setting",
                "refAnswerCode": "YES",
                "staticText": "Yes"
            },
            {
                "refQuestionCode": "R3.4",
                "questionText": "Vulnerability",
                "refAnswerCode": "YES",
                "staticText": "Yes"
            },
            {
                "refQuestionCode": "R4.1",
                "questionText": "Escape / abscond",
                "refAnswerCode": "YES",
                "staticText": "Yes"
            },
            {
                "refQuestionCode": "R4.2",
                "questionText": "Control issues / disruptive behaviour",
                "refAnswerCode": "YES",
                "staticText": "Yes"
            },
            {
                "refQuestionCode": "R4.3",
                "questionText": "Concerns in respect of breach of trust",
                "refAnswerCode": "DK",
                "staticText": "Don't know"
            },
            {
                "refQuestionCode": "R4.4",
                "questionText": "Risks to other prisoners",
                "refAnswerCode": "YES",
                "staticText": "Yes"
            }
        ],
        "ROSHFULL": [
            {
                "refQuestionCode": "FA1",
                "questionText": "Offence details",
                "freeFormText": "shjkhsdjhkjshfkjshdkfjhskjdhkjhkjhkj"
            },
            {
                "refQuestionCode": "FA16",
                "questionText": "b) is involved in a situation where there are identifiable children who are considered to be at risk from others",
                "refAnswerCode": "YES",
                "staticText": "Yes"
            },
            {
                "refQuestionCode": "FA2",
                "questionText": "Where and when did s/he do it",
                "freeFormText": "klsdjkljlksd"
            },
            {
                "refQuestionCode": "FA3",
                "questionText": "How did s/he do it (was there any pre planning, use of weapon, tool etc)",
                "freeFormText": "sdlljsd;ljf;lds"
            },
            {
                "refQuestionCode": "FA31",
                "questionText": "Are there any current concerns about suicide",
                "refAnswerCode": "YES",
                "staticText": "Yes"
            },
            {
                "refQuestionCode": "FA33",
                "questionText": "Describe circumstances, relevant issues and needs regarding current concerns (refer to sections 1-12 for indicators, particularly Section 10)",
                "freeFormText": "Suicide and/or Self-harm current concerns"
            },
            {
                "refQuestionCode": "FA34",
                "questionText": "Is there a current ACCT (Assessment, Care in Custody and Teamwork?)",
                "refAnswerCode": "YES",
                "staticText": "Yes"
            },
            {
                "refQuestionCode": "FA36",
                "questionText": "Have there been any concerns about suicide in the past",
                "refAnswerCode": "YES",
                "staticText": "Yes"
            },
            {
                "refQuestionCode": "FA39",
                "questionText": "Are there any current concerns about coping in custody",
                "refAnswerCode": "N/A",
                "staticText": "N/A"
            },
            {
                "refQuestionCode": "FA4",
                "questionText": "Who were the victims (are there concerns about targeting, type, age, race of vulnerability or victim)",
                "freeFormText": "sdflkl;sdkjf;l"
            },
            {
                "refQuestionCode": "FA40",
                "questionText": "Are there any current concerns about coping in hostel settings",
                "refAnswerCode": "NO",
                "staticText": "No"
            },
            {
                "refQuestionCode": "FA41",
                "questionText": "Describe circumstances, relevant issues and needs",
                "freeFormText": "Coping in custody / hostel setting current concerns"
            },
            {
                "refQuestionCode": "FA42",
                "questionText": "Are there any previous concerns about coping in custody",
                "refAnswerCode": "YES",
                "staticText": "Yes"
            },
            {
                "refQuestionCode": "FA43",
                "questionText": "Are there any previous concerns about coping in hostel settings",
                "refAnswerCode": "DK",
                "staticText": "Don't know"
            },
            {
                "refQuestionCode": "FA44",
                "questionText": "Describe circumstances, relevant issues and needs",
                "freeFormText": "Coping in custody / hostel setting previous concerns"
            },
            {
                "refQuestionCode": "FA45",
                "questionText": "Are there any current concerns about vulnerability (eg victimisation, being bullied, assaulted, exploited)",
                "refAnswerCode": "YES",
                "staticText": "Yes"
            },
            {
                "refQuestionCode": "FA45.t",
                "questionText": "Describe circumstances, relevant issues and needs",
                "freeFormText": "Vulnerability current concerns free text"
            },
            {
                "refQuestionCode": "FA47",
                "questionText": "Have there been any previous concerns about vulnerability (eg victimisation, being bullied, assaulted, exploited)",
                "refAnswerCode": "YES",
                "staticText": "Yes"
            },
            {
                "refQuestionCode": "FA47.t",
                "questionText": "Describe circumstances, relevant issues and needs",
                "freeFormText": "Vulnerability previous concerns free text"
            },
            {
                "refQuestionCode": "FA49",
                "questionText": " Do any of the above (R8.1 - 8.3) indicate a risk of serious harm to others.  If YES complete the risk of serious harm analysis, R6",
                "refAnswerCode": "YES",
                "staticText": "Yes"
            },
            {
                "refQuestionCode": "FA49.t",
                "questionText": "Describe circumstances, relevant issues and needs",
                "freeFormText": "Risk of serious harm free text"
            },
            {
                "refQuestionCode": "FA5",
                "questionText": "Was anyone else present / involved",
                "freeFormText": "sflk;ljkf;lds"
            },
            {
                "refQuestionCode": "FA6",
                "questionText": "Why did s/he do it (motivation and triggers)",
                "freeFormText": "sfkjlj"
            },
            {
                "refQuestionCode": "FA7",
                "questionText": "Sources of information",
                "freeFormText": "fshjkjhk"
            }
        ],
        "ROSHSUM": [
            {
                "refQuestionCode": "SUM1",
                "questionText": "Who is at risk",
                "freeFormText": "whoisAtRisk"
            },
            {
                "refQuestionCode": "SUM2",
                "questionText": "What is the nature of the risk",
                "freeFormText": "natureOfRisk"
            },
            {
                "refQuestionCode": "SUM3",
                "questionText": "When is the risk likely to be greatest\nConsider the timescale and indicate whether risk is immediate or not.  Consider the risks in custody as well as on release.\n",
                "freeFormText": "riskImminence"
            },
            {
                "refQuestionCode": "SUM4",
                "questionText": "What circumstances are likely to increase risk\nDescribe factors, actions, events which might increase level of risk, now and in the future\n",
                "freeFormText": "riskIncreaseFactors"
            },
            {
                "refQuestionCode": "SUM5",
                "questionText": "What factors are likely to reduce the risk\nDescribe factors, actions, and events which may reduce or contain the level of risk. What has previously stopped him / her?  ",
                "freeFormText": "riskMitigationFactors"
            },
            {
                "refQuestionCode": "SUM6.1.1",
                "questionText": "Risk in Community",
                "refAnswerCode": "L",
                "staticText": "Low"
            },
            {
                "refQuestionCode": "SUM6.1.2",
                "questionText": "Risk in Custody",
                "refAnswerCode": "L",
                "staticText": "Low"
            },
            {
                "refQuestionCode": "SUM6.2.1",
                "questionText": "Risk in Community",
                "refAnswerCode": "M",
                "staticText": "Medium"
            },
            {
                "refQuestionCode": "SUM6.2.2",
                "questionText": "Risk in Custody",
                "refAnswerCode": "L",
                "staticText": "Low"
            },
            {
                "refQuestionCode": "SUM6.3.1",
                "questionText": "Risk in Community",
                "refAnswerCode": "L",
                "staticText": "Low"
            },
            {
                "refQuestionCode": "SUM6.3.2",
                "questionText": "Risk in Custody",
                "refAnswerCode": "L",
                "staticText": "Low"
            },
            {
                "refQuestionCode": "SUM6.4.1",
                "questionText": "Risk in Community",
                "refAnswerCode": "H",
                "staticText": "High"
            },
            {
                "refQuestionCode": "SUM6.4.2",
                "questionText": "Risk in Custody",
                "refAnswerCode": "V",
                "staticText": "Very High"
            },
            {
                "refQuestionCode": "SUM6.5.2",
                "questionText": "Risk in Custody",
                "refAnswerCode": "H",
                "staticText": "High"
            },
            {
                "refQuestionCode": "SUM8",
                "questionText": "If necessary record the details of any key documents or reports used in this analysis:",
                "freeFormText": "zccx"
            }
        ]
    },
    "assessedOn": "2021-06-21T15:55:04"
}
      """.trimIndent()
  }

  private val needsJson = """
    {
    "needs": [
      {
          "section": "ACCOMMODATION",
          "name": "Accommodation",
          "overThreshold": false,
          "riskOfHarm": true,
          "riskOfReoffending": true,
          "flaggedAsNeed": false,
          "severity": "SEVERE",
          "identifiedAsNeed": true,
          "needScore" : 2
      },
      {
          "section": "EDUCATION_TRAINING_AND_EMPLOYABILITY",
          "name": "4 - Education, Training and Employability",
          "overThreshold": false,
          "riskOfHarm": true,
          "riskOfReoffending": true,
          "flaggedAsNeed": false,
          "severity": "SEVERE",
          "identifiedAsNeed": true,
          "needScore" : 2
      },
      {
          "section": "FINANCIAL_MANAGEMENT_AND_INCOME",
          "name": "Financial Management and Income",
          "overThreshold": false,
          "riskOfHarm": false,
          "riskOfReoffending": false,
          "flaggedAsNeed": false,
          "severity": "NO_NEED",
          "identifiedAsNeed": false
      },
      {
          "section": "RELATIONSHIPS",
          "name": "Relationships",
          "overThreshold": false,
          "riskOfHarm": true,
          "riskOfReoffending": true,
          "flaggedAsNeed": false,
          "severity": "SEVERE",
          "identifiedAsNeed": true,
          "needScore" : 2
      },
      {
          "section": "LIFESTYLE_AND_ASSOCIATES",
          "name": "Lifestyle and Associates",
          "overThreshold": false,
          "riskOfHarm": true,
          "riskOfReoffending": true,
          "flaggedAsNeed": false,
          "severity": "SEVERE",
          "identifiedAsNeed": true,
          "needScore" : 2
      },
      {
          "section": "DRUG_MISUSE",
          "name": "Drug Misuse",
          "overThreshold": false,
          "riskOfHarm": true,
          "riskOfReoffending": true,
          "flaggedAsNeed": false,
          "severity": "SEVERE",
          "identifiedAsNeed": true,
          "needScore" : 2
      },
      {
          "section": "ALCOHOL_MISUSE",
          "name": "Alcohol Misuse",
          "overThreshold": false,
          "riskOfHarm": true,
          "riskOfReoffending": true,
          "flaggedAsNeed": false,
          "severity": "SEVERE",
          "identifiedAsNeed": true,
          "needScore" : 2
      },
      {
          "section": "EMOTIONAL_WELL_BEING",
          "name": "Emotional Well-Being",
          "overThreshold": false,
          "riskOfHarm": false,
          "riskOfReoffending": false,
          "flaggedAsNeed": false,
          "severity": "NO_NEED",
          "identifiedAsNeed": false
      }
    ],
    "assessedOn": "2021-06-21T15:55:04",
    "historicStatus": "CURRENT"
  } """

  private val notCurrentNeedsJson = """
    {
    "needs": [
      {
          "section": "ACCOMMODATION",
          "name": "Accommodation",
          "overThreshold": false,
          "riskOfHarm": true,
          "riskOfReoffending": true,
          "flaggedAsNeed": false,
          "severity": "SEVERE",
          "identifiedAsNeed": true,
          "needScore" : 2
      },
      {
          "section": "EMOTIONAL_WELL_BEING",
          "name": "Emotional Well-Being",
          "overThreshold": false,
          "riskOfHarm": false,
          "riskOfReoffending": false,
          "flaggedAsNeed": false,
          "severity": "NO_NEED",
          "identifiedAsNeed": false,
          "needScore" : 0
      }
    ],
    "assessedOn": "2021-06-21T15:55:04",
    "historicStatus": "HISTORIC"
  } """

  private val emptyNeedsJson = """
      {
      "needs": [],
      "assessedOn": "2021-06-21T15:55:04",
      "historicStatus": "CURRENT"
    } """

  private val assessmentJsonMissingRoshData =

    """{
    "assessmentId": 9479348,
    "sections": {
        "ROSH": [
            {
                "refQuestionCode": "R2.1",
                "questionText": "Is the offender, now or on release, likely to live with, or have frequent contact with, any child who is on the child protection register or is being looked after by the local authority.",
                "refAnswerCode": "YES",
                "staticText": "Yes"
            },
            {
                "refQuestionCode": "R2.2",
                "questionText": "Are there any concerns in relation to children",
                "refAnswerCode": "YES",
                "staticText": "Yes"
            },
            {
                "refQuestionCode": "R2.2.1",
                "questionText": "Should contact be made with social services",
                "refAnswerCode": "YES",
                "staticText": "Yes"
            },
            {
                "refQuestionCode": "R3.1",
                "questionText": "Risk of suicide",
                "refAnswerCode": "NO",
                "staticText": "No"
            },
            {
                "refQuestionCode": "R3.2",
                "questionText": "Risk of self-harm",
                "refAnswerCode": "YES",
                "staticText": "Yes"
            },
            {
                "refQuestionCode": "R3.3",
                "questionText": "Coping in custody / hostel setting",
                "refAnswerCode": "YES",
                "staticText": "Yes"
            },
            {
                "refQuestionCode": "R3.4",
                "questionText": "Vulnerability",
                "refAnswerCode": "YES",
                "staticText": "Yes"
            },
            {
                "refQuestionCode": "R4.1",
                "questionText": "Escape / abscond",
                "refAnswerCode": "YES",
                "staticText": "Yes"
            },
            {
                "refQuestionCode": "R4.2",
                "questionText": "Control issues / disruptive behaviour",
                "refAnswerCode": "YES",
                "staticText": "Yes"
            },
            {
                "refQuestionCode": "R4.3",
                "questionText": "Concerns in respect of breach of trust",
                "refAnswerCode": "DK",
                "staticText": "Don't know"
            },
            {
                "refQuestionCode": "R4.4",
                "questionText": "Risks to other prisoners",
                "refAnswerCode": "YES",
                "staticText": "Yes"
            }
        ],
        "ROSHFULL": [
            {
                "refQuestionCode": "FA1",
                "questionText": "Offence details",
                "freeFormText": "shjkhsdjhkjshfkjshdkfjhskjdhkjhkjhkj"
            },
            {
                "refQuestionCode": "FA16",
                "questionText": "b) is involved in a situation where there are identifiable children who are considered to be at risk from others",
                "refAnswerCode": "YES",
                "staticText": "Yes"
            },
            {
                "refQuestionCode": "FA2",
                "questionText": "Where and when did s/he do it",
                "freeFormText": "klsdjkljlksd"
            },
            {
                "refQuestionCode": "FA3",
                "questionText": "How did s/he do it (was there any pre planning, use of weapon, tool etc)",
                "freeFormText": "sdlljsd;ljf;lds"
            },
            {
                "refQuestionCode": "FA31",
                "questionText": "Are there any current concerns about suicide",
                "refAnswerCode": "YES",
                "staticText": "Yes"
            },
            {
                "refQuestionCode": "FA32",
                "questionText": "Are there any current concerns about self-harm",
                "refAnswerCode": "YES",
                "staticText": "Yes"
            },
            {
                "refQuestionCode": "FA33",
                "questionText": "Describe circumstances, relevant issues and needs regarding current concerns (refer to sections 1-12 for indicators, particularly Section 10)",
                "freeFormText": "Suicide and/or Self-harm current concerns"
            },
            {
                "refQuestionCode": "FA34",
                "questionText": "Is there a current ACCT (Assessment, Care in Custody and Teamwork?)",
                "refAnswerCode": "YES",
                "staticText": "Yes"
            },
            {
                "refQuestionCode": "FA36",
                "questionText": "Have there been any concerns about suicide in the past",
                "refAnswerCode": "YES",
                "staticText": "Yes"
            },
            {
                "refQuestionCode": "FA37",
                "questionText": "Have there been any concerns about self-harm in the past",
                "refAnswerCode": "YES",
                "staticText": "Yes"
            },
            {
                "refQuestionCode": "FA39",
                "questionText": "Are there any current concerns about coping in custody",
                "refAnswerCode": "YES",
                "staticText": "Yes"
            },
            {
                "refQuestionCode": "FA4",
                "questionText": "Who were the victims (are there concerns about targeting, type, age, race of vulnerability or victim)",
                "freeFormText": "sdflkl;sdkjf;l"
            },
            {
                "refQuestionCode": "FA40",
                "questionText": "Are there any current concerns about coping in hostel settings",
                "refAnswerCode": "NO",
                "staticText": "No"
            },
            {
                "refQuestionCode": "FA41",
                "questionText": "Describe circumstances, relevant issues and needs",
                "freeFormText": "Coping in custody / hostel setting current concerns"
            },
            {
                "refQuestionCode": "FA42",
                "questionText": "Are there any previous concerns about coping in custody",
                "refAnswerCode": "YES",
                "staticText": "Yes"
            },
            {
                "refQuestionCode": "FA43",
                "questionText": "Are there any previous concerns about coping in hostel settings",
                "refAnswerCode": "NO",
                "staticText": "No"
            },
            {
                "refQuestionCode": "FA44",
                "questionText": "Describe circumstances, relevant issues and needs",
                "freeFormText": "Coping in custody / hostel setting previous concerns"
            },
            {
                "refQuestionCode": "FA45",
                "questionText": "Are there any current concerns about vulnerability (eg victimisation, being bullied, assaulted, exploited)",
                "refAnswerCode": "YES",
                "staticText": "Yes"
            },
            {
                "refQuestionCode": "FA45.t",
                "questionText": "Describe circumstances, relevant issues and needs",
                "freeFormText": "Vulnerability current concerns free text"
            },
            {
                "refQuestionCode": "FA47",
                "questionText": "Have there been any previous concerns about vulnerability (eg victimisation, being bullied, assaulted, exploited)",
                "refAnswerCode": "YES",
                "staticText": "Yes"
            },
            {
                "refQuestionCode": "FA47.t",
                "questionText": "Describe circumstances, relevant issues and needs",
                "freeFormText": "Vulnerability previous concerns free text"
            },
            {
                "refQuestionCode": "FA49",
                "questionText": " Do any of the above (R8.1 - 8.3) indicate a risk of serious harm to others.  If YES complete the risk of serious harm analysis, R6",
                "refAnswerCode": "YES",
                "staticText": "Yes"
            },
            {
                "refQuestionCode": "FA49.t",
                "questionText": "Describe circumstances, relevant issues and needs",
                "freeFormText": "Risk of serious harm free text"
            },
            {
                "refQuestionCode": "FA5",
                "questionText": "Was anyone else present / involved",
                "freeFormText": "sflk;ljkf;lds"
            },
            {
                "refQuestionCode": "FA6",
                "questionText": "Why did s/he do it (motivation and triggers)",
                "freeFormText": "sfkjlj"
            },
            {
                "refQuestionCode": "FA7",
                "questionText": "Sources of information",
                "freeFormText": "fshjkjhk"
            }
        ],
        "ROSHSUM": [
            {
                "refQuestionCode": "SUM1",
                "questionText": "Who is at risk",
                "freeFormText": "whoisAtRisk"
            },
            {
                "refQuestionCode": "SUM2",
                "questionText": "What is the nature of the risk",
                "freeFormText": "natureOfRisk"
            },
            {
                "refQuestionCode": "SUM3",
                "questionText": "When is the risk likely to be greatest\nConsider the timescale and indicate whether risk is immediate or not.  Consider the risks in custody as well as on release.\n",
                "freeFormText": "riskImminence"
            },
            {
                "refQuestionCode": "SUM4",
                "questionText": "What circumstances are likely to increase risk\nDescribe factors, actions, events which might increase level of risk, now and in the future\n",
                "freeFormText": "riskIncreaseFactors"
            },
            {
                "refQuestionCode": "SUM5",
                "questionText": "What factors are likely to reduce the risk\nDescribe factors, actions, and events which may reduce or contain the level of risk. What has previously stopped him / her?  ",
                "freeFormText": "riskMitigationFactors"
            },
            {
                "refQuestionCode": "SUM6.2.1",
                "questionText": "Risk in Community",
                "refAnswerCode": "M",
                "staticText": "Medium"
            },
            {
                "refQuestionCode": "SUM6.2.2",
                "questionText": "Risk in Custody",
                "refAnswerCode": "L",
                "staticText": "Low"
            },
            {
                "refQuestionCode": "SUM6.3.1",
                "questionText": "Risk in Community",
                "refAnswerCode": "L",
                "staticText": "Low"
            },
            {
                "refQuestionCode": "SUM6.3.2",
                "questionText": "Risk in Custody",
                "refAnswerCode": "L",
                "staticText": "Low"
            },
            {
                "refQuestionCode": "SUM8",
                "questionText": "If necessary record the details of any key documents or reports used in this analysis:",
                "freeFormText": "zccx"
            }
        ]
    },
    "assessedOn": "2021-06-21T15:55:04"
}
    """.trimIndent()
}
