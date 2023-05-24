package uk.gov.justice.digital.hmpps.assessrisksandneeds.jpa.entities

import com.vladmihalcea.hibernate.type.json.JsonType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Type
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "supplementary_risk")
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

  @Type(JsonType::class)
  @Column(columnDefinition = "jsonb", name = "RISK_ANSWERS")
  val riskAnswers: Map<String, Any> = mutableMapOf(),

  @Column(name = "RISK_COMMENTS")
  val riskComments: String,

)
