package uk.gov.justice.digital.hmpps.assessrisksandneeds.integration

import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assumptions
import org.assertj.core.api.SoftAssertions
import org.assertj.core.data.Offset
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.CurrentOffenceDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.CurrentOffencesDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.DynamicScoringOffencesDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.EmploymentType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.Gender
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.OffenderAndOffencesDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.PredictorSubType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.PredictorType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.PreviousOffencesDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ProblemsLevel
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RiskPredictorsDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.Score
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ScoreLevel
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.stream.Stream

private const val offset = 0.00001

@AutoConfigureWebTestClient(timeout = "360000000")
@DisplayName("ONNX RSR Integration Tests")
@ActiveProfiles("test", "onnx-rsr", inheritProfiles = false)
@EnabledIfEnvironmentVariable(
  named = "ENABLE_ONNX_INTEGRATION_TESTS",
  matches = "true",
  disabledReason = "ONNX Integration Tests disabled until ONNX file can be deployed at runtime into tests"
)
class ONNXRSRCalculatorTest() : IntegrationTestBase() {

  companion object {

    fun convertLongDateToLocalDate(longDate: Long?): LocalDate? {
      if (longDate == null) return null
      val year = longDate.toString().substring(0, 4).toInt()
      val month = longDate.toString().substring(4, 6).toInt()
      val day = longDate.toString().substring(6, 8).toInt()
      return LocalDate.of(year, month, day)
    }

    @JvmStatic
    fun offenderInputsAndOutputs(): Stream<Arguments> {

      val inputJsonFile = "./src/test/resources/onnx-test-fixtures/rsr_input_v0.0.0.json"
      val outputJsonFile = "./src/test/resources/onnx-test-fixtures/rsr_output_v0.0.0.json"
      val parameters: MutableList<Pair<OffenderAndOffencesDto, RiskPredictorsDto>> = mutableListOf()
      val parser = Parser.default()
      val inputJson = parser.parse(inputJsonFile) as JsonArray<JsonObject>
      val outputJson = parser.parse(outputJsonFile) as JsonArray<JsonObject>

      inputJson.forEachIndexed { index, jsonObject ->
        val offenderAndOffencesDto =
          OffenderAndOffencesDto(
            crn = jsonObject.string("crn"),
            gender = Gender.fromONNX(jsonObject.string("gender")!!),
            dob = convertLongDateToLocalDate(jsonObject.string("dob")?.toLong())!!,
            assessmentDate = LocalDateTime.of(
              convertLongDateToLocalDate(
                jsonObject.string("assessmentDate")?.toLong()
              )!!,
              LocalTime.MIN
            ),
            currentOffence = CurrentOffenceDto(
              jsonObject.int("currentOffence").toString(),
              jsonObject.int("offenceSubcode").toString().padStart(2, '0')
            ),
            dateOfFirstSanction = convertLongDateToLocalDate(jsonObject.string("dateOfFirstSanction")?.toLong())!!,
            totalOffences = jsonObject.int("totalOffences")!!,
            totalViolentOffences = jsonObject.int("totalViolentOffences")!!,
            dateOfCurrentConviction = convertLongDateToLocalDate(
              jsonObject.string("dateOfCurrentConviction")?.toLong()
            )!!,
            hasAnySexualOffences = jsonObject.boolean("hasAnySexualOffences")!!,
            isCurrentSexualOffence = jsonObject.boolean("isCurrentSexualOffence"),
            isCurrentOffenceVictimStranger = jsonObject.boolean("isCurrentOffenceVictimStranger"),
            mostRecentSexualOffenceDate = convertLongDateToLocalDate(
              jsonObject.string("mostRecentSexualOffenceDate")?.toLong()
            ),
            totalSexualOffencesInvolvingAnAdult = jsonObject.int("totalSexualOffencesInvolvingAnAdult"),
            totalSexualOffencesInvolvingAChild = jsonObject.int("totalSexualOffencesInvolvingAChild"),
            totalSexualOffencesInvolvingChildImages = jsonObject.int("totalSexualOffencesInvolvingChildImages"),
            totalNonContactSexualOffences = jsonObject.int("totalNonContactSexualOffences"),
            earliestReleaseDate = convertLongDateToLocalDate(jsonObject.string("earliestReleaseDate")?.toLong())!!,
            hasCompletedInterview = jsonObject.boolean("hasCompletedInterview")!!,
            dynamicScoringOffences = DynamicScoringOffencesDto(
              hasSuitableAccommodation = ProblemsLevel.getByValue(jsonObject.int("hasSuitableAccommodation")),
              employment = EmploymentType.getByValue(jsonObject.int("employment")),
              currentRelationshipWithPartner = ProblemsLevel.getByValue(jsonObject.int("currentRelationshipWithPartner")),
              evidenceOfDomesticViolence = jsonObject.boolean("evidenceOfDomesticViolence"),
              isPerpetrator = jsonObject.boolean("evidenceOfDomesticViolence"),
              alcoholUseIssues = ProblemsLevel.getByValue(jsonObject.int("alcoholUseIssues")),
              bingeDrinkingIssues = ProblemsLevel.getByValue(jsonObject.int("bingeDrinkingIssues")),
              impulsivityIssues = ProblemsLevel.getByValue(jsonObject.int("impulsivityIssues")),
              temperControlIssues = ProblemsLevel.getByValue(jsonObject.int("temperControlIssues")),
              proCriminalAttitudes = ProblemsLevel.getByValue(jsonObject.int("proCriminalAttitudes")),
              previousOffences = PreviousOffencesDto(
                murderAttempt = jsonObject.boolean("murderAttempt"),
                wounding = jsonObject.boolean("wounding"),
                aggravatedBurglary = jsonObject.boolean("aggravatedBurglary"),
                arson = jsonObject.boolean("arson"),
                criminalDamage = jsonObject.boolean("criminalDamage"),
                kidnapping = jsonObject.boolean("kidnapping"),
                firearmPossession = jsonObject.boolean("firearmPossession"),
                robbery = jsonObject.boolean("robbery"),
                offencesWithWeapon = jsonObject.boolean("offencesWithWeapon")
              ),
              currentOffences = CurrentOffencesDto(
                firearmPossession = jsonObject.boolean("currentfirearmPossession"),
                offencesWithWeapon = jsonObject.boolean("currentoffencesWithWeapon"),
              )
            )
          )

        val result = RiskPredictorsDto(
          type = PredictorType.RSR,
          scores = mapOf(
            PredictorSubType.RSR_1YR_BRIEF to Score(
              score = outputJson[index].float("rsr_brief_1yr_prob")?.toBigDecimal(), isValid = true, level = null
            ),
            PredictorSubType.RSR_2YR_BRIEF to Score(
              score = outputJson[index].float("rsr_brief_2yr_prob")?.toBigDecimal(), isValid = true,
              level = ScoreLevel.findByType(outputJson[index].string("rsr_band_brief"))
            ),
            PredictorSubType.RSR_1YR_EXTENDED to Score(
              score = outputJson[index].float("rsr_extended_1yr_prob")?.toBigDecimal(), isValid = true, level = null
            ),
            PredictorSubType.RSR_2YR_EXTENDED to Score(
              score = outputJson[index].float("rsr_extended_2yr_prob")?.toBigDecimal(), isValid = true,
              level = ScoreLevel.findByType(outputJson[index].string("rsr_band_extended"))
            ),
            PredictorSubType.OSPI_1YR to Score(
              score = outputJson[index].float("osp_i_1yr_prob")?.toBigDecimal(),
              isValid = true,
              level = null
            ),
            PredictorSubType.OSPI_2YR to Score(
              score = outputJson[index].float("osp_i_2yr_prob")?.toBigDecimal(),
              isValid = true,
              level = null
            ),
            PredictorSubType.OSPC_1YR to Score(
              score = outputJson[index].float("osp_c_1yr_prob")?.toBigDecimal(),
              isValid = true,
              level = null
            ),
            PredictorSubType.OSPC_2YR to Score(
              score = outputJson[index].float("osp_c_2yr_prob")?.toBigDecimal(),
              isValid = true,
              level = ScoreLevel.findByType(outputJson[index].string("osp_c_band"))
            ),
            PredictorSubType.SNSV_1YR_BRIEF to Score(
              score = outputJson[index].float("snsv_brief_1yr_prob")?.toBigDecimal(), isValid = true, level = null
            ),
            PredictorSubType.SNSV_2YR_BRIEF to Score(
              score = outputJson[index].float("snsv_brief_2yr_prob")?.toBigDecimal(), isValid = true, level = null
            ),
            PredictorSubType.SNSV_1YR_EXTENDED to Score(
              score = outputJson[index].float("snsv_extended_1yr_prob")?.toBigDecimal(), isValid = true, level = null
            ),
            PredictorSubType.SNSV_2YR_EXTENDED to Score(
              score = outputJson[index].float("snsv_extended_2yr_prob")?.toBigDecimal(), isValid = true, level = null
            )
          ),
          errorCount = 0,
          calculatedAt = LocalDateTime.now(),
          algorithmVersion = "", scoreType = null
        )
        parameters.add(Pair(offenderAndOffencesDto, result))
      }
      return parameters.map { Arguments.of(it.first, it.second) }.stream()
    }
  }

  @ParameterizedTest
  @MethodSource("offenderInputsAndOutputs")
  fun `calculate rsr predictors returns rsr scoring using Onnx file`(input: OffenderAndOffencesDto, output: RiskPredictorsDto) {
    webTestClient.post()
      .uri("/risks/predictors/RSR?final=false&source=ASSESSMENTS_API&sourceId=90f2b674-ae1c-488d-8b85-0251708ef6b6")
      .header("Content-Type", "application/json")
      .headers(setAuthorisation(user = "Gary C", roles = listOf("ROLE_PROBATION")))
      .bodyValue(input)
      .exchange()
      .expectStatus().isEqualTo(HttpStatus.OK)
      .expectBody<RiskPredictorsDto>()
      .consumeWith {
        val result = it.responseBody
        Assumptions.assumeThat(result.errorCount)
          .withFailMessage("Test skipped due to validation errors", result.errors)
          .isEqualTo(0)

        SoftAssertions.assertSoftly {

          if (output.scores[PredictorSubType.RSR_2YR_EXTENDED]?.level != null) {
            assertThat(
              result.scores[PredictorSubType.RSR_2YR_EXTENDED]?.level?.ordinal
            ).isEqualTo(
              output.scores[PredictorSubType.RSR_2YR_EXTENDED]?.level?.ordinal
            )
          }
          if (output.scores[PredictorSubType.RSR_2YR_BRIEF]?.level != null) {
            assertThat(
              result.scores[PredictorSubType.RSR_2YR_BRIEF]?.level?.ordinal
            ).isEqualTo(
              output.scores[PredictorSubType.RSR_2YR_BRIEF]?.level?.ordinal
            )
          }
          if (output.scores[PredictorSubType.OSPC_2YR]?.level != null) {
            assertThat(
              result.scores[PredictorSubType.OSPC_2YR]?.level?.ordinal
            ).isEqualTo(
              output.scores[PredictorSubType.OSPC_2YR]?.level?.ordinal
            )
          }

          if (output.scores[PredictorSubType.RSR_2YR_EXTENDED]?.score != null) {
            assertThat(
              result.scores[PredictorSubType.RSR_2YR_EXTENDED]?.score
            ).isCloseTo(
              output.scores[PredictorSubType.RSR_2YR_EXTENDED]?.score, Offset.offset(BigDecimal.valueOf(offset))
            )
          }
          if (output.scores[PredictorSubType.RSR_1YR_EXTENDED]?.score != null) {
            assertThat(
              result.scores[PredictorSubType.RSR_1YR_EXTENDED]?.score
            ).isCloseTo(
              output.scores[PredictorSubType.RSR_1YR_EXTENDED]?.score, Offset.offset(BigDecimal.valueOf(offset))
            )
          }
          if (output.scores[PredictorSubType.RSR_2YR_BRIEF]?.score != null) {
            assertThat(result.scores[PredictorSubType.RSR_2YR_BRIEF]?.score).isCloseTo(
              output.scores[PredictorSubType.RSR_2YR_BRIEF]?.score, Offset.offset(BigDecimal.valueOf(offset))
            )
          }
          if (output.scores[PredictorSubType.RSR_1YR_BRIEF]?.score != null) {
            assertThat(result.scores[PredictorSubType.RSR_1YR_BRIEF]?.score).isCloseTo(
              output.scores[PredictorSubType.RSR_1YR_BRIEF]?.score, Offset.offset(BigDecimal.valueOf(offset))
            )
          }
          if (output.scores[PredictorSubType.SNSV_2YR_EXTENDED]?.score != null) {
            assertThat(
              result.scores[PredictorSubType.SNSV_2YR_EXTENDED]?.score
            ).isCloseTo(
              output.scores[PredictorSubType.SNSV_2YR_EXTENDED]?.score, Offset.offset(BigDecimal.valueOf(offset))
            )
          }
          if (output.scores[PredictorSubType.SNSV_1YR_EXTENDED]?.score != null) {
            assertThat(
              result.scores[PredictorSubType.SNSV_1YR_EXTENDED]?.score
            ).isCloseTo(
              output.scores[PredictorSubType.SNSV_1YR_EXTENDED]?.score, Offset.offset(BigDecimal.valueOf(offset))
            )
          }
          if (output.scores[PredictorSubType.SNSV_2YR_BRIEF]?.score != null) {
            assertThat(
              result.scores[PredictorSubType.SNSV_2YR_BRIEF]?.score
            ).isCloseTo(
              output.scores[PredictorSubType.SNSV_2YR_BRIEF]?.score, Offset.offset(BigDecimal.valueOf(offset))
            )
          }
          if (output.scores[PredictorSubType.SNSV_1YR_BRIEF]?.score != null) {
            assertThat(
              result.scores[PredictorSubType.SNSV_1YR_BRIEF]?.score
            ).isCloseTo(
              output.scores[PredictorSubType.SNSV_1YR_BRIEF]?.score, Offset.offset(BigDecimal.valueOf(offset))
            )
          }
          if (output.scores[PredictorSubType.OSPI_2YR]?.score != null) {
            assertThat(result.scores[PredictorSubType.OSPI_2YR]?.score).isCloseTo(
              output.scores[PredictorSubType.OSPI_2YR]?.score, Offset.offset(BigDecimal.valueOf(offset))
            )
          }
          if (output.scores[PredictorSubType.OSPI_1YR]?.score != null) {
            assertThat(result.scores[PredictorSubType.OSPI_1YR]?.score).isCloseTo(
              output.scores[PredictorSubType.OSPI_1YR]?.score, Offset.offset(BigDecimal.valueOf(offset))
            )
          }
          if (output.scores[PredictorSubType.OSPC_2YR]?.score != null) {
            assertThat(result.scores[PredictorSubType.OSPC_2YR]?.score).isCloseTo(
              output.scores[PredictorSubType.OSPC_2YR]?.score, Offset.offset(BigDecimal.valueOf(offset))
            )
          }
          if (output.scores[PredictorSubType.OSPC_1YR]?.score != null) {
            assertThat(result.scores[PredictorSubType.OSPC_1YR]?.score).isCloseTo(
              output.scores[PredictorSubType.OSPC_1YR]?.score, Offset.offset(BigDecimal.valueOf(offset))
            )
          }
        }
      }
  }
}
