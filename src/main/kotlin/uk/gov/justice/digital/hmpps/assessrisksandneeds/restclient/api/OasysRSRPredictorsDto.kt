package uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api

import com.fasterxml.jackson.annotation.JsonFormat
import java.math.BigDecimal
import java.time.LocalDateTime

data class OasysRSRPredictorsDto(
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
  val errorCount: Int,
  val errorMessage: String? = null,
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  val calculationDateAndTime: LocalDateTime
)
