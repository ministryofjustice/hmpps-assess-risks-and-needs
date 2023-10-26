package uk.gov.justice.digital.hmpps.assessrisksandneeds.jpa.entities

import com.vladmihalcea.hibernate.type.json.JsonType
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.annotations.Type
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.PredictorSource
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.PredictorType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ScoreType
import java.io.Serializable
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "offender_predictors_history")
data class OffenderPredictorsHistoryEntity(

  @Id
  @Column(name = "OFFENDER_PREDICTOR_ID")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val offenderPredictorId: Long? = null,

  @Column(name = "OFFENDER_PREDICTOR_UUID")
  val offenderPredictorUuid: UUID? = UUID.randomUUID(),

  @Column(name = "PREDICTOR_TYPE")
  @Enumerated(EnumType.STRING)
  val predictorType: PredictorType,

  @Column(name = "ALGORITHM_VERSION")
  val algorithmVersion: String,

  @Column(name = "CALCULATED_AT")
  val calculatedAt: LocalDateTime,

  @Column(name = "CRN")
  val crn: String,

  @Column(name = "PREDICTOR_TRIGGER_SOURCE")
  @Enumerated(EnumType.STRING)
  val predictorTriggerSource: PredictorSource,

  @Column(name = "SOURCE_ID")
  val predictorTriggerSourceId: String,

  @Type(JsonType::class)
  @Column(columnDefinition = "jsonb", name = "SOURCE_ANSWERS")
  var sourceAnswers: Map<String, Any> = mutableMapOf(),

  @Column(name = "CREATED_BY")
  val createdBy: String,

  @Column(name = "CREATED_DATE")
  val createdDate: LocalDateTime = LocalDateTime.now(),

  @Column(name = "COMPLETED_DATE")
  val assessmentCompletedDate: LocalDateTime,

  @Column(name = "SCORE_TYPE")
  @Enumerated(EnumType.STRING)
  val scoreType: ScoreType,

  @OneToMany(mappedBy = "offenderPredictors", cascade = [CascadeType.ALL])
  val predictors: MutableList<PredictorEntity> = mutableListOf(),

) : Serializable
