package uk.gov.justice.digital.hmpps.assessrisksandneeds.services

import com.beust.klaxon.Klaxon
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.OffenderAndOffencesDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.PredictorSource
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.PredictorSubType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.PredictorType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RiskPredictorsDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.Score
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ScoreLevel
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ScoreType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.config.RequestData
import uk.gov.justice.digital.hmpps.assessrisksandneeds.jpa.entities.OffenderPredictorsHistoryEntity
import uk.gov.justice.digital.hmpps.assessrisksandneeds.jpa.entities.PredictorEntity
import uk.gov.justice.digital.hmpps.assessrisksandneeds.jpa.respositories.OffenderPredictorsHistoryRepository
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.AssessmentApiRestClient
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.OasysRSRPredictorsDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.exceptions.IncorrectInputParametersException
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.exceptions.PredictorCalculationError

@Service
class RiskPredictorService(
  private val assessmentClient: AssessmentApiRestClient,
  private val offenderPredictorsHistoryRepository: OffenderPredictorsHistoryRepository,
  @Qualifier("globalObjectMapper") private val objectMapper: ObjectMapper
) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  fun calculatePredictorScores(
    predictorType: PredictorType,
    offenderAndOffences: OffenderAndOffencesDto,
    final: Boolean,
    source: PredictorSource,
    sourceId: String,
    algorithmVersion: String? = null
  ): RiskPredictorsDto {
    if (offenderAndOffences.crn == null && final) throw IncorrectInputParametersException("Crn can't be null for a final Predictor calculation, params crn:${offenderAndOffences.crn} and final:$final")
    val errorMessage =
      "Oasys Predictor Calculation failed for offender with CRN ${offenderAndOffences.crn} and $predictorType"
    val predictorCalculation =
      assessmentClient.calculatePredictorTypeScoring(predictorType, offenderAndOffences, algorithmVersion)
        ?: throw PredictorCalculationError(errorMessage)

    if (predictorCalculation.errorCount > 0) log.error("$errorMessage - ${predictorCalculation.errorMessage}")
    val predictors = predictorCalculation.toRiskPredictorsDto(predictorType)
    if (final) {
      log.info("Saving predictors calculation for offender with CRN ${offenderAndOffences.crn} and $predictorType")
      offenderPredictorsHistoryRepository.save(
        predictors.toOffenderPredictorsHistory(
          offenderAndOffences,
          source,
          sourceId
        )
      )
    }
    return predictors
  }

  private fun OasysRSRPredictorsDto.toRiskPredictorsDto(predictorType: PredictorType): RiskPredictorsDto {
    return when (predictorType) {
      PredictorType.RSR -> {
        RiskPredictorsDto(
          algorithmVersion = this.algorithmVersion.toString(),
          calculatedAt = this.calculationDateAndTime,
          type = predictorType,
          scoreType = ScoreType.findByType(this.scoreType!!),
          scores = mapOf(
            PredictorSubType.RSR to Score(
              level = ScoreLevel.findByType(this.rsrBand!!),
              score = this.rsrScore,
              isValid = this.validRsrScore.toBoolean()
            ),
            PredictorSubType.OSPC to Score(
              level = ScoreLevel.findByType(this.ospcBand!!),
              score = this.ospcScore,
              isValid = this.validOspcScore.toBoolean()
            ),
            PredictorSubType.OSPI to Score(
              level = ScoreLevel.findByType(this.ospiBand!!),
              score = this.ospiScore,
              isValid = this.validOspiScore.toBoolean()
            ),
          ),
          errors = this.toErrors(),
          errorCount = this.errorCount
        )
      }
    }
  }

  private fun RiskPredictorsDto.toOffenderPredictorsHistory(
    offenderAndOffencesDto: OffenderAndOffencesDto,
    source: PredictorSource,
    sourceId: String
  ): OffenderPredictorsHistoryEntity {

    val offenderPredictorsHistoryEntity = OffenderPredictorsHistoryEntity(
      predictorType = type,
      algorithmVersion = algorithmVersion,
      calculatedAt = calculatedAt,
      crn = offenderAndOffencesDto.crn!!,
      predictorTriggerSource = source,
      predictorTriggerSourceId = sourceId,
      sourceAnswers = offenderAndOffencesDto.toSourceAnswers(offenderAndOffencesDto.crn),
      createdBy = RequestData.getUserName(),
    )
    this.scores.toPredictors(offenderPredictorsHistoryEntity)
    return offenderPredictorsHistoryEntity
  }

  private fun Map<PredictorSubType, Score>.toPredictors(offenderPredictorsHistoryEntity: OffenderPredictorsHistoryEntity): List<PredictorEntity> {
    return this.entries.map {
      offenderPredictorsHistoryEntity.newPredictor(
        it.key,
        it.value.score,
        it.value.level,
      )
    }
  }

  private fun String?.toBoolean(): Boolean {
    return this?.equals(AnswerType.Y.name) == true
  }

  private fun OasysRSRPredictorsDto.toErrors(): List<String> {
    return if (this.errorCount > 0) this.errorMessage?.split('\n')?.filter { !it.isNullOrBlank() }
      ?: emptyList() else emptyList()
  }

  enum class AnswerType {
    Y, N
  }

  private fun OffenderAndOffencesDto.toSourceAnswers(crn: String): Map<String, Any> {
    val sourceAnswers = SourceAnswers(
      this.gender,
      this.dob,
      this.assessmentDate,
      this.currentOffence.offenceCode,
      this.currentOffence.offenceSubcode,
      this.dateOfFirstSanction,
      this.totalOffences,
      this.totalViolentOffences,
      this.dateOfCurrentConviction,
      this.hasAnySexualOffences,
      this.isCurrentSexualOffence,
      this.isCurrentOffenceVictimStranger,
      this.mostRecentSexualOffenceDate,
      this.totalSexualOffencesInvolvingAnAdult,
      this.totalSexualOffencesInvolvingAnAdult,
      this.totalSexualOffencesInvolvingChildImages,
      this.totalNonContactSexualOffences,
      this.earliestReleaseDate,
      this.hasCompletedInterview,
      this.dynamicScoringOffences?.hasSuitableAccommodation,
      this.dynamicScoringOffences?.employment,
      this.dynamicScoringOffences?.currentRelationshipWithPartner,
      this.dynamicScoringOffences?.evidenceOfDomesticViolence,
      this.dynamicScoringOffences?.isPerpetrator,
      this.dynamicScoringOffences?.alcoholUseIssues,
      this.dynamicScoringOffences?.bingeDrinkingIssues,
      this.dynamicScoringOffences?.impulsivityIssues,
      this.dynamicScoringOffences?.temperControlIssues,
      this.dynamicScoringOffences?.proCriminalAttitudes,
      this.dynamicScoringOffences?.previousOffences?.murderAttempt,
      this.dynamicScoringOffences?.previousOffences?.wounding,
      this.dynamicScoringOffences?.previousOffences?.aggravatedBurglary,
      this.dynamicScoringOffences?.previousOffences?.arson,
      this.dynamicScoringOffences?.previousOffences?.criminalDamage,
      this.dynamicScoringOffences?.previousOffences?.kidnapping,
      this.dynamicScoringOffences?.previousOffences?.firearmPossession,
      this.dynamicScoringOffences?.previousOffences?.robbery,
      this.dynamicScoringOffences?.previousOffences?.offencesWithWeapon,
      this.dynamicScoringOffences?.currentOffences?.firearmPossession,
      this.dynamicScoringOffences?.currentOffences?.offencesWithWeapon,
    )

    return Klaxon().parse<Map<String, Any>>(
      objectMapper.writeValueAsString(sourceAnswers)
    ) ?: throw PredictorCalculationError("Error parsing answers for CRN: $crn")
  }
}
