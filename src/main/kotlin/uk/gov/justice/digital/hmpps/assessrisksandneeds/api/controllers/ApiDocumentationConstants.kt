package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.controllers

const val GET_ALL_RSR_SCORES_BY_IDENTIFIER_TYPE_DESC =
  """# Gets Combined Serious Reoffending Predictor scores for an identifier type (e.g. CRN)

Returns a list of assessments containing Combined Serious Reoffending Predictor scores (previously known as RSR scores).
Assessments within the list will have predictor scores in **either legacy** (OGRS3 generation) **or new** (OGRS4 generation) format.

Note that all fields should be implemented as nullable as data may not be available.

## Determining the predictor score format
Each assessment contains a top level `outputVersion` field which dictates the format of the predictors nested within the `output` field.

### Legacy risk predictor score format (outputVersion = 1)
All numbers should be coded as decimals as may not be integers
```json
[
  {
    "completedDate": "2025-10-23T03:02:59",
    "source": "OASYS",
    "status": "COMPLETE",
    "outputVersion": "1",
    "output": {
      "staticOrDynamic": "STATIC",
      "algorithmVersion": "5",
      "rsrPercentageScore": 4.34,
      "rsrScoreLevel": "MEDIUM",
      "ospcPercentageScore": null,
      "ospcScoreLevel": null,
      "ospiPercentageScore": null,
      "ospiScoreLevel": null,
      "ospiiPercentageScore": 1,
      "ospdcPercentageScore": 2,
      "ospiiScoreLevel": "LOW",
      "ospdcScoreLevel": "LOW"
    }
  }
]
```

### New risk predictor score format (outputVersion = 2)
All numbers should be coded as decimals as may not be integers
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
        "score": 10.01,
        "band": "LOW"
      },
      "directContactSexualReoffendingPredictor": {
        "score": 11.34,
        "band": "LOW"
      },
      "indirectImageContactSexualReoffendingPredictor": {
        "score": 12.34,
        "band": "LOW"
      },
      "combinedSeriousReoffendingPredictor": {
        "algorithmVersion": "6",
        "staticOrDynamic": "STATIC",
        "score": 9.97,
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
      "algorithmVersion": "5",
      "rsrPercentageScore": 10.1,
      "rsrScoreLevel": "HIGH",
      "ospcPercentageScore": null,
      "ospcScoreLevel": null,
      "ospiPercentageScore": null,
      "ospiScoreLevel": null,
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
        "score": 10.01,
        "band": "LOW"
      },
      "directContactSexualReoffendingPredictor": {
        "score": 11.34,
        "band": "LOW"
      },
      "indirectImageContactSexualReoffendingPredictor": {
        "score": 12.34,
        "band": "LOW"
      },
      "combinedSeriousReoffendingPredictor": {
        "algorithmVersion": "6",
        "staticOrDynamic": "STATIC",
        "score": 9.97,
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

Note that all fields should be implemented as nullable as data may not be available.

## Determining the predictor score format
Each assessment contains a top level `outputVersion` field which dictates the format of the predictors nested within the `output` field.

### Legacy risk predictor score format (outputVersion = 1)
All numbers should be coded as decimals as may not be integers
```json
[
  {
    "completedDate": "2025-10-23T03:02:59",
    "status": "COMPLETE",
    "assessmentType": "LAYER3",
    "outputVersion": "1",
    "output": {
      "groupReconvictionScore": {
        "oneYear": 6,
        "twoYears": 12,
        "scoreLevel": "LOW"
      },
      "violencePredictorScore": {
        "ovpStaticWeightedScore": 10,
        "ovpDynamicWeightedScore": 4,
        "ovpTotalWeightedScore": 14,
        "oneYear": 3,
        "twoYears": 5,
        "ovpRisk": "LOW"
      },
      "generalPredictorScore": {
        "ogpStaticWeightedScore": 7,
        "ogpDynamicWeightedScore": 0,
        "ogpTotalWeightedScore": 7,
        "ogp1Year": 4,
        "ogp2Year": 7,
        "ogpRisk": "LOW"
      },
      "riskOfSeriousRecidivismScore": {
        "percentageScore": 0.05,
        "staticOrDynamic": "DYNAMIC",
        "source": "OASYS",
        "algorithmVersion": "5",
        "scoreLevel": "LOW"
      },
      "sexualPredictorScore": {
        "ospIndirectImagePercentageScore": 0,
        "ospDirectContactPercentageScore": 0,
        "ospIndirectImageScoreLevel": "NOT_APPLICABLE",
        "ospDirectContactScoreLevel": "NOT_APPLICABLE"
      }
    }
  }
]
```

### New risk predictor score format (outputVersion = 2)
All numbers should be coded as decimals as may not be integers
```json
[
 {
    "completedDate": "2026-01-21T15:01:20",
    "status": "COMPLETE",
    "assessmentType": "LAYER3",
    "outputVersion": "2",
    "output": {
      "allReoffendingPredictor": {
        "staticOrDynamic": "DYNAMIC",
        "score": 4.41,
        "band": "LOW"
      },
      "violentReoffendingPredictor": {
        "staticOrDynamic": "DYNAMIC",
        "score": 1.91,
        "band": "LOW"
      },
      "seriousViolentReoffendingPredictor": {
        "staticOrDynamic": "DYNAMIC",
        "score": 0.03,
        "band": "LOW"
      },
      "directContactSexualReoffendingPredictor": {
        "score": 0.02,
        "band": "LOW"
      },
      "indirectImageContactSexualReoffendingPredictor": {
        "score": 0.12,
        "band": "LOW"
      },
      "combinedSeriousReoffendingPredictor": {
        "algorithmVersion": "6",
        "staticOrDynamic": "DYNAMIC",
        "score": 0.17,
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
    "completedDate": "2025-10-23T03:02:59",
    "status": "COMPLETE",
    "assessmentType": "LAYER3",
    "outputVersion": "1",
    "output": {
      "groupReconvictionScore": {
        "oneYear": 6,
        "twoYears": 12,
        "scoreLevel": "LOW"
      },
      "violencePredictorScore": {
        "ovpStaticWeightedScore": 10,
        "ovpDynamicWeightedScore": 4,
        "ovpTotalWeightedScore": 14,
        "oneYear": 3,
        "twoYears": 5,
        "ovpRisk": "LOW"
      },
      "generalPredictorScore": {
        "ogpStaticWeightedScore": 7,
        "ogpDynamicWeightedScore": 0,
        "ogpTotalWeightedScore": 7,
        "ogp1Year": 4,
        "ogp2Year": 7,
        "ogpRisk": "LOW"
      },
      "riskOfSeriousRecidivismScore": {
        "percentageScore": 0.05,
        "staticOrDynamic": "DYNAMIC",
        "source": "OASYS",
        "algorithmVersion": "5",
        "scoreLevel": "LOW"
      },
      "sexualPredictorScore": {
        "ospIndirectImagePercentageScore": 0,
        "ospDirectContactPercentageScore": 0,
        "ospIndirectImageScoreLevel": "NOT_APPLICABLE",
        "ospDirectContactScoreLevel": "NOT_APPLICABLE"
      }
    }
  },
  {
    "completedDate": "2026-01-21T15:01:20",
    "status": "COMPLETE",
    "assessmentType": "LAYER3",
    "outputVersion": "2",
    "output": {
      "allReoffendingPredictor": {
        "staticOrDynamic": "DYNAMIC",
        "score": 4.41,
        "band": "LOW"
      },
      "violentReoffendingPredictor": {
        "staticOrDynamic": "DYNAMIC",
        "score": 1.91,
        "band": "LOW"
      },
      "seriousViolentReoffendingPredictor": {
        "staticOrDynamic": "DYNAMIC",
        "score": 0.03,
        "band": "LOW"
      },
      "directContactSexualReoffendingPredictor": {
        "score": 0.02,
        "band": "LOW"
      },
      "indirectImageContactSexualReoffendingPredictor": {
        "score": 0.12,
        "band": "LOW"
      },
      "combinedSeriousReoffendingPredictor": {
        "algorithmVersion": "6",
        "staticOrDynamic": "DYNAMIC",
        "score": 0.17,
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

Note that all fields should be implemented as nullable as data may not be available.

## Determining the predictor score format
The assessment contains a top level `outputVersion` field which dictates the format of the predictors nested within the `output` field.

### Legacy risk predictor score format (outputVersion = 1)
All numbers should be coded as decimals as may not be integers
```json
{
  "completedDate": "2025-10-23T03:02:59",
  "status": "COMPLETE",
  "assessmentType": "LAYER3",
  "outputVersion": "1",
  "output": {
    "groupReconvictionScore": {
      "oneYear": 6,
      "twoYears": 12,
      "scoreLevel": "LOW"
    },
    "violencePredictorScore": {
      "ovpStaticWeightedScore": 10,
      "ovpDynamicWeightedScore": 4,
      "ovpTotalWeightedScore": 14,
      "oneYear": 3,
      "twoYears": 5,
      "ovpRisk": "LOW"
    },
    "generalPredictorScore": {
      "ogpStaticWeightedScore": 7,
      "ogpDynamicWeightedScore": 0,
      "ogpTotalWeightedScore": 7,
      "ogp1Year": 4,
      "ogp2Year": 7,
      "ogpRisk": "LOW"
    },
    "riskOfSeriousRecidivismScore": {
      "percentageScore": 0.05,
      "staticOrDynamic": "DYNAMIC",
      "source": "OASYS",
      "algorithmVersion": "5",
      "scoreLevel": "LOW"
    },
    "sexualPredictorScore": {
      "ospIndirectImagePercentageScore": 0,
      "ospDirectContactPercentageScore": 0,
      "ospIndirectImageScoreLevel": "NOT_APPLICABLE",
      "ospDirectContactScoreLevel": "NOT_APPLICABLE"
    }
  }
}
```

### New risk predictor score format (outputVersion = 2)
All numbers should be coded as decimals as may not be integers
```json
{
  "completedDate": "2026-01-21T15:01:20",
  "status": "COMPLETE",
  "assessmentType": "LAYER3",
  "outputVersion": "2",
  "output": {
    "allReoffendingPredictor": {
      "staticOrDynamic": "DYNAMIC",
      "score": 4.41,
      "band": "LOW"
    },
    "violentReoffendingPredictor": {
      "staticOrDynamic": "DYNAMIC",
      "score": 1.91,
      "band": "LOW"
    },
    "seriousViolentReoffendingPredictor": {
      "staticOrDynamic": "DYNAMIC",
      "score": 0.03,
      "band": "LOW"
    },
    "directContactSexualReoffendingPredictor": {
      "score": 0.02,
      "band": "LOW"
    },
    "indirectImageContactSexualReoffendingPredictor": {
      "score": 0.12,
      "band": "LOW"
    },
    "combinedSeriousReoffendingPredictor": {
      "algorithmVersion": "6",
      "staticOrDynamic": "DYNAMIC",
      "score": 0.17,
      "band": "LOW"
    }
  }
}
```

Please see the associated documentation for further information: [OGRS4 ARNS API Change Specification - All Risk Predictors](https://dsdmoj.atlassian.net/wiki/spaces/ARN/pages/5962203966/OGRS4+ARNS+API+Change+Specification#Endpoint-%232%2F%234-Changes---All-Risk-Predictors-by-CRN-or-assessment-id).
  """

const val GET_ALL_RISK_SCORES_BY_ASSESSMENT_ID_LEGACY_EXAMPLE = """
{
  "completedDate": "2025-10-23T03:02:59",
  "status": "COMPLETE",
  "assessmentType": "LAYER3",
  "outputVersion": "1",
  "output": {
    "groupReconvictionScore": {
      "oneYear": 6,
      "twoYears": 12,
      "scoreLevel": "LOW"
    },
    "violencePredictorScore": {
      "ovpStaticWeightedScore": 10,
      "ovpDynamicWeightedScore": 4,
      "ovpTotalWeightedScore": 14,
      "oneYear": 3,
      "twoYears": 5,
      "ovpRisk": "LOW"
    },
    "generalPredictorScore": {
      "ogpStaticWeightedScore": 7,
      "ogpDynamicWeightedScore": 0,
      "ogpTotalWeightedScore": 7,
      "ogp1Year": 4,
      "ogp2Year": 7,
      "ogpRisk": "LOW"
    },
    "riskOfSeriousRecidivismScore": {
      "percentageScore": 0.05,
      "staticOrDynamic": "DYNAMIC",
      "source": "OASYS",
      "algorithmVersion": "5",
      "scoreLevel": "LOW"
    },
    "sexualPredictorScore": {
      "ospIndirectImagePercentageScore": 0,
      "ospDirectContactPercentageScore": 0,
      "ospIndirectImageScoreLevel": "NOT_APPLICABLE",
      "ospDirectContactScoreLevel": "NOT_APPLICABLE"
    }
  }
}
  """

const val GET_ALL_RISK_SCORES_BY_ASSESSMENT_ID_NEW_EXAMPLE = """
{
  "completedDate": "2026-01-21T15:01:20",
  "status": "COMPLETE",
  "assessmentType": "LAYER3",
  "outputVersion": "2",
  "output": {
    "allReoffendingPredictor": {
      "staticOrDynamic": "DYNAMIC",
      "score": 4.41,
      "band": "LOW"
    },
    "violentReoffendingPredictor": {
      "staticOrDynamic": "DYNAMIC",
      "score": 1.91,
      "band": "LOW"
    },
    "seriousViolentReoffendingPredictor": {
      "staticOrDynamic": "DYNAMIC",
      "score": 0.03,
      "band": "LOW"
    },
    "directContactSexualReoffendingPredictor": {
      "score": 0.02,
      "band": "LOW"
    },
    "indirectImageContactSexualReoffendingPredictor": {
      "score": 0.12,
      "band": "LOW"
    },
    "combinedSeriousReoffendingPredictor": {
      "algorithmVersion": "6",
      "staticOrDynamic": "DYNAMIC",
      "score": 0.17,
      "band": "LOW"
    }
  }
}
  """
