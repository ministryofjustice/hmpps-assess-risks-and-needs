package uk.gov.justice.digital.hmpps.assessrisksandneeds.services

import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.Source
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.SupplementaryRiskDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.UserType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.jpa.entities.SupplementaryRiskEntity
import uk.gov.justice.digital.hmpps.assessrisksandneeds.jpa.respositories.SupplementaryRiskRepository
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.exceptions.EntityNotFoundException
import java.time.LocalDateTime
import java.util.*

@ExtendWith(MockKExtension::class)
@DisplayName("Supplementary Risk Service Tests")
class SupplementaryRiskServiceTest {
  private val supplementaryRiskRepository: SupplementaryRiskRepository = mockk()

  private val supplementaryRiskService = SupplementaryRiskService(supplementaryRiskRepository)

  @Test
  fun `get supplementary risk data by existing source and sourceId`() {
    val supplementaryRiskUuid = UUID.randomUUID()
    val source = "INTERVENTION_REFERRAL"
    val sourceId = "123"
    val crn = "CRN123"
    val createdDate = LocalDateTime.now()
    val createdByUserType = "DELIUS"
    val createdBy = "Arnold G."
    val riskComments = "risk comments bla bla"
    every {
      supplementaryRiskRepository.findBySourceAndSourceId(
        source,
        sourceId
      )
    } returns SupplementaryRiskEntity(
      123L, supplementaryRiskUuid, source, sourceId, crn,
      createdDate, createdByUserType, createdBy, riskComments
    )

    val riskBySourceAndSourceId =
      supplementaryRiskService.getRiskBySourceAndSourceId(Source.INTERVENTION_REFERRAL, sourceId)

    assertThat(riskBySourceAndSourceId).isEqualTo(
      SupplementaryRiskDto(
        supplementaryRiskUuid,
        Source.INTERVENTION_REFERRAL,
        sourceId,
        crn,
        createdBy,
        UserType.DELIUS,
        createdDate,
        riskComments
      )
    )
  }

  @Test
  fun `get supplementary risk data by existing source and sourceId throw Exception when risk not found`() {
    val source = "INTERVENTION_REFERRAL"
    val sourceId = "123"
    every {
      supplementaryRiskRepository.findBySourceAndSourceId(
        source,
        sourceId
      )
    } returns null

    val exception = assertThrows<EntityNotFoundException> {
      supplementaryRiskService.getRiskBySourceAndSourceId(Source.INTERVENTION_REFERRAL, sourceId)
    }
    assertEquals("Error retrieving Supplementary Risk for source:INTERVENTION_REFERRAL and sourceId:123", exception.message)
  }
}
