package uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient

import java.math.BigDecimal

class OasysRSRPredictorsDto(
  val algorithmVersion: Int,
  val rsrScore: BigDecimal? = null,
  val rsrBand: String? = null,
  val validRsrScore: String? = null,
  val scoreType: String? = null,
  val ospcScore: BigDecimal? = null,
  val ospcBand: String? = null,
  val validOspcScore: String? = null,
  val ospiScore: BigDecimal? = null,
  val ospiBand: String? = null,
  val validOspiScore: String? = null,
  val snsvScore: BigDecimal? = null,
  val errorCount: Int? = null,
  val errorMessage: String? = null,
)
