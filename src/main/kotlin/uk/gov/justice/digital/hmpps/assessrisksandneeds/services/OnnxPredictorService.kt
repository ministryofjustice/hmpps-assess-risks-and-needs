package uk.gov.justice.digital.hmpps.assessrisksandneeds.services

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.OffenderAndOffencesDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.PredictorSubType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.PredictorType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RiskPredictorsDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.Score
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ScoreType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.exceptions.ONNXResponseFailure
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.time.format.DateTimeFormatter

@Service
class OnnxPredictorService(@Value("\${onnx-predictors.onnx-path}") private val onnxFilePath: String) {

  fun calculatePredictorScores(
    offenderAndOffences: OffenderAndOffencesDto,
  ): RiskPredictorsDto {

    log.info("Generating RSR score using ONNX runtime for CRN ${offenderAndOffences.crn}")
    val results = OrtEnvironment.getEnvironment().use { env ->
      env.createSession(onnxFilePath).use { session ->

        log.info("Loaded ONNX Runtime file from $onnxFilePath with ${session.numInputs} inputs and ${session.numOutputs}")

        val aggregatedMap = buildStaticTensors(offenderAndOffences, env) +
          buildDynamicTensors(offenderAndOffences, env) +
          buildPreviousOffencesTensors(offenderAndOffences, env) +
          buildCurrentOffencesTensors(offenderAndOffences, env)

        // Identify if any ONNX input parameters are missing and log an error
        val missingTensors = session.inputNames.asSequence().minus(aggregatedMap.keys).toList()
        if (missingTensors.isNotEmpty()) log.error("ONNX parameters not provided: $missingTensors")

        return@use session.run(aggregatedMap)
      }
    }

    val rsr1YearProb = (
      results["rsr_1yr_prob"]
        .orElseThrow { ONNXResponseFailure("rsr1YearProb not returned for CRN ${offenderAndOffences.crn}") }.value as FloatArray
      )

    val rsr2YearProb = (
      results["rsr_2yr_prob"]
        .orElseThrow { ONNXResponseFailure("rsr2YearProb not returned for CRN ${offenderAndOffences.crn}") }.value as FloatArray
      )

    log.debug("Results: year 1 ${rsr1YearProb[0]} - year 2 ${rsr2YearProb[0]}")

    val predictorResults = mapOf(PredictorSubType.RSR to Score(null, rsr1YearProb.getOrNull(0)?.toBigDecimal(), true))

    return RiskPredictorsDto(
      "ONNX-pre-release",
      LocalDateTime.now(),
      PredictorType.RSR,
      ScoreType.DYNAMIC,
      predictorResults,
      emptyList(),
      0
    )
  }

  fun buildStaticTensors(offenderAndOffences: OffenderAndOffencesDto, env: OrtEnvironment): Map<String, OnnxTensor> {
    with(offenderAndOffences) {
      return mapOf(
        "gender" to OnnxTensor.createTensor(env, arrayOf(gender.toOnnxFormat())),
        "dob" to OnnxTensor.createTensor(env, getDateAsLong(dob)),
        "currentOffence" to OnnxTensor.createTensor(env, arrayOf(currentOffence.offenceCode)),
        "offenceSubcode" to OnnxTensor.createTensor(env, arrayOf(currentOffence.offenceSubcode)),
        "homeOfficeCode" to OnnxTensor.createTensor(
          env,
          longArrayOf("${currentOffence.offenceCode}${currentOffence.offenceSubcode}".toLong())
        ),
        "dateOfFirstSanction" to OnnxTensor.createTensor(env, getDateAsLong(dateOfFirstSanction)),
        "ageAtFirstSanction" to OnnxTensor.createTensor(
          env,
          longArrayOf(calculateAgeAtFirstSanction(dob, dateOfFirstSanction))
        ),
        "totalOffences" to OnnxTensor.createTensor(env, longArrayOf(totalOffences.toLong())),
        "totalViolentOffences" to OnnxTensor.createTensor(env, longArrayOf(totalViolentOffences.toLong())),
        "dateOfCurrentConviction" to OnnxTensor.createTensor(env, getDateAsLong(dateOfCurrentConviction)),
        "hasAnySexualOffences" to OnnxTensor.createTensor(env, booleanArrayOf(hasAnySexualOffences)),
        "isCurrentSexualOffence" to OnnxTensor.createTensor(env, booleanArrayOf(isCurrentSexualOffence ?: false)),
        "isCurrentOffenceVictimStranger" to OnnxTensor.createTensor(
          env,
          booleanArrayOf(isCurrentOffenceVictimStranger ?: false)
        ),
        "mostRecentSexualOffenceDate" to OnnxTensor.createTensor(env, getDateAsLong(mostRecentSexualOffenceDate)),
        "totalSexualOffencesInvolvingAnAdult" to OnnxTensor.createTensor(
          env,
          longArrayOf(totalSexualOffencesInvolvingAnAdult?.toLong() ?: -1)
        ),
        "totalSexualOffencesInvolvingAChild" to OnnxTensor.createTensor(
          env,
          longArrayOf(totalSexualOffencesInvolvingAChild?.toLong() ?: -1)
        ),
        "totalSexualOffencesInvolvingChildImages" to OnnxTensor.createTensor(
          env,
          longArrayOf(totalSexualOffencesInvolvingChildImages?.toLong() ?: -1)
        ),
        "totalNonContactSexualOffences" to OnnxTensor.createTensor(
          env,
          longArrayOf(totalNonContactSexualOffences?.toLong() ?: -1)
        ),
        "earliestReleaseDate" to OnnxTensor.createTensor(env, getDateAsLong(earliestReleaseDate)),
        "hasCompletedInterview" to OnnxTensor.createTensor(env, booleanArrayOf(hasCompletedInterview))
      )
    }
  }

  fun buildDynamicTensors(offenderAndOffences: OffenderAndOffencesDto, env: OrtEnvironment): Map<String, OnnxTensor> {
    with(offenderAndOffences.dynamicScoringOffences) {
      return mapOf(
        "hasSuitableAccommodation" to OnnxTensor.createTensor(
          env,
          longArrayOf(this?.hasSuitableAccommodation?.score?.toLong() ?: -1)
        ),
        "employment" to OnnxTensor.createTensor(env, longArrayOf(this?.employment?.score?.toLong() ?: -1)),
        "currentRelationshipWithPartner" to OnnxTensor.createTensor(
          env,
          longArrayOf(this?.currentRelationshipWithPartner?.score?.toLong() ?: -1)
        ),
        "evidenceOfDomesticViolence" to OnnxTensor.createTensor(
          env, booleanArrayOf(this?.evidenceOfDomesticViolence ?: false)
        ),
        "alcoholUseIssues" to OnnxTensor.createTensor(
          env,
          longArrayOf(this?.alcoholUseIssues?.score?.toLong() ?: -1)
        ),
        "bingeDrinkingIssues" to OnnxTensor.createTensor(
          env,
          longArrayOf(this?.bingeDrinkingIssues?.score?.toLong() ?: -1)
        ),
        "impulsivityIssues" to OnnxTensor.createTensor(
          env,
          longArrayOf(this?.impulsivityIssues?.score?.toLong() ?: -1)
        ),
        "temperControlIssues" to OnnxTensor.createTensor(
          env,
          longArrayOf(this?.temperControlIssues?.score?.toLong() ?: -1)
        ),
        "proCriminalAttitudes" to OnnxTensor.createTensor(
          env,
          longArrayOf(this?.proCriminalAttitudes?.score?.toLong() ?: -1)
        )
      )
    }
  }

  fun buildPreviousOffencesTensors(
    offenderAndOffences: OffenderAndOffencesDto,
    env: OrtEnvironment
  ): Map<String, OnnxTensor> {
    with(offenderAndOffences.dynamicScoringOffences?.previousOffences) {
      return mapOf(
        "murderAttempt" to OnnxTensor.createTensor(
          env,
          booleanArrayOf(this?.murderAttempt ?: false)
        ),
        "wounding" to OnnxTensor.createTensor(
          env,
          booleanArrayOf(this?.wounding ?: false)
        ),
        "aggravatedBurglary" to OnnxTensor.createTensor(
          env,
          booleanArrayOf(this?.aggravatedBurglary ?: false)
        ),
        "arson" to OnnxTensor.createTensor(
          env,
          booleanArrayOf(this?.arson ?: false)
        ),
        "criminalDamage" to OnnxTensor.createTensor(
          env,
          booleanArrayOf(this?.criminalDamage ?: false)
        ),
        "kidnapping" to OnnxTensor.createTensor(
          env,
          booleanArrayOf(this?.kidnapping ?: false)
        ),
        "robbery" to OnnxTensor.createTensor(
          env,
          booleanArrayOf(this?.robbery ?: false)
        ),
        "prevfirearmPossession" to OnnxTensor.createTensor(
          env,
          booleanArrayOf(this?.firearmPossession ?: false)
        ),
        "prevoffencesWithWeapon" to OnnxTensor.createTensor(
          env,
          booleanArrayOf(this?.offencesWithWeapon ?: false)
        )
      )
    }
  }

  fun buildCurrentOffencesTensors(
    offenderAndOffences: OffenderAndOffencesDto,
    env: OrtEnvironment
  ): Map<String, OnnxTensor> {
    with(offenderAndOffences.dynamicScoringOffences?.currentOffences) {
      return mapOf(
        "currentfirearmPossession" to OnnxTensor.createTensor(
          env,
          booleanArrayOf(this?.firearmPossession ?: false)
        ),
        "currentoffencesWithWeapon" to OnnxTensor.createTensor(
          env,
          booleanArrayOf(this?.offencesWithWeapon ?: false)
        )
      )
    }
  }

  fun calculateAgeAtFirstSanction(dob: LocalDate?, dateOfFirstSanction: LocalDate?): Long {
    if (dob == null || dateOfFirstSanction == null) return -1
    return Period.between(dob, dateOfFirstSanction).years.toLong()
  }

  fun getDateAsLong(date: LocalDate?): LongArray {
    if (date == null) return longArrayOf()
    return longArrayOf(date.format(DateTimeFormatter.ofPattern("yyyyMMdd")).toLong())
  }

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }
}
