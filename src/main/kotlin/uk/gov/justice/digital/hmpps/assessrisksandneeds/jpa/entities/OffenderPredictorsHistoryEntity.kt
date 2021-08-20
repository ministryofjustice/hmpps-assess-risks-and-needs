package uk.gov.justice.digital.hmpps.assessrisksandneeds.jpa.entities

import com.vladmihalcea.hibernate.type.json.JsonBinaryType
import com.vladmihalcea.hibernate.type.json.JsonStringType
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import org.hibernate.annotations.TypeDefs
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.PredictorType
import java.io.Serializable
import java.time.LocalDateTime
import java.util.UUID
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.OneToMany
import javax.persistence.Table

@Entity
@Table(name = "offender_predictors_history")
@TypeDefs(
  TypeDef(name = "json", typeClass = JsonStringType::class),
  TypeDef(name = "jsonb", typeClass = JsonBinaryType::class)
)
data class OffenderPredictorsHistoryEntity(

  @Id
  @Column(name = "OFFENDER_PREDICTOR_ID")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val offenderPredictorId: Long? = null,

  @Column(name = "OFFENDER_PREDICTOR_UUID")
  val offenderPredictorUuid: UUID? = UUID.randomUUID(),

  @Column(name = "PREDICTOR_TYPE")
  val predictorType: PredictorType,

  @Column(name = "ALGORITHM_VERSION")
  val algorithmVersion: String,

  @Column(name = "CALCULATED_AT")
  val calculatedAt: LocalDateTime,

  @Column(name = "CRN")
  val crn: String,

  @Column(name = "PREDICTOR_TRIGGER_SOURCE")
  val predictorTriggerSource: String,

  @Column(name = "SOURCE_ID")
  val predictorTriggerSourceId: String,

  @Type(type = "json")
  @Column(columnDefinition = "jsonb", name = "SOURCE_ANSWERS")
  var sourceAnswers: MutableMap<String, List<String>> = mutableMapOf(),

  @Column(name = "CREATED_BY")
  val createdBy: String,

  @Column(name = "CREATED_DATE")
  val createdDate: LocalDateTime = LocalDateTime.now(),

  @OneToMany(mappedBy = "offenderPredictors", cascade = [CascadeType.ALL])
  val predictors: MutableList<PredictorEntity> = mutableListOf(),

) : Serializable
