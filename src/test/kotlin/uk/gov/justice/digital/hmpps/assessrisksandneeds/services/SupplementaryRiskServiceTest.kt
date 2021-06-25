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
import org.slf4j.MDC
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.CreateSupplementaryRiskDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.Source
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.SupplementaryRiskDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.config.RequestData.Companion.USER_NAME_HEADER
import uk.gov.justice.digital.hmpps.assessrisksandneeds.jpa.entities.SupplementaryRiskEntity
import uk.gov.justice.digital.hmpps.assessrisksandneeds.jpa.respositories.SupplementaryRiskRepository
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.exceptions.DuplicateSourceRecordFound
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.exceptions.EntityNotFoundException
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.exceptions.UserNameNotFoundException
import java.time.LocalDateTime
import java.util.UUID

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
    val createdByUserType = "delius"
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
        "delius",
        createdDate,
        riskComments
      )
    )
  }

  @Test
  fun `get supplementary risk data by existing source and sourceId throws Exception when risk not found`() {
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
    assertEquals(
      "Error retrieving Supplementary Risk for source: INTERVENTION_REFERRAL and sourceId: 123",
      exception.message
    )
  }

  @Test
  fun `get supplementary risk data by existing supplementaryRiskUuid`() {
    val supplementaryRiskUuid = UUID.randomUUID()
    val source = "INTERVENTION_REFERRAL"
    val sourceId = "123"
    val crn = "CRN123"
    val createdDate = LocalDateTime.now()
    val createdByUserType = "delius"
    val createdBy = "Arnold G."
    val riskComments = "risk comments bla bla"
    every {
      supplementaryRiskRepository.findBySupplementaryRiskUuid(
        supplementaryRiskUuid
      )
    } returns SupplementaryRiskEntity(
      123L, supplementaryRiskUuid, source, sourceId, crn,
      createdDate, createdByUserType, createdBy, riskComments
    )

    val riskBySourceAndSourceId =
      supplementaryRiskService.getRiskBySupplementaryRiskUuid(supplementaryRiskUuid)

    assertThat(riskBySourceAndSourceId).isEqualTo(
      SupplementaryRiskDto(
        supplementaryRiskUuid,
        Source.INTERVENTION_REFERRAL,
        sourceId,
        crn,
        createdBy,
        "delius",
        createdDate,
        riskComments
      )
    )
  }

  @Test
  fun `get supplementary risk data by supplementaryRiskUuid throws Exception when risk not found`() {
    val supplementaryRiskUuid = UUID.randomUUID()

    every {
      supplementaryRiskRepository.findBySupplementaryRiskUuid(
        supplementaryRiskUuid
      )
    } returns null

    val exception = assertThrows<EntityNotFoundException> {
      supplementaryRiskService.getRiskBySupplementaryRiskUuid(supplementaryRiskUuid)
    }
    assertEquals(
      "Error retrieving Supplementary Risk for supplementaryRiskUuid: $supplementaryRiskUuid",
      exception.message
    )
  }

  @Test
  fun `get all risks data by existing crn`() {
    val supplementaryRiskUuid = UUID.randomUUID()
    val supplementaryRiskUuid2 = UUID.randomUUID()
    val source = "INTERVENTION_REFERRAL"
    val sourceId = "123"
    val crn = "CRN123"
    val createdDate = LocalDateTime.now()
    val createdByUserType = "delius"
    val createdBy = "Arnold G."
    val riskComments = "risk comments bla bla"
    every {
      supplementaryRiskRepository.findAllByCrnOrderByCreatedByDesc(
        crn
      )
    } returns listOf(
      SupplementaryRiskEntity(
        123L, supplementaryRiskUuid, source, sourceId, crn,
        createdDate, createdByUserType, createdBy, riskComments
      ),
      SupplementaryRiskEntity(
        124L, supplementaryRiskUuid2, source, sourceId, crn,
        createdDate, createdByUserType, createdBy, riskComments
      )
    )

    val risksByCrn =
      supplementaryRiskService.getRisksByCrn(crn)

    assertThat(risksByCrn).containsExactly(
      SupplementaryRiskDto(
        supplementaryRiskUuid,
        Source.INTERVENTION_REFERRAL,
        sourceId,
        crn,
        createdBy,
        "delius",
        createdDate,
        riskComments
      ),
      SupplementaryRiskDto(
        supplementaryRiskUuid2,
        Source.INTERVENTION_REFERRAL,
        sourceId,
        crn,
        createdBy,
        "delius",
        createdDate,
        riskComments
      )
    )
  }

  @Test
  fun `create supplementary risk data`() {
    MDC.put(USER_NAME_HEADER, "Arnold G.")

    val supplementaryRiskUuid = UUID.randomUUID()
    val source = "INTERVENTION_REFERRAL"
    val sourceId = "123"
    val crn = "CRN123"
    val createdDate = LocalDateTime.now()
    val createdByUserType = "delius"
    val createdBy = "Arnold G."
    val riskComments = "risk comments bla bla"
    val riskEntity = SupplementaryRiskEntity(
      supplementaryRiskId = 123L,
      supplementaryRiskUuid = supplementaryRiskUuid,
      source = source,
      sourceId = sourceId,
      crn = crn,
      createdDate = createdDate,
      createdByUserType = createdByUserType,
      createdBy = createdBy,
      riskComments = riskComments
    )
    every {
      supplementaryRiskRepository.findBySourceAndSourceId(source, sourceId)
    } returns null
    every {
      supplementaryRiskRepository.save(any())
    } returns riskEntity

    val supplementaryRiskDto = CreateSupplementaryRiskDto(
      source = Source.INTERVENTION_REFERRAL,
      sourceId = sourceId,
      crn = crn,
      createdByUserType = "delius",
      createdDate = createdDate,
      riskSummaryComments = riskComments
    )
    val risk = supplementaryRiskService.createNewSupplementaryRisk(supplementaryRiskDto)

    assertThat(risk).isEqualTo(
      SupplementaryRiskDto(
        supplementaryRiskId = supplementaryRiskUuid,
        source = Source.INTERVENTION_REFERRAL,
        sourceId = sourceId,
        crn = crn,
        createdByUser = createdBy,
        createdByUserType = "delius",
        createdDate = createdDate,
        riskSummaryComments = riskComments
      )
    )
  }

  @Test
  fun `throw exception when supplementary risk data already exists for source`() {
    val supplementaryRiskUuid = UUID.randomUUID()
    val source = "INTERVENTION_REFERRAL"
    val sourceId = "123"
    val crn = "CRN123"
    val createdDate = LocalDateTime.now()
    val createdByUserType = "delius"
    val createdBy = "Arnold G."
    val riskComments = "risk comments bla bla"
    val riskEntity = SupplementaryRiskEntity(
      123L, supplementaryRiskUuid, source, sourceId, crn,
      createdDate, createdByUserType, createdBy, riskComments
    )
    every {
      supplementaryRiskRepository.findBySourceAndSourceId(source, sourceId)
    } returns riskEntity

    val supplementaryRiskDto = CreateSupplementaryRiskDto(
      Source.INTERVENTION_REFERRAL,
      sourceId,
      crn,
      "delius",
      createdDate,
      riskComments
    )

    val exception = assertThrows<DuplicateSourceRecordFound> {
      supplementaryRiskService.createNewSupplementaryRisk(supplementaryRiskDto)
    }

    assertEquals(
      "Duplicate supplementary risk found for source: $source with sourceId: $sourceId",
      exception.message
    )
  }

  @Test
  fun `throws UserNameNotFoundException when user name is not in the auth context`() {
    MDC.clear()
    val source = Source.INTERVENTION_REFERRAL
    val sourceId = UUID.randomUUID().toString()
    every {
      supplementaryRiskRepository.findBySourceAndSourceId(source.name, sourceId)
    } returns null

    assertThrows<UserNameNotFoundException> {
      supplementaryRiskService.createNewSupplementaryRisk(
        CreateSupplementaryRiskDto(
          source = source,
          sourceId = sourceId,
          crn = "XD83873",
          createdByUserType = "createdByUserType",
          riskSummaryComments = "comments"
        )
      )
    }
  }
}
