package uk.gov.justice.digital.hmpps.assessrisksandneeds.jpa.entities

import java.time.LocalDateTime
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "supplementary_risk")
class SupplementaryRiskEntity(
  @Id
  @Column(name = "SUPPLEMENTARY_RISK_ID")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val supplementaryRiskId: Long? = null,

  @Column(name = "SUPPLEMENTARY_RISK_UUID")
  val supplementaryRiskUuid: UUID = UUID.randomUUID(),

  @Column(name = "RISK_SOURCE")
  val source: String? = null,

  @Column(name = "SOURCE_ID")
  val sourceId: String? = null,

  @Column(name = "CRN")
  val crn: String? = null,

  @Column(name = "CREATED_DATE")
  val createdDate: LocalDateTime? = null,

  @Column(name = "CREATED_BY_USER_TYPE")
  val createdByUserType: String? = null,

  @Column(name = "CREATED_BY")
  val createdBy: String? = null,

  @Column(name = "RISK_COMMENTS")
  val riskComments: String? = null,

)
