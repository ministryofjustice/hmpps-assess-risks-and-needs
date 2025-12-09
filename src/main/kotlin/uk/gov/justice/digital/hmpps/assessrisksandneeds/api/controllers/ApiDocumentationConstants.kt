package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.controllers

const val GET_ALL_RSR_SCORES_BY_IDENTIFIER_TYPE_DESC =
  """# Gets Combined Serious Reoffending Predictor scores for an identifier type (e.g. CRN)

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
  """

const val GET_ALL_RSR_SCORES_BY_IDENTIFIER_TYPE_EXAMPLE = """
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
  """

const val GET_ALL_RISK_SCORES_BY_IDENTIFIER_TYPE_DESC =
  """# Gets all risk predictor scores for completed assessments for an identifier type (e.g. CRN)

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
  """

const val GET_ALL_RISK_SCORES_BY_IDENTIFIER_TYPE_EXAMPLE = """
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
  """

const val GET_ALL_RISK_SCORES_BY_ASSESSMENT_ID_DESC =
  """# Gets all risk predictors scores for the requested assessment ID

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
  """

const val GET_ALL_RISK_SCORES_BY_ASSESSMENT_ID_LEGACY_EXAMPLE = """
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
  """

const val GET_ALL_RISK_SCORES_BY_ASSESSMENT_ID_NEW_EXAMPLE = """
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
  """
