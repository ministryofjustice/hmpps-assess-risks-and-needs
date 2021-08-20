package uk.gov.justice.digital.hmpps.assessrisksandneeds.jpa.entities

import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ScoreLevel
import uk.gov.justice.digital.hmpps.assessrisksandneeds.jpa.dao.PredictorSubType
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

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
  val predictorSubType: PredictorSubType,

  @Column(name = "PREDICTOR_SCORE")
  val predictorScore: BigDecimal?,

  @Column(name = "PREDICTOR_LEVEL")
  val predictorLevel: ScoreLevel?,

  @Column(name = "CREATED_DATE")
  val createdDate: LocalDateTime = LocalDateTime.now(),
) : Serializable
