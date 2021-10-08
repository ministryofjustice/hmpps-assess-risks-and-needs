package uk.gov.justice.digital.hmpps.assessrisksandneeds.jpa.respositories

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.assessrisksandneeds.jpa.entities.OffenderPredictorsHistoryEntity

@Repository
interface OffenderPredictorsHistoryRepository : JpaRepository<OffenderPredictorsHistoryEntity, Long> {

  fun findAllByCrn(crn: String): List<OffenderPredictorsHistoryEntity>
}
