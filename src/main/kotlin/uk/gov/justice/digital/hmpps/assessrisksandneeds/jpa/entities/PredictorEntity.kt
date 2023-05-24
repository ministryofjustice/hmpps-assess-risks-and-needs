package uk.gov.justice.digital.hmpps.assessrisksandneeds.jpa.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.PredictorSubType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ScoreLevel
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "predictors")
class PredictorEntity(
  @Id
  @Column(name = "PREDICTOR_ID")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val predictorId: Long? = null,

  @Column(name = "PREDICTOR_UUID")
  val predictorUuid: UUID? = UUID.randomUUID(),

  @ManyToOne
  @JoinColumn(name = "OFFENDER_PREDICTOR_UUID", referencedColumnName = "OFFENDER_PREDICTOR_UUID")
  val offenderPredictors: OffenderPredictorsHistoryEntity? = null,

  @Column(name = "PREDICTOR_SUBTYPE")
  @Enumerated(EnumType.STRING)
  val predictorSubType: PredictorSubType,

  @Column(name = "PREDICTOR_SCORE")
  val predictorScore: BigDecimal?,

  @Column(name = "PREDICTOR_LEVEL")
  @Enumerated(EnumType.STRING)
  val predictorLevel: ScoreLevel?,

  @Column(name = "CREATED_DATE")
  val createdDate: LocalDateTime = LocalDateTime.now(),
) : Serializable
