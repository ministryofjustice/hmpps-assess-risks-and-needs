package uk.gov.justice.digital.hmpps.assessrisksandneeds.jpa.respositories

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.assessrisksandneeds.jpa.entities.SupplementaryRiskEntity
import java.util.*

@Repository
interface SupplementaryRiskRepository : JpaRepository<SupplementaryRiskEntity, Long> {
  fun findBySourceAndSourceId(source: String, sourceId: String): SupplementaryRiskEntity?
  fun findBySupplementaryRiskUuid(supplementaryRiskUuid: UUID): SupplementaryRiskEntity?
  fun findAllByCrn(crn: String): List<SupplementaryRiskEntity>
}
