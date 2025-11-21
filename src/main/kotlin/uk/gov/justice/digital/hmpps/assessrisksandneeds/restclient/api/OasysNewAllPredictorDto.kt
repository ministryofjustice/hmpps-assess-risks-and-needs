package uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

data class OasysNewAllPredictorDto(
  @JsonProperty("ogrs4vYr2")
  val ogrs4vYr2: BigDecimal? = null,
  @JsonProperty("ogrs4vBand")
  val ogrs4vBand: String? = null,
  @JsonProperty("ogrs4vCalculated")
  val ogrs4vCalculated: String? = null,
  @JsonProperty("ogrs4gYr2")
  val ogrs4gYr2: BigDecimal? = null,
  @JsonProperty("ogrs4gBand")
  val ogrs4gBand: String? = null,
  @JsonProperty("ogrs4gCalculated")
  val ogrs4gCalculated: String? = null,
  @JsonProperty("ogp2Yr2")
  val ogp2Yr2: BigDecimal? = null,
  @JsonProperty("ogp2Band")
  val ogp2Band: String? = null,
  @JsonProperty("ogp2Calculated")
  val ogp2Calculated: String? = null,
  @JsonProperty("ovp2Yr2")
  val ovp2Yr2: BigDecimal? = null,
  @JsonProperty("ovp2Band")
  val ovp2Band: String? = null,
  @JsonProperty("ovp2Calculated")
  val ovp2Calculated: String? = null,
  @JsonProperty("snsvStaticYr2")
  val snsvStaticYr2: BigDecimal? = null,
  @JsonProperty("snsvDynamicYr2")
  val snsvDynamicYr2: BigDecimal? = null,
  @JsonProperty("snsvStaticYr2Band")
  val snsvStaticYr2Band: String? = null,
  @JsonProperty("snsvDynamicYr2Band")
  val snsvDynamicYr2Band: String? = null,
  @JsonProperty("snsvStaticCalculated")
  val snsvStaticCalculated: String? = null,
  @JsonProperty("snsvDynamicCalculated")
  val snsvDynamicCalculated: String? = null,
)
