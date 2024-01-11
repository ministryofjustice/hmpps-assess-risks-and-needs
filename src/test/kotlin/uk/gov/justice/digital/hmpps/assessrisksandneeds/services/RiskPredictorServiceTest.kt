package uk.gov.justice.digital.hmpps.assessrisksandneeds.services

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.MDC
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AssessmentStatus
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.CaseAccess
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RsrScoreSource.OASYS
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ScoreLevel.LOW
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ScoreLevel.MEDIUM
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ScoreType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.config.RequestData
import uk.gov.justice.digital.hmpps.assessrisksandneeds.jpa.respositories.OffenderPredictorsHistoryRepository
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.CommunityApiRestClient
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.OasysApiRestClient
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.OffenderAssessmentApiRestClient
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.OasysOgpDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.OasysOgrDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.OasysOspDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.OasysOvpDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.OasysRiskPredictorsDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.OasysRsrDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.RiskPredictorAssessmentDto
import java.math.BigDecimal
import java.time.LocalDateTime

@ExtendWith(MockKExtension::class)
@DisplayName("Risk Predictors Service Tests")
class RiskPredictorServiceTest {

  private val assessmentApiClient: OffenderAssessmentApiRestClient = mockk()
  private val oasysApiClient: OasysApiRestClient = mockk()
  private val communityApiRestClient: CommunityApiRestClient = mockk()
  private val offenderPredictorsHistoryRepository: OffenderPredictorsHistoryRepository = mockk()
  private val auditService: AuditService = mockk()
  private val objectMapper: ObjectMapper = mockk()

  @BeforeEach
  fun setup() {
    MDC.put(RequestData.USER_NAME_HEADER, "User name")
    every { objectMapper.writeValueAsString(any()) } returns sourceAnswersJson
    every { communityApiRestClient.verifyUserAccess(any(), any()) } answers {
      CaseAccess(
        it.invocation.args[0] as String,
        userExcluded = false,
        userRestricted = false,
        null,
        null,
      )
    }
    every { auditService.sendEvent(any(), any()) } returns Unit
  }

  @Nested
  @DisplayName("OASys Scores")
  inner class ValidateOASysScores {

    private val riskPredictorsService =
      RiskPredictorService(
        assessmentApiClient,
        oasysApiClient,
        communityApiRestClient,
        offenderPredictorsHistoryRepository,
        auditService,
      )

    @Test
    fun `should return a list of all risk predictors for valid crn`() {
      // Given
      val crn = "X12345"
      val now = LocalDateTime.now()

      val oasysRiskPredictorsDto = OasysRiskPredictorsDto(
        listOf(
          RiskPredictorAssessmentDto(
            dateCompleted = now,
            assessmentStatus = AssessmentStatus.COMPLETE,
            ogpScoreDto = OasysOgpDto(
              ogpStWesc = BigDecimal.valueOf(3),
              ogpDyWesc = BigDecimal.valueOf(7),
              ogpTotWesc = BigDecimal.valueOf(10),
              ogp1Year = BigDecimal.valueOf(4),
              ogp2Year = BigDecimal.valueOf(8),
              ogpRisk = LOW.type,
            ),
            ovpScoreDto = OasysOvpDto(
              ovpStWesc = BigDecimal.valueOf(14),
              ovpDyWesc = BigDecimal.valueOf(3),
              ovpTotWesc = BigDecimal.valueOf(17),
              ovp1Year = BigDecimal.valueOf(4),
              ovp2Year = BigDecimal.valueOf(7),
              ovpRisk = LOW.type,
            ),
            ogrScoreDto = OasysOgrDto(
              ogrs31Year = BigDecimal.valueOf(3),
              ogrs32Year = BigDecimal.valueOf(5),
              ogrs3RiskRecon = LOW.type,
            ),
            rsrScoreDto = OasysRsrDto(
              rsrPercentageScore = BigDecimal.valueOf(50.1234),
              rsrStaticOrDynamic = ScoreType.DYNAMIC,
              rsrAlgorithmVersion = "11",
              scoreLevel = MEDIUM.type,
            ),
            ospScoreDto = OasysOspDto(
              ospImagePercentageScore = BigDecimal.valueOf(2.81),
              ospContactPercentageScore = BigDecimal.valueOf(1.07),
              ospImageScoreLevel = MEDIUM.type,
              ospContactScoreLevel = MEDIUM.type,
            ),
          ),
        ),
      )

      every {
        oasysApiClient.getRiskPredictorsForCompletedAssessments(crn)
      }.returns(oasysRiskPredictorsDto)

      // When
      val allRiskScores = riskPredictorsService.getAllRiskScores(crn)

      // Should
      with(allRiskScores[0]) {
        assertThat(completedDate).isEqualTo(now)

        assertThat(violencePredictorScore?.ovpStaticWeightedScore).isEqualTo(BigDecimal(14))
        assertThat(violencePredictorScore?.ovpDynamicWeightedScore).isEqualTo(BigDecimal(3))
        assertThat(violencePredictorScore?.ovpTotalWeightedScore).isEqualTo(BigDecimal(17))
        assertThat(violencePredictorScore?.oneYear).isEqualTo(BigDecimal(4))
        assertThat(violencePredictorScore?.twoYears).isEqualTo(BigDecimal(7))
        assertThat(violencePredictorScore?.ovpRisk).isEqualTo(LOW)

        assertThat(groupReconvictionScore?.oneYear).isEqualTo(BigDecimal(3))
        assertThat(groupReconvictionScore?.twoYears).isEqualTo(BigDecimal(5))
        assertThat(groupReconvictionScore?.scoreLevel).isEqualTo(LOW)

        assertThat(riskOfSeriousRecidivismScore?.percentageScore).isEqualTo(BigDecimal.valueOf(50.1234))
        assertThat(riskOfSeriousRecidivismScore?.staticOrDynamic).isEqualTo(ScoreType.DYNAMIC)
        assertThat(riskOfSeriousRecidivismScore?.source).isEqualTo(OASYS)
        assertThat(riskOfSeriousRecidivismScore?.algorithmVersion).isEqualTo("11")
        assertThat(riskOfSeriousRecidivismScore?.scoreLevel).isEqualTo(MEDIUM)

        assertThat(generalPredictorScore?.ogpStaticWeightedScore).isEqualTo(BigDecimal(3))
        assertThat(generalPredictorScore?.ogpDynamicWeightedScore).isEqualTo(BigDecimal(7))
        assertThat(generalPredictorScore?.ogpTotalWeightedScore).isEqualTo(BigDecimal(10))
        assertThat(generalPredictorScore?.ogp1Year).isEqualTo(BigDecimal(4))
        assertThat(generalPredictorScore?.ogp2Year).isEqualTo(BigDecimal(8))
        assertThat(generalPredictorScore?.ogpRisk).isEqualTo(LOW)

        assertThat(sexualPredictorScore?.ospIndecentPercentageScore).isEqualTo(BigDecimal.valueOf(2.81))
        assertThat(sexualPredictorScore?.ospContactPercentageScore).isEqualTo(BigDecimal.valueOf(1.07))
        assertThat(sexualPredictorScore?.ospIndecentScoreLevel).isEqualTo(MEDIUM)
        assertThat(sexualPredictorScore?.ospContactScoreLevel).isEqualTo(MEDIUM)
      }
    }

    @Test
    fun `should return an empty list of all risk predictors for invalid crn`() {
      // Given
      val crn = "X12345"
      every {
        oasysApiClient.getRiskPredictorsForCompletedAssessments(crn)
      }.returns(null)

      // When
      val allRiskScores = riskPredictorsService.getAllRiskScores(crn)

      // Should
      assertThat(allRiskScores.isEmpty())
    }
  }

  companion object {
    val sourceAnswersJson =
      """{"gender":"MALE","dob":"2001-01-01","assessment_date":"2021-01-01T00:00:00","offence_code":"138","offence_subcode":"00","date_first_sanction":"2020-01-01","total_sanctions":10,"total_violent_offences":8,"date_current_conviction":"2020-12-18","any_sexual_offences":true,"current_sexual_offence":true,"current_offence_victim_stranger":true,"most_recent_sexual_offence_date":"2020-12-11","total_sexual_offences_adult":5,"total_sexual_offences_child":5,"total_sexual_offences_child_image":2,"total_non_contact_sexual_offences":2,"earliest_release_date":"2021-11-01","completed_interview":true,"suitable_accommodation":"MISSING","unemployed_on_release":"NOT_AVAILABLE_FOR_WORK","current_relationship_with_partner":"SIGNIFICANT_PROBLEMS","evidence_domestic_violence":true,"perpetrator_domestic_violence":true,"use_of_alcohol":"SIGNIFICANT_PROBLEMS","binge_drinking":"SIGNIFICANT_PROBLEMS","impulsivity_issues":"SOME_PROBLEMS","temper_control_issues":"SIGNIFICANT_PROBLEMS","pro_criminal_attitudes":"SOME_PROBLEMS","previous_murder_attempt":true,"previous_wounding":true,"previous_aggravated_burglary":true,"previous_arson":true,"previous_criminal_damage":true,"previous_kidnapping":true,"previous_possession_firearm":true,"previous_robbery":true,"previous_offence_weapon":true,"current_possession_firearm":true,"current_offence_weapon":true}  """.trimIndent()
  }
}
