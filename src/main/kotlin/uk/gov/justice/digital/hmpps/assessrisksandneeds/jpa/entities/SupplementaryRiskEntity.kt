package uk.gov.justice.digital.hmpps.assessrisksandneeds.jpa.entities

import com.vladmihalcea.hibernate.type.json.JsonBinaryType
import com.vladmihalcea.hibernate.type.json.JsonStringType
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import org.hibernate.annotations.TypeDefs
import java.time.LocalDateTime
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "supplementary_risk")
@TypeDefs(
  TypeDef(name = "json", typeClass = JsonStringType::class),
  TypeDef(name = "jsonb", typeClass = JsonBinaryType::class),
)
class SupplementaryRiskEntity(
  @Id
  @Column(name = "SUPPLEMENTARY_RISK_ID")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val supplementaryRiskId: Long? = null,

  @Column(name = "SUPPLEMENTARY_RISK_UUID")
  val supplementaryRiskUuid: UUID? = UUID.randomUUID(),

  @Column(name = "RISK_SOURCE")
  val source: String,

  @Column(name = "SOURCE_ID")
  val sourceId: String,

  @Column(name = "CRN")
  val crn: String,

  @Column(name = "CREATED_DATE")
  val createdDate: LocalDateTime,

  @Column(name = "CREATED_BY_USER_TYPE")
  val createdByUserType: String,

  @Column(name = "CREATED_BY")
  val createdBy: String,

  @Type(type = "json")
  @Column(columnDefinition = "jsonb", name = "RISK_ANSWERS")
  val riskAnswers: Map<String, Any> = mutableMapOf(),

  @Column(name = "RISK_COMMENTS")
  val riskComments: String,

)
