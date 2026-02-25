package uk.gov.justice.digital.hmpps.assessrisksandneeds.config

import io.swagger.v3.core.converter.ModelConverters
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.media.ComposedSchema
import io.swagger.v3.oas.models.media.Schema
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AllPredictorVersionedDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AllPredictorVersionedLegacyDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RiskScoresDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RsrPredictorDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RsrPredictorVersionedDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RsrPredictorVersionedLegacyDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ogrs4.AllPredictorDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ogrs4.BasePredictorDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ogrs4.StaticOrDynamicPredictorDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ogrs4.VersionedStaticOrDynamicPredictorDto

// Due to limitations in Swagger annotations we need to manually define the schema directly on the OpenAPI bean
fun Components.addRiskActuarualSchemas(): Components = this.addSchemas(
  "RsrPredictorVersionedDto",
  ModelConverters.getInstance().readAllAsResolvedSchema(RsrPredictorVersionedDto::class.java).schema,
)
  .addSchemas(
    "RsrPredictorVersionedLegacyDto",
    ModelConverters.getInstance().readAllAsResolvedSchema(RsrPredictorVersionedLegacyDto::class.java).schema,
  )
  .addSchemas(
    "Ogrs3RsrPredictorDto",
    ModelConverters.getInstance().readAllAsResolvedSchema(RsrPredictorDto::class.java).schema,
  )
  .addSchemas(
    "Ogrs4RsrPredictorDto",
    ModelConverters.getInstance()
      .readAllAsResolvedSchema(uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ogrs4.RsrPredictorDto::class.java).schema,
  )
  .addSchemas(
    "StaticOrDynamicPredictorDto",
    ModelConverters.getInstance().readAllAsResolvedSchema(StaticOrDynamicPredictorDto::class.java).schema,
  )
  .addSchemas(
    "BasePredictorDto",
    ModelConverters.getInstance().readAllAsResolvedSchema(BasePredictorDto::class.java).schema,
  )
  .addSchemas(
    "VersionedStaticOrDynamicPredictorDto",
    ModelConverters.getInstance().readAllAsResolvedSchema(VersionedStaticOrDynamicPredictorDto::class.java).schema,
  )
  .addSchemas(
    "RsrPredictorVersionedUnion",
    ComposedSchema().anyOf(
      listOf(
        Schema<Any>().`$ref`("#/components/schemas/RsrPredictorVersionedDto"),
        Schema<Any>().`$ref`("#/components/schemas/RsrPredictorVersionedLegacyDto"),
      ),
    ),
  )
  .addSchemas(
    "AllPredictorVersionedDto",
    ModelConverters.getInstance().readAllAsResolvedSchema(AllPredictorVersionedDto::class.java).schema,
  )
  .addSchemas(
    "AllPredictorVersionedLegacyDto",
    ModelConverters.getInstance().readAllAsResolvedSchema(AllPredictorVersionedLegacyDto::class.java).schema,
  )
  .addSchemas(
    "AllPredictorDto",
    ModelConverters.getInstance().readAllAsResolvedSchema(AllPredictorDto::class.java).schema,
  )
  .addSchemas(
    "RiskScoresDto",
    ModelConverters.getInstance().readAllAsResolvedSchema(RiskScoresDto::class.java).schema,
  )
  .addSchemas(
    "AllPredictorVersionedUnion",
    ComposedSchema().anyOf(
      listOf(
        Schema<Any>().`$ref`("#/components/schemas/AllPredictorVersionedDto"),
        Schema<Any>().`$ref`("#/components/schemas/AllPredictorVersionedLegacyDto"),
      ),
    ),
  )
