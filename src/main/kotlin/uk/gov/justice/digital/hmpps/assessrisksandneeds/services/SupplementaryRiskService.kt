package uk.gov.justice.digital.hmpps.assessrisksandneeds.services

import com.beust.klaxon.Klaxon
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.CreateSupplementaryRiskDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RedactedOasysRiskDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.Source
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.SupplementaryRiskDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.UserType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.config.RequestData
import uk.gov.justice.digital.hmpps.assessrisksandneeds.jpa.entities.SupplementaryRiskEntity
import uk.gov.justice.digital.hmpps.assessrisksandneeds.jpa.respositories.SupplementaryRiskRepository
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.exceptions.DuplicateSourceRecordFound
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.exceptions.EntityNotFoundException
import java.util.UUID

@Service
class SupplementaryRiskService(
  private val supplementaryRiskRepository: SupplementaryRiskRepository,
  @Qualifier("globalObjectMapper") private val objectMapper: ObjectMapper,
) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  fun getRiskBySourceAndSourceId(source: Source, sourceId: String): SupplementaryRiskDto {
    log.info("Get supplementary risk data by source: $source and sourceId: $sourceId")
    val supplementaryRiskEntity = supplementaryRiskRepository.findBySourceAndSourceId(source.name, sourceId)
    return supplementaryRiskEntity.toSupplementaryRiskDto("for source: $source and sourceId: $sourceId")
      .also { log.info("returning supplementary risk records found for source: $source and sourceId: $sourceId") }
  }

  fun getRisksByCrn(crn: String): List<SupplementaryRiskDto> {
    log.info("Get all supplementary risk data by crn: $crn")
    val supplementaryRiskEntities = supplementaryRiskRepository.findAllByCrnOrderByCreatedByDesc(crn)
    return supplementaryRiskEntities.toSupplementaryRiskDtos("for crn: $crn")
      .also { log.info("${it.size} supplementary risk records found for crn: $crn") }
  }

  fun getRiskBySupplementaryRiskUuid(supplementaryRiskUuid: UUID): SupplementaryRiskDto {
    log.info("Get supplementary risk data by supplementaryRiskUuid: $supplementaryRiskUuid")
    val supplementaryRiskEntity = supplementaryRiskRepository.findBySupplementaryRiskUuid(supplementaryRiskUuid)
    return supplementaryRiskEntity.toSupplementaryRiskDto("for supplementaryRiskUuid: $supplementaryRiskUuid")
      .also { log.info("returning supplementary risk records found for risk ID: $supplementaryRiskUuid") }
  }

  fun createNewSupplementaryRisk(supplementaryRiskDto: CreateSupplementaryRiskDto): SupplementaryRiskDto {
    with(supplementaryRiskDto) {
      log.info("Create new supplementary risk for crn: $crn")
      val existingRisk = supplementaryRiskRepository.findBySourceAndSourceId(source.name, sourceId)
      if (existingRisk != null) {
        throw DuplicateSourceRecordFound(
          "Duplicate supplementary risk found for source: $source with sourceId: $sourceId",
          existingRisk.toSupplementaryRiskDto("for source: $source and sourceId: $sourceId"),
        )
      }
      return supplementaryRiskRepository.save(this.toSupplementaryRiskEntity())
        .toSupplementaryRiskDto("for crn: $crn").also { log.info("Supplementary risk record created for crn: $crn") }
    }
  }

  fun SupplementaryRiskEntity?.toSupplementaryRiskDto(message: String): SupplementaryRiskDto {
    if (this == null) throw EntityNotFoundException("Error retrieving Supplementary Risk $message")
    return SupplementaryRiskDto(
      this.supplementaryRiskUuid,
      Source.fromString(this.source),
      this.sourceId,
      this.crn,
      this.createdBy,
      this.createdByUserType.lowercase(),
      this.createdDate,
      toRedactedOasysRisk(this.riskAnswers),
      this.riskComments,
    )
  }

  fun CreateSupplementaryRiskDto.toSupplementaryRiskEntity(): SupplementaryRiskEntity = SupplementaryRiskEntity(
    source = this.source.name,
    sourceId = this.sourceId,
    crn = this.crn,
    createdBy = RequestData.getUserName(),
    createdByUserType = UserType.fromString(this.createdByUserType).name,
    createdDate = this.createdDate,
    riskAnswers = toJson(this.redactedRisk) ?: mapOf(),
    riskComments = this.riskSummaryComments,
  )

  fun toJson(redactedRiskDto: RedactedOasysRiskDto?): Map<String, Any>? = if (redactedRiskDto == null) {
    null
  } else {
    Klaxon().parse<Map<String, Any>>(objectMapper.writeValueAsString(redactedRiskDto))
  }

  fun toRedactedOasysRisk(json: Map<String, Any>?): RedactedOasysRiskDto? = if (json.isNullOrEmpty()) {
    null
  } else {
    Klaxon().parse<RedactedOasysRiskDto>(objectMapper.writeValueAsString(json))
  }

  fun List<SupplementaryRiskEntity>.toSupplementaryRiskDtos(message: String): List<SupplementaryRiskDto> = this.map { it.toSupplementaryRiskDto(message) }
}
