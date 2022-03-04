package uk.gov.justice.digital.hmpps.assessrisksandneeds.services

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OnnxValue
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.OffenderAndOffencesDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.PredictorSubType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.PredictorType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RiskPredictorsDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.Score
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ScoreLevel
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ScoreType
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.time.format.DateTimeFormatter

private const val onnxVersion = "1.0.0"

@Profile("onnx-rsr")
@Service
class OnnxCalculatorServiceImpl(private val ortEnvironment: OrtEnvironment, private val ortSession: OrtSession) :
  RiskCalculatorService {

  override fun calculatePredictorScores(
    predictorType: PredictorType,
    offenderAndOffences: OffenderAndOffencesDto,
    algorithmVersion: String?
  ): RiskPredictorsDto {

    val validationErrors = validateInputParams(offenderAndOffences)
    if (validationErrors.isNotEmpty()) return RiskPredictorsDto(
      onnxVersion,
      LocalDateTime.now(),
      PredictorType.RSR,
      null,
      emptyMap(),
      validationErrors,
      validationErrors.count()
    ).also { log.info("${validationErrors.count()} found for CRN ${offenderAndOffences.crn}") }

    log.info("Generating RSR score using ONNX runtime for CRN ${offenderAndOffences.crn}")
    val results = getResults(buildTensorsFromInputParameters(offenderAndOffences))

    val rsr1YearBriefProb = getBigDecimalResultFor("rsr_brief_1yr_prob", results)
    val rsr2YearBriefProb = getBigDecimalResultFor("rsr_brief_2yr_prob", results)
    val rsr1YearExtendedProb = getBigDecimalResultFor("rsr_extended_1yr_prob", results)
    val rsr2YearExtendedProb = getBigDecimalResultFor("rsr_extended_2yr_prob", results)
    val rsrBriefBand = getScoreBandResultFor("rsr_band_brief", results)
    val rsrExtendedBand = getScoreBandResultFor("rsr_band_extended", results)

    val ospC1YearProb = getBigDecimalResultFor("osp_c_1yr_prob", results)
    val ospC2YearProb = getBigDecimalResultFor("osp_c_2yr_prob", results)
    val ospI1YearProb = getBigDecimalResultFor("osp_i_1yr_prob", results)
    val ospI2YearProb = getBigDecimalResultFor("osp_i_2yr_prob", results)
    val ospCBand = getScoreBandResultFor("osp_4band", results)
    val ospIBand = getScoreBandResultFor("osp_iband", results)

    val snsv1YearBriefProb = getBigDecimalResultFor("snsv_brief_1yr_prob", results)
    val snsv2YearBriefProb = getBigDecimalResultFor("snsv_brief_2yr_prob", results)
    val snsv1YearExtendedProb = getBigDecimalResultFor("snsv_extended_1yr_prob", results)
    val snsv2YearExtendedProb = getBigDecimalResultFor("snsv_extended_2yr_prob", results)

    val predictorResults: MutableMap<PredictorSubType, Score> = mutableMapOf()
    val scoreType = if (rsr2YearExtendedProb != null) ScoreType.DYNAMIC else ScoreType.STATIC

    if (rsr1YearBriefProb != null) {
      predictorResults[PredictorSubType.RSR_1YR_BRIEF] = Score(null, rsr1YearBriefProb, true)
    }
    if (rsr2YearBriefProb != null) {
      predictorResults[PredictorSubType.RSR_2YR_BRIEF] = Score(rsrBriefBand, rsr2YearBriefProb, true)
      predictorResults[PredictorSubType.RSR] = Score(
        rsrBriefBand,
        rsr2YearBriefProb.multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP),
        true
      )
    }

    if (rsr1YearExtendedProb != null) {
      predictorResults[PredictorSubType.RSR_1YR_EXTENDED] = Score(null, rsr1YearExtendedProb, true)
    }
    if (rsr2YearExtendedProb != null) {
      predictorResults[PredictorSubType.RSR_2YR_EXTENDED] =
        Score(rsrExtendedBand, rsr2YearExtendedProb, true)
      predictorResults[PredictorSubType.RSR] = Score(
        rsrExtendedBand,
        rsr2YearExtendedProb.multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP),
        true
      )
    }

    if (ospC1YearProb != null) {
      predictorResults[PredictorSubType.OSPC_1YR] = Score(null, ospC1YearProb, true)
    }
    if (ospC2YearProb != null) {
      predictorResults[PredictorSubType.OSPC_2YR] = Score(ospCBand, ospC2YearProb, true)
      predictorResults[PredictorSubType.OSPC] = Score(
        ospCBand,
        ospC2YearProb.multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP),
        true
      )
    }

    if (ospI1YearProb != null) {
      predictorResults[PredictorSubType.OSPI_1YR] = Score(null, ospI1YearProb, true)
    }
    if (ospI2YearProb != null) {
      predictorResults[PredictorSubType.OSPI_2YR] = Score(ospIBand, ospI2YearProb, true)
      predictorResults[PredictorSubType.OSPI] = Score(
        ospIBand,
        ospI2YearProb.multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP),
        true
      )
    }

    if (snsv1YearBriefProb != null) {
      predictorResults[PredictorSubType.SNSV_1YR_BRIEF] = Score(null, snsv1YearBriefProb, true)
    }
    if (snsv2YearBriefProb != null) {
      predictorResults[PredictorSubType.SNSV_2YR_BRIEF] = Score(null, snsv2YearBriefProb, true)
      predictorResults[PredictorSubType.SNSV] = Score(
        null,
        snsv2YearBriefProb.multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP),
        true
      )
    }

    if (snsv1YearExtendedProb != null) {
      predictorResults[PredictorSubType.SNSV_1YR_EXTENDED] = Score(null, snsv1YearExtendedProb, true)
    }
    if (snsv2YearExtendedProb != null) {
      predictorResults[PredictorSubType.SNSV_2YR_EXTENDED] = Score(null, snsv2YearExtendedProb, true)
      predictorResults[PredictorSubType.SNSV] = Score(
        null,
        snsv2YearExtendedProb.multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP),
        true
      )
    }

    return RiskPredictorsDto(
      onnxVersion,
      LocalDateTime.now(),
      PredictorType.RSR,
      scoreType,
      predictorResults
    )
  }

  private fun getBigDecimalResultFor(outputFieldName: String, results: Map<String, OnnxValue>): BigDecimal? {
    return (results[outputFieldName]?.value as? FloatArray)?.filterNot { it.isNaN() }?.getOrNull(0)?.toBigDecimal()
  }

  private fun getScoreBandResultFor(outputFieldName: String, results: Map<String, OnnxValue>): ScoreLevel? {
    when (results[outputFieldName]?.value) {
      is FloatArray -> return ScoreLevel.findByOrdinal(
        (results[outputFieldName]?.value as? FloatArray)?.getOrNull(0)?.toInt()
      )
      is LongArray -> return ScoreLevel.findByOrdinal(
        (results[outputFieldName]?.value as? LongArray)?.getOrNull(0)?.toInt()
      )
    }

    return ScoreLevel.findByOrdinal(
      (results[outputFieldName]?.value as? FloatArray)?.getOrNull(0)?.toInt()
    )
  }

  fun getResults(inputTensors: Map<String, OnnxTensor>): Map<String, OnnxValue> {
    val results = ortSession.run(inputTensors)
    return results.associate { it.key to it.value }
  }

  fun validateInputParams(offenderAndOffences: OffenderAndOffencesDto): List<String> {

    val errors = mutableListOf<String>()

    with(offenderAndOffences) {

      if (dateOfCurrentConviction.isAfter(LocalDate.now())) errors.add("Date of current conviction cannot be in the future")
      if (dateOfCurrentConviction.isBefore(dateOfFirstSanction)) errors.add("Date of current conviction cannot be before the date of first conviction")

      if (dateOfFirstSanction.isAfter(LocalDate.now())) errors.add("Date of first sanction cannot be in the future")
      if (Period.between(
          dob,
          dateOfFirstSanction
        ).years < 8
      ) errors.add("The individual must be aged 8 or older on the date of first sanction")

      if (earliestReleaseDate.isBefore(dob)) errors.add("Date of earliest possible release from custody must be later than the individual’s date of birth")
      if (Period.between(
          dob,
          earliestReleaseDate
        ).years > 110
      ) errors.add("The individual must be aged 110 or younger on commencement")

      if (totalOffences < 1) errors.add("Total offences must be at least 1 including the current offence")
      if (totalViolentOffences > totalOffences) errors.add("Violent offences must less than equal to the total number of offences")

      if (hasAnySexualOffences) {
        if (isCurrentSexualOffence == null) errors.add("Has current sexual offence is required")
        if (mostRecentSexualOffenceDate == null) errors.add("Date of most recent sexual offence is required")
        if (isCurrentOffenceVictimStranger == null) errors.add("Does the current offence involve a victim who was a stranger is required")
        if (mostRecentSexualOffenceDate?.isAfter(LocalDate.now()) == true) errors.add("Date of most recent sexual offence cannot be in the future")
        if (mostRecentSexualOffenceDate?.isBefore(dob) == true) errors.add("Date of most recent sexual offence must be later than the individual’s date of birth")

        if (totalSexualOffencesInvolvingAnAdult == null) errors.add("Number of previous or current sanctions involving contact adult sexual or sexually motivated offences is required")
        if (totalSexualOffencesInvolvingAChild == null) errors.add("Number of previous or current sanctions involving contact child sexual or sexually motivated offences is required")
        if (totalSexualOffencesInvolvingChildImages == null) errors.add("Number of previous or current sanctions involving indecent child image sexual or sexually motivated offences is required")
        if (totalNonContactSexualOffences == null) errors.add("Number of previous or current sanctions involving other non-contact sexual or sexually motivated offences is required")

        if (listOfNotNull(
            totalNonContactSexualOffences,
            totalSexualOffencesInvolvingAnAdult,
            totalSexualOffencesInvolvingAChild,
            totalSexualOffencesInvolvingChildImages,
            totalNonContactSexualOffences
          ).sum() < 1
        ) {
          errors.add("At least one sexual offence is required")
        }
      }

      if (hasCompletedInterview) {
        with(dynamicScoringOffences) {
          if (this?.hasSuitableAccommodation == null) errors.add("Is the individual living in suitable accommodation is required")
          if (this?.employment == null) errors.add("Is the person unemployed or will be unemployed upon release is required")
          if (this?.currentRelationshipWithPartner == null) errors.add("What is the person's current relationship with their partner is required")
          if (this?.evidenceOfDomesticViolence == null) errors.add("Is there evidence that the individual is a perpetrator of domestic abuse is required")
          if (this?.alcoholUseIssues == null) errors.add("Is the person's current use of alcohol a problem is required")
          if (this?.bingeDrinkingIssues == null) errors.add("Is there evidence of binge drinking or excessive use of alcohol in the last 6 months is required")
          if (this?.impulsivityIssues == null) errors.add("Is impulsivity a problem for the individual is required")
          if (this?.temperControlIssues == null) errors.add("Is temper control a problem for the individual is required")
          if (this?.proCriminalAttitudes == null) errors.add("Does the individual have pro-criminal attitudes is required")
          if (this?.currentOffences?.firearmPossession == null) errors.add("Possession of a firearm with intent to endanger life or resist arrest is required")
          if (this?.currentOffences?.offencesWithWeapon == null) errors.add("Any other offence involving possession and/or use of weapons is required")
        }

        with(dynamicScoringOffences?.previousOffences) {
          if (this?.murderAttempt == null) errors.add("Murder/attempted murder/threat or conspiracy to murder/manslaughter is required")
          if (this?.wounding == null) errors.add("Wounding/GBH (Sections 18/20 Offences Against the Person Act 1861) is required")
          if (this?.aggravatedBurglary == null) errors.add("Aggravated burglary is required")
          if (this?.arson == null) errors.add("Arson is required")
          if (this?.criminalDamage == null) errors.add("Criminal damage with intent to endanger life is required")
          if (this?.kidnapping == null) errors.add("Kidnapping/false imprisonment")
          if (this?.firearmPossession == null) errors.add("Possession of a firearm with intent to endanger life or resist arrest is required")
          if (this?.robbery == null) errors.add("Robbery is required")
          if (this?.offencesWithWeapon == null) errors.add("Any other offence involving possession and/or use of weapons is required")
        }
      }
    }
    return errors
  }

  fun buildTensorsFromInputParameters(offenderAndOffences: OffenderAndOffencesDto): MutableMap<String, OnnxTensor> {
    val aggregatedMap = mutableMapOf<String, OnnxTensor>()
    aggregatedMap += buildStaticTensors(offenderAndOffences) +
      buildDynamicTensors(offenderAndOffences) +
      buildPreviousOffencesTensors(offenderAndOffences) +
      buildCurrentOffencesTensors(offenderAndOffences)

    return aggregatedMap
  }

  private fun buildStaticTensors(offenderAndOffences: OffenderAndOffencesDto): Map<String, OnnxTensor> {
    with(offenderAndOffences) {
      return mapOf(
        "crn" to OnnxTensor.createTensor(ortEnvironment, arrayOf(crn)),
        "gender" to OnnxTensor.createTensor(ortEnvironment, arrayOf(gender.toOnnxFormat())),
        "dob" to OnnxTensor.createTensor(ortEnvironment, getDateAsString(dob)),
        "assessmentDate" to OnnxTensor.createTensor(ortEnvironment, getDateAsString(assessmentDate)),
        "currentOffence" to OnnxTensor.createTensor(ortEnvironment, longArrayOf(currentOffence.offenceCode.toLong())),
        "offenceSubcode" to OnnxTensor.createTensor(
          ortEnvironment,
          longArrayOf(currentOffence.offenceSubcode.toLong())
        ),
        "homeOfficeCode" to OnnxTensor.createTensor(
          ortEnvironment,
          longArrayOf("${currentOffence.offenceCode}${currentOffence.offenceSubcode}".toLong())
        ),
        "dateOfFirstSanction" to OnnxTensor.createTensor(ortEnvironment, getDateAsString(dateOfFirstSanction)),
        "totalOffences" to OnnxTensor.createTensor(ortEnvironment, longArrayOf(totalOffences.toLong())),
        "totalViolentOffences" to OnnxTensor.createTensor(ortEnvironment, longArrayOf(totalViolentOffences.toLong())),
        "dateOfCurrentConviction" to OnnxTensor.createTensor(ortEnvironment, getDateAsString(dateOfCurrentConviction)),
        "hasAnySexualOffences" to OnnxTensor.createTensor(ortEnvironment, booleanArrayOf(hasAnySexualOffences)),
        "isCurrentSexualOffence" to OnnxTensor.createTensor(
          ortEnvironment,
          booleanArrayOf(isCurrentSexualOffence ?: false)
        ),
        "isCurrentOffenceVictimStranger" to OnnxTensor.createTensor(
          ortEnvironment,
          booleanArrayOf(isCurrentOffenceVictimStranger ?: false)
        ),
        "mostRecentSexualOffenceDate" to OnnxTensor.createTensor(
          ortEnvironment,
          getDateAsString(mostRecentSexualOffenceDate)
        ),
        "totalSexualOffencesInvolvingAnAdult" to OnnxTensor.createTensor(
          ortEnvironment,
          longArrayOf(totalSexualOffencesInvolvingAnAdult?.toLong() ?: -1)
        ),
        "totalSexualOffencesInvolvingAChild" to OnnxTensor.createTensor(
          ortEnvironment,
          longArrayOf(totalSexualOffencesInvolvingAChild?.toLong() ?: -1)
        ),
        "totalSexualOffencesInvolvingChildImages" to OnnxTensor.createTensor(
          ortEnvironment,
          longArrayOf(totalSexualOffencesInvolvingChildImages?.toLong() ?: -1)
        ),
        "totalNonContactSexualOffences" to OnnxTensor.createTensor(
          ortEnvironment,
          longArrayOf(totalNonContactSexualOffences?.toLong() ?: -1)
        ),
        "earliestReleaseDate" to OnnxTensor.createTensor(ortEnvironment, getDateAsString(earliestReleaseDate)),
        "hasCompletedInterview" to OnnxTensor.createTensor(ortEnvironment, booleanArrayOf(hasCompletedInterview))
      )
    }
  }

  private fun buildDynamicTensors(offenderAndOffences: OffenderAndOffencesDto): Map<String, OnnxTensor> {
    with(offenderAndOffences.dynamicScoringOffences) {
      return mapOf(
        "hasSuitableAccommodation" to OnnxTensor.createTensor(
          ortEnvironment,
          longArrayOf(this?.hasSuitableAccommodation?.score?.toLong() ?: -1)
        ),
        "employment" to OnnxTensor.createTensor(ortEnvironment, longArrayOf(this?.employment?.score?.toLong() ?: -1)),
        "currentRelationshipWithPartner" to OnnxTensor.createTensor(
          ortEnvironment,
          longArrayOf(this?.currentRelationshipWithPartner?.score?.toLong() ?: -1)
        ),
        "evidenceOfDomesticViolence" to OnnxTensor.createTensor(
          ortEnvironment, booleanArrayOf(this?.evidenceOfDomesticViolence ?: false)
        ),
        "alcoholUseIssues" to OnnxTensor.createTensor(
          ortEnvironment,
          longArrayOf(this?.alcoholUseIssues?.score?.toLong() ?: -1)
        ),
        "bingeDrinkingIssues" to OnnxTensor.createTensor(
          ortEnvironment,
          longArrayOf(this?.bingeDrinkingIssues?.score?.toLong() ?: -1)
        ),
        "impulsivityIssues" to OnnxTensor.createTensor(
          ortEnvironment,
          longArrayOf(this?.impulsivityIssues?.score?.toLong() ?: -1)
        ),
        "temperControlIssues" to OnnxTensor.createTensor(
          ortEnvironment,
          longArrayOf(this?.temperControlIssues?.score?.toLong() ?: -1)
        ),
        "proCriminalAttitudes" to OnnxTensor.createTensor(
          ortEnvironment,
          longArrayOf(this?.proCriminalAttitudes?.score?.toLong() ?: -1)
        )
      )
    }
  }

  private fun buildPreviousOffencesTensors(
    offenderAndOffences: OffenderAndOffencesDto
  ): Map<String, OnnxTensor> {
    with(offenderAndOffences.dynamicScoringOffences?.previousOffences) {
      return mapOf(
        "murderAttempt" to OnnxTensor.createTensor(
          ortEnvironment,
          booleanArrayOf(this?.murderAttempt ?: false)
        ),
        "wounding" to OnnxTensor.createTensor(
          ortEnvironment,
          booleanArrayOf(this?.wounding ?: false)
        ),
        "aggravatedBurglary" to OnnxTensor.createTensor(
          ortEnvironment,
          booleanArrayOf(this?.aggravatedBurglary ?: false)
        ),
        "arson" to OnnxTensor.createTensor(
          ortEnvironment,
          booleanArrayOf(this?.arson ?: false)
        ),
        "criminalDamage" to OnnxTensor.createTensor(
          ortEnvironment,
          booleanArrayOf(this?.criminalDamage ?: false)
        ),
        "kidnapping" to OnnxTensor.createTensor(
          ortEnvironment,
          booleanArrayOf(this?.kidnapping ?: false)
        ),
        "robbery" to OnnxTensor.createTensor(
          ortEnvironment,
          booleanArrayOf(this?.robbery ?: false)
        ),
        "firearmPossession" to OnnxTensor.createTensor(
          ortEnvironment,
          booleanArrayOf(this?.firearmPossession ?: false)
        ),
        "offencesWithWeapon" to OnnxTensor.createTensor(
          ortEnvironment,
          booleanArrayOf(this?.offencesWithWeapon ?: false)
        )
      )
    }
  }

  private fun buildCurrentOffencesTensors(
    offenderAndOffences: OffenderAndOffencesDto,
  ): Map<String, OnnxTensor> {
    with(offenderAndOffences.dynamicScoringOffences?.currentOffences) {
      return mapOf(
        "currentfirearmPossession" to OnnxTensor.createTensor(
          ortEnvironment,
          booleanArrayOf(this?.firearmPossession ?: false)
        ),
        "currentoffencesWithWeapon" to OnnxTensor.createTensor(
          ortEnvironment,
          booleanArrayOf(this?.offencesWithWeapon ?: false)
        )
      )
    }
  }

  fun getDateAsString(date: LocalDate?): Array<String> {
    if (date == null) return arrayOf("null")
    return arrayOf(date.format(DateTimeFormatter.ofPattern("yyyyMMdd")))
  }

  fun getDateAsString(date: LocalDateTime?): Array<String> {
    if (date == null) return arrayOf("null")
    return arrayOf(date.format(DateTimeFormatter.ofPattern("yyyyMMdd")))
  }

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }
}
