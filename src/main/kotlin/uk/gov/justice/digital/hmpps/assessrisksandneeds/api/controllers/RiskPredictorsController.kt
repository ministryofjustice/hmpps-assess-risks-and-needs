package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AllPredictorVersioned
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AllPredictorVersionedDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AllPredictorVersionedLegacyDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.IdentifierType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RiskScoresDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RsrPredictorDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RsrPredictorVersioned
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.RiskPredictorService

@RestController
class RiskPredictorsController(private val riskPredictorService: RiskPredictorService) {
  @Deprecated("Use /risks/predictors/rsr/{identifierType}/{identifierValue}. This endpoint will be removed in a future release.")
  @RequestMapping(path = ["/risks/crn/{crn}/predictors/rsr/history"], method = [RequestMethod.GET])
  @Operation(
    description = """
    Gets RSR score history for a CRN
    Deprecated endpoint.
    Please use /risks/predictors/rsr/{identifierType}/{identifierValue} instead.
    This endpoint will be removed in a future release.
    """,
    deprecated = true,
  )
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "403", description = "Unauthorized"),
      ApiResponse(responseCode = "200", description = "OK"),
    ],
  )
  @PreAuthorize("hasAnyRole('ROLE_PROBATION')")
  fun getRsrScoresByCrn(
    @Parameter(description = "CRN", required = true)
    @PathVariable
    crn: String,
  ): List<RsrPredictorDto> {
    log.info("Retrieving RSR score history for crn: $crn")
    return riskPredictorService.getAllRsrHistory(crn)
  }

  @RequestMapping(path = ["/risks/predictors/rsr/{identifierType}/{identifierValue}"], method = [RequestMethod.GET])
  @Operation(
    description = """# Gets Combined Serious Reoffending Predictor scores for an identifier type (e.g. CRN)

Returns a list of assessments containing Combined Serious Reoffending Predictor scores (previously known as RSR scores).  
Assessments within the list will have predictor scores in **either legacy** (OGRS3 generation) **or new** (OGRS4 generation) format.  

## Determining the predictor score format
Each assessment contains a top level `outputVersion` field which dictates the format of the predictors nested within the `output` field.  

### Legacy risk predictor score format (outputVersion = 1)
```json
[
  {
    "completedDate": "2025-10-23T03:02:59",
    "source": "OASYS",
    "status": "COMPLETE",
    "outputVersion": "1",
    "output": {
      "staticOrDynamic": "STATIC",
      "algorithmVersion": "string",
      "rsrPercentageScore": 0,
      "rsrScoreLevel": "LOW",
      "ospcPercentageScore": 0,
      "ospcScoreLevel": "LOW",
      "ospiPercentageScore": 0,
      "ospiScoreLevel": "LOW",
      "ospiiPercentageScore": 0,
      "ospdcPercentageScore": 0,
      "ospiiScoreLevel": "LOW",
      "ospdcScoreLevel": "LOW"
    }
  }
]
```

### New risk predictor score format (outputVersion = 2)
```json
[
  {
    "completedDate": "2025-10-23T03:02:59",
    "source": "OASYS",
    "status": "COMPLETE",
    "outputVersion": "2",
    "output": {
      "seriousViolentReoffendingPredictor": {
        "staticOrDynamic": "STATIC",
        "score": 10,
        "band": "LOW"
      },
      "directContactSexualReoffendingPredictor": {
        "score": 10,
        "band": "LOW"
      },
      "indirectImageContactSexualReoffendingPredictor": {
        "score": 10,
        "band": "LOW"
      },
      "combinedSeriousReoffendingPredictor": {
        "algorithmVersion": "string",
        "staticOrDynamic": "STATIC",
        "score": 0,
        "band": "LOW"
      }
    }
  }
]
```

Please see the associated documentation for further information: [OGRS4 ARNS API Change Specification - RSR Risk Predictors](https://dsdmoj.atlassian.net/wiki/spaces/ARN/pages/5962203966/OGRS4+ARNS+API+Change+Specification#Endpoint-%233-Changes---RSR-Risk-Predictors).
  """,
  )
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "403", description = "Unauthorized"),
      ApiResponse(
        responseCode = "200", description = "OK",
        content = [
          Content(
            mediaType = "application/json",
            examples = [
              ExampleObject(
                name = "List of assessments containing both Legacy RSR and New Combined Serious Reoffending Predictor score formats.",
                summary = "RSR & Combined Serious Reoffending Predictor Assessments",
                value = """
[
  {
    "completedDate": "2025-10-23T03:02:59",
    "source": "OASYS",
    "status": "COMPLETE",
    "outputVersion": "1",
    "output": {
      "staticOrDynamic": "STATIC",
      "algorithmVersion": "string",
      "rsrPercentageScore": 10,
      "rsrScoreLevel": "LOW",
      "ospcPercentageScore": 10,
      "ospcScoreLevel": "LOW",
      "ospiPercentageScore": 10,
      "ospiScoreLevel": "LOW",
      "ospiiPercentageScore": 10,
      "ospdcPercentageScore": 10,
      "ospiiScoreLevel": "LOW",
      "ospdcScoreLevel": "LOW"
    }
  },
  {
    "completedDate": "2025-10-23T03:02:59",
    "source": "OASYS",
    "status": "COMPLETE",
    "outputVersion": "2",
    "output": {
      "seriousViolentReoffendingPredictor": {
        "staticOrDynamic": "STATIC",
        "score": 10,
        "band": "LOW"
      },
      "directContactSexualReoffendingPredictor": {
        "score": 10,
        "band": "LOW"
      },
      "indirectImageContactSexualReoffendingPredictor": {
        "score": 10,
        "band": "LOW"
      },
      "combinedSeriousReoffendingPredictor": {
        "algorithmVersion": "string",
        "staticOrDynamic": "STATIC",
        "score": 10,
        "band": "LOW"
      }
    }
  }
]
              """,
              ),
            ],
          ),
        ],
      ),
    ],
  )
  @PreAuthorize("hasAnyRole('ROLE_PROBATION')")
  fun getRsrScoresByIdentifierType(
    @Parameter(description = "Identifier type (e.g. crn)", required = true)
    @PathVariable
    identifierType: IdentifierType,
    @Parameter(description = "Identifier Value", required = true)
    @PathVariable
    identifierValue: String,
  ): List<RsrPredictorVersioned<Any>> = riskPredictorService.getAllRsrScores(identifierType, identifierValue)

  @Deprecated("Use /risks/predictors/all/{identifierType}/{identifierValue}. This endpoint will be removed in a future release.")
  @RequestMapping(path = ["/risks/crn/{crn}/predictors/all"], method = [RequestMethod.GET])
  @Operation(
    description = """
    Gets risk predictors scores for all latest completed assessments from the last 1 year
    Deprecated endpoint.
    Please use /risks/predictors/all/{identifierType}/{identifierValue} instead.
    This endpoint will be removed in a future release.
    """,
    deprecated = true,
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "403",
        description = "User does not have permission to access offender with provided CRN",
      ),
      ApiResponse(responseCode = "404", description = "Risk data does not exist for CRN"),
      ApiResponse(responseCode = "404", description = "Offender does not exist in Delius for provided CRN"),
      ApiResponse(responseCode = "404", description = "User does not exist in Delius for provided user name"),
      ApiResponse(responseCode = "401", description = "Unauthorised"),
      ApiResponse(responseCode = "200", description = "OK"),
    ],
  )
  @PreAuthorize("hasAnyRole('ROLE_PROBATION', 'ROLE_RISK_RESETTLEMENT_PASSPORT_RO', 'ROLE_RISK_INTEGRATIONS_RO', 'ROLE_ACCREDITED_PROGRAMS_RO', 'ROLE_ARNS__MANAGE_PEOPLE_ON_PROBATION__RO')")
  fun getAllRiskScores(@PathVariable crn: String): List<RiskScoresDto> {
    log.info("Entered getAllRiskScores for crn: $crn")
    return riskPredictorService.getAllRiskScores(crn)
  }

  @RequestMapping(path = ["/risks/predictors/all/{identifierType}/{identifierValue}"], method = [RequestMethod.GET])
  @Operation(
    description = """# Gets all risk predictor scores for completed assessments for an identifier type (e.g. CRN)

Returns a list of completed assessments containing all predictor scores.  
Assessments within the list will have predictor scores in **either legacy** (OGRS3 generation) **or new** (OGRS4 generation) format.  

## Determining the predictor score format
Each assessment contains a top level `outputVersion` field which dictates the format of the predictors nested within the `output` field.  

### Legacy risk predictor score format (outputVersion = 1)
```json
[
  {
    "completedDate": "2025-10-23T03:02:59",
    "source": "OASYS",
    "status": "COMPLETE",
    "outputVersion": "1",
    "output": {
      "groupReconvictionScore": {
        "oneYear": 0,
        "twoYears": 0,
        "scoreLevel": "LOW"
      },
      "violencePredictorScore": {
        "ovpStaticWeightedScore": 0,
        "ovpDynamicWeightedScore": 0,
        "ovpTotalWeightedScore": 0,
        "oneYear": 0,
        "twoYears": 0,
        "ovpRisk": "LOW"
      },
      "generalPredictorScore": {
        "ogpStaticWeightedScore": 0,
        "ogpDynamicWeightedScore": 0,
        "ogpTotalWeightedScore": 0,
        "ogp1Year": 0,
        "ogp2Year": 0,
        "ogpRisk": "LOW"
      },
      "riskOfSeriousRecidivismScore": {
        "percentageScore": 0,
        "staticOrDynamic": "STATIC",
        "source": "OASYS",
        "algorithmVersion": "string",
        "scoreLevel": "LOW"
      },
      "sexualPredictorScore": {
        "ospIndecentPercentageScore": 0,
        "ospContactPercentageScore": 0,
        "ospIndecentScoreLevel": "LOW",
        "ospContactScoreLevel": "LOW",
        "ospIndirectImagePercentageScore": 0,
        "ospDirectContactPercentageScore": 0,
        "ospIndirectImageScoreLevel": "LOW",
        "ospDirectContactScoreLevel": "LOW"
      }
    }
  }
]
```

### New risk predictor score format (outputVersion = 2)
```json
[
  {
    "completedDate": "2025-10-23T03:02:59",
    "source": "OASYS",
    "status": "COMPLETE",
    "outputVersion": "2",
    "output": {
      "allReoffendingPredictor": {
        "staticOrDynamic": "STATIC",
        "score": 1,
        "band": "LOW"
      },
      "violentReoffendingPredictor": {
        "staticOrDynamic": "DYNAMIC",
        "score": 30,
        "band": "MEDIUM"
      },
      "seriousViolentReoffendingPredictor": {
        "staticOrDynamic": "STATIC",
        "score": 99,
        "band": "HIGH"
      },
      "directContactSexualReoffendingPredictor": {
        "score": 10,
        "band": "LOW"
      },
      "indirectImageContactSexualReoffendingPredictor": {
        "score": 10,
        "band": "LOW"
      },
      "combinedSeriousReoffendingPredictor": {
        "algorithmVersion": "string",
        "staticOrDynamic": "STATIC",
        "score": 0,
        "band": "LOW"
      }
    }
  }
]
```

Please see the associated documentation for further information: [OGRS4 ARNS API Change Specification - All Risk Predictors](https://dsdmoj.atlassian.net/wiki/spaces/ARN/pages/5962203966/OGRS4+ARNS+API+Change+Specification#Endpoint-%232%2F%234-Changes---All-Risk-Predictors-by-CRN-or-assessment-id).
  """,
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "403",
        description = "User does not have permission to access offender with provided CRN",
      ),
      ApiResponse(responseCode = "404", description = "Risk data does not exist for CRN"),
      ApiResponse(responseCode = "404", description = "Offender does not exist in Delius for provided CRN"),
      ApiResponse(responseCode = "404", description = "User does not exist in Delius for provided user name"),
      ApiResponse(responseCode = "401", description = "Unauthorised"),
      ApiResponse(responseCode = "400", description = "Bad request"),
      ApiResponse(
        responseCode = "200", description = "OK",
        content = [
          Content(
            mediaType = "application/json",
            examples = [
              ExampleObject(
                name = "List of completed assessments containing both Legacy and New predictor score formats.",
                summary = "Completed assessments and associated risk predictor scores",
                value = """
[
  {
    "completedDate": "2025-01-01",
    "status": "COMPLETE",
    "outputVersion": "1",
    "output": {
      "groupReconvictionScore": {
        "oneYear": 0,
        "twoYears": 0,
        "scoreLevel": "LOW"
      },
      "violencePredictorScore": {
        "ovpStaticWeightedScore": 0,
        "ovpDynamicWeightedScore": 0,
        "ovpTotalWeightedScore": 0,
        "oneYear": 0,
        "twoYears": 0,
        "ovpRisk": "LOW"
      },
      "generalPredictorScore": {
        "ogpStaticWeightedScore": 0,
        "ogpDynamicWeightedScore": 0,
        "ogpTotalWeightedScore": 0,
        "ogp1Year": 0,
        "ogp2Year": 0,
        "ogpRisk": "LOW"
      },
      "riskOfSeriousRecidivismScore": {
        "percentageScore": 0,
        "staticOrDynamic": "STATIC",
        "source": "OASYS",
        "algorithmVersion": "string",
        "scoreLevel": "LOW"
      },
      "sexualPredictorScore": {
        "ospIndecentPercentageScore": 0,
        "ospContactPercentageScore": 0,
        "ospIndecentScoreLevel": "LOW",
        "ospContactScoreLevel": "LOW",
        "ospIndirectImagePercentageScore": 0,
        "ospDirectContactPercentageScore": 0,
        "ospIndirectImageScoreLevel": "LOW",
        "ospDirectContactScoreLevel": "LOW"
      }
    }
  },
  {
    "completedDate": "2026-03-01",
    "status": "COMPLETE",
    "outputVersion": "2",
    "output": {
      "allReoffendingPredictor": {
        "staticOrDynamic": "STATIC",
        "score": 1,
        "band": "LOW"
      },
      "violentReoffendingPredictor": {
        "staticOrDynamic": "DYNAMIC",
        "score": 30,
        "band": "MEDIUM"
      },
      "seriousViolentReoffendingPredictor": {
        "staticOrDynamic": "STATIC",
        "score": 99,
        "band": "HIGH"
      },
      "directContactSexualReoffendingPredictor": {
        "score": 10,
        "band": "LOW"
      },
      "indirectImageContactSexualReoffendingPredictor": {
        "score": 10,
        "band": "LOW"
      },
      "combinedSeriousReoffendingPredictor": {
        "algorithmVersion": "string",
        "staticOrDynamic": "STATIC",
        "score": 0,
        "band": "LOW"
      }
    }
  }
]
              """,
              ),
            ],
          ),
        ],
      ),
    ],
  )
  @PreAuthorize("hasAnyRole('ROLE_PROBATION', 'ROLE_RISK_RESETTLEMENT_PASSPORT_RO', 'ROLE_RISK_INTEGRATIONS_RO', 'ROLE_ACCREDITED_PROGRAMS_RO', 'ROLE_ARNS__MANAGE_PEOPLE_ON_PROBATION__RO')")
  fun getAllRiskScoresVersioned(
    @Parameter(description = "Identifier type (e.g. crn)", required = true)
    @PathVariable
    identifierType: IdentifierType,
    @Parameter(description = "Identifier Value", required = true)
    @PathVariable
    identifierValue: String,
  ): List<AllPredictorVersioned<Any>> = riskPredictorService.getAllRiskScores(identifierType, identifierValue)

  @RequestMapping(path = ["/assessments/id/{id}/risk/predictors/all"], method = [RequestMethod.GET])
  @Operation(
    description = """# Gets all risk predictors scores for the requested assessment ID

Returns the requested assessment containing all associated predictor scores.  
The assessment will have predictor scores in **either legacy** (OGRS3 generation) **or new** (OGRS4 generation) format.  

## Determining the predictor score format
The assessment contains a top level `outputVersion` field which dictates the format of the predictors nested within the `output` field.  

### Legacy risk predictor score format (outputVersion = 1)
```json
{
  "completedDate": "2024-01-01",
  "status": "COMPLETE",
  "outputVersion": "1",
  "output": {
    "groupReconvictionScore": {
      "oneYear": 0,
      "twoYears": 0,
      "scoreLevel": "LOW"
    },
    "violencePredictorScore": {
      "ovpStaticWeightedScore": 0,
      "ovpDynamicWeightedScore": 0,
      "ovpTotalWeightedScore": 0,
      "oneYear": 0,
      "twoYears": 0,
      "ovpRisk": "LOW"
    },
    "generalPredictorScore": {
      "ogpStaticWeightedScore": 0,
      "ogpDynamicWeightedScore": 0,
      "ogpTotalWeightedScore": 0,
      "ogp1Year": 0,
      "ogp2Year": 0,
      "ogpRisk": "LOW"
    },
    "riskOfSeriousRecidivismScore": {
      "percentageScore": 0,
      "staticOrDynamic": "STATIC",
      "source": "OASYS",
      "algorithmVersion": "string",
      "scoreLevel": "LOW"
    },
    "sexualPredictorScore": {
      "ospIndecentPercentageScore": 0,
      "ospContactPercentageScore": 0,
      "ospIndecentScoreLevel": "LOW",
      "ospContactScoreLevel": "LOW",
      "ospIndirectImagePercentageScore": 0,
      "ospDirectContactPercentageScore": 0,
      "ospIndirectImageScoreLevel": "LOW",
      "ospDirectContactScoreLevel": "LOW"
    }
  }
}
```

### New risk predictor score format (outputVersion = 2)
```json
{
  "completedDate": "2026-03-01",
  "status": "COMPLETE",
  "outputVersion": "2",
  "output": {
    "allReoffendingPredictor": {
      "staticOrDynamic": "STATIC",
      "score": 1,
      "band": "LOW"
    },
    "violentReoffendingPredictor": {
      "staticOrDynamic": "DYNAMIC",
      "score": 30,
      "band": "MEDIUM"
    },
    "seriousViolentReoffendingPredictor": {
      "staticOrDynamic": "STATIC",
      "score": 99,
      "band": "HIGH"
    },
    "directContactSexualReoffendingPredictor": {
      "score": 10,
      "band": "LOW"
    },
    "indirectImageContactSexualReoffendingPredictor": {
      "score": 10,
      "band": "LOW"
    },
    "combinedSeriousReoffendingPredictor": {
      "algorithmVersion": "string",
      "staticOrDynamic": "STATIC",
      "score": 0,
      "band": "LOW"
    }
  }
}
```

Please see the associated documentation for further information: [OGRS4 ARNS API Change Specification - All Risk Predictors](https://dsdmoj.atlassian.net/wiki/spaces/ARN/pages/5962203966/OGRS4+ARNS+API+Change+Specification#Endpoint-%232%2F%234-Changes---All-Risk-Predictors-by-CRN-or-assessment-id).
  """,
  )
  @Schema(oneOf = [AllPredictorVersionedLegacyDto::class, AllPredictorVersionedDto::class])
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "403",
        description = "User does not have permission to access assessment with provided ID",
      ),
      ApiResponse(responseCode = "404", description = "Risk data does not exist for assessment ID"),
      ApiResponse(responseCode = "401", description = "Unauthorised"),
      ApiResponse(responseCode = "400", description = "Bad request"),
      ApiResponse(
        responseCode = "200", description = "OK",
        content = [
          Content(
            mediaType = "application/json",
            examples = [
              ExampleObject(
                name = "Assessment containing Legacy predictor score format.",
                summary = "Assessment containing Legacy predictor score format",
                value = """
{
  "completedDate": "2024-01-01",
  "status": "COMPLETE",
  "outputVersion": "1",
  "output": {
    "groupReconvictionScore": {
      "oneYear": 0,
      "twoYears": 0,
      "scoreLevel": "LOW"
    },
    "violencePredictorScore": {
      "ovpStaticWeightedScore": 0,
      "ovpDynamicWeightedScore": 0,
      "ovpTotalWeightedScore": 0,
      "oneYear": 0,
      "twoYears": 0,
      "ovpRisk": "LOW"
    },
    "generalPredictorScore": {
      "ogpStaticWeightedScore": 0,
      "ogpDynamicWeightedScore": 0,
      "ogpTotalWeightedScore": 0,
      "ogp1Year": 0,
      "ogp2Year": 0,
      "ogpRisk": "LOW"
    },
    "riskOfSeriousRecidivismScore": {
      "percentageScore": 0,
      "staticOrDynamic": "STATIC",
      "source": "OASYS",
      "algorithmVersion": "string",
      "scoreLevel": "LOW"
    },
    "sexualPredictorScore": {
      "ospIndecentPercentageScore": 0,
      "ospContactPercentageScore": 0,
      "ospIndecentScoreLevel": "LOW",
      "ospContactScoreLevel": "LOW",
      "ospIndirectImagePercentageScore": 0,
      "ospDirectContactPercentageScore": 0,
      "ospIndirectImageScoreLevel": "LOW",
      "ospDirectContactScoreLevel": "LOW"
    }
  }
}
              """,
              ),
              ExampleObject(
                name = "Assessment containing New predictor score format.",
                summary = "Assessment containing New predictor score format",
                value = """
{
  "completedDate": "2026-03-01",
  "status": "COMPLETE",
  "outputVersion": "2",
  "output": {
    "allReoffendingPredictor": {
      "staticOrDynamic": "STATIC",
      "score": 1,
      "band": "LOW"
    },
    "violentReoffendingPredictor": {
      "staticOrDynamic": "DYNAMIC",
      "score": 30,
      "band": "MEDIUM"
    },
    "seriousViolentReoffendingPredictor": {
      "staticOrDynamic": "STATIC",
      "score": 99,
      "band": "HIGH"
    },
    "directContactSexualReoffendingPredictor": {
      "score": 10,
      "band": "LOW"
    },
    "indirectImageContactSexualReoffendingPredictor": {
      "score": 10,
      "band": "LOW"
    },
    "combinedSeriousReoffendingPredictor": {
      "algorithmVersion": "string",
      "staticOrDynamic": "STATIC",
      "score": 0,
      "band": "LOW"
    }
  }
}
              """,
              ),
            ],
          ),
        ],
      ),
    ],
  )
  @PreAuthorize("hasAnyRole('ROLE_ARNS__RISKS__RO')")
  fun getRiskScoresByAssessmentId(
    @Parameter(description = "Assessment ID", required = true)
    @PathVariable
    id: Long,
  ): AllPredictorVersioned<Any> = riskPredictorService.getAllRiskScoresByAssessmentId(id)

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }
}
