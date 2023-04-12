package uk.gov.justice.digital.hmpps.assessrisksandneeds.services

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AssessmentStatus
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.PredictorSource
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.PredictorSubType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.PredictorType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RsrPredictorDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RsrScoreSource
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ScoreLevel
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ScoreType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.jpa.entities.OffenderPredictorsHistoryEntity
import uk.gov.justice.digital.hmpps.assessrisksandneeds.jpa.entities.PredictorEntity
import uk.gov.justice.digital.hmpps.assessrisksandneeds.jpa.respositories.OffenderPredictorsHistoryRepository
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.CommunityApiRestClient
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.OffenderAssessmentApiRestClient
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.OasysPredictorsDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.OspDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.RefElementDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.RsrDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.riskCalculations.OASysCalculatorServiceImpl
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.riskCalculations.RiskPredictorService
import java.math.BigDecimal
import java.time.LocalDateTime

@ExtendWith(MockKExtension::class)
@DisplayName("Risk Predictors Service RSR Tests")
class RiskPredictorServiceRsrTest {

  private val assessmentApiClient: OffenderAssessmentApiRestClient = mockk()
  private val communityApiRestClient: CommunityApiRestClient = mockk()
  private val offenderPredictorsHistoryRepository: OffenderPredictorsHistoryRepository = mockk()
  private val objectMapper: ObjectMapper = mockk()
  private val riskCalculatorService = OASysCalculatorServiceImpl(assessmentApiClient)
  private val riskPredictorsService = RiskPredictorService(assessmentApiClient, communityApiRestClient, offenderPredictorsHistoryRepository, riskCalculatorService, objectMapper)

  val crn = "TEST_CRN"

  @Test
  fun `get all RSR scores history from OASys and ARN`() {
    every { assessmentApiClient.getPredictorScoresForOffender(crn) } returns listOf(
      getOasysPredictor(LocalDateTime.of(2020, 1, 1, 1, 1, 1)),
    )
    every { offenderPredictorsHistoryRepository.findAllByCrn(crn) } returns listOf(
      getOffenderPredictorsEntity(LocalDateTime.of(2021, 1, 1, 1, 1, 1)),
    )

    val rsrHistory: List<RsrPredictorDto> = riskPredictorsService.getAllRsrHistory(crn)

    assertThat(rsrHistory).hasSize(2)
    with(rsrHistory[0]) {
      assertThat(rsrPercentageScore).isEqualTo(BigDecimal(20.6))
      assertThat(rsrScoreLevel).isEqualTo(ScoreLevel.MEDIUM)
      assertThat(calculatedDate).isEqualTo(LocalDateTime.of(2021, 1, 1, 1, 1, 1))
      assertThat(completedDate).isEqualTo(LocalDateTime.of(2021, 1, 1, 1, 1, 1))
      assertThat(staticOrDynamic).isEqualTo(ScoreType.DYNAMIC)
      assertThat(source).isEqualTo(RsrScoreSource.ASSESSMENTS_API)
      assertThat(status).isEqualTo(AssessmentStatus.COMPLETE)
      assertThat(algorithmVersion).isEqualTo("12")
    }
    with(rsrHistory[1]) {
      assertThat(rsrPercentageScore).isEqualTo(BigDecimal(10))
      assertThat(rsrScoreLevel).isEqualTo(ScoreLevel.LOW)
      assertThat(calculatedDate).isNull()
      assertThat(completedDate).isEqualTo(LocalDateTime.of(2020, 1, 1, 1, 1, 1))
      assertThat(staticOrDynamic).isEqualTo(ScoreType.DYNAMIC)
      assertThat(source).isEqualTo(RsrScoreSource.OASYS)
      assertThat(status).isEqualTo(AssessmentStatus.COMPLETE)
      assertThat(algorithmVersion).isEqualTo("10")
    }
  }

  @Test
  fun `get all RSR scores are sorted by completed date`() {
    every { assessmentApiClient.getPredictorScoresForOffender(crn) } returns listOf(
      getOasysPredictor(LocalDateTime.of(2020, 1, 1, 1, 1, 1)),
      getOasysPredictor(LocalDateTime.of(2021, 4, 1, 1, 1, 1)),
    )
    every { offenderPredictorsHistoryRepository.findAllByCrn(crn) } returns listOf(
      getOffenderPredictorsEntity(LocalDateTime.of(2021, 1, 1, 1, 1, 1)),
    )

    val rsrHistory: List<RsrPredictorDto> = riskPredictorsService.getAllRsrHistory(crn)

    assertThat(rsrHistory).hasSize(3)
    assertThat(rsrHistory[0].completedDate).isEqualTo(LocalDateTime.of(2021, 4, 1, 1, 1, 1))
    assertThat(rsrHistory[1].completedDate).isEqualTo(LocalDateTime.of(2021, 1, 1, 1, 1, 1))
    assertThat(rsrHistory[2].completedDate).isEqualTo(LocalDateTime.of(2020, 1, 1, 1, 1, 1))
  }

  @Test
  fun `get all RSR scores does not include non-rsr predictor scores`() {
    every { assessmentApiClient.getPredictorScoresForOffender(crn) } returns listOf(
      getOasysPredictorNoRsr(),
    )
    every { offenderPredictorsHistoryRepository.findAllByCrn(crn) } returns emptyList()

    val rsrHistory: List<RsrPredictorDto> = riskPredictorsService.getAllRsrHistory(crn)

    assertThat(rsrHistory).isEmpty()
  }

  private fun getOasysPredictorNoRsr(): OasysPredictorsDto {
    return OasysPredictorsDto(
      completedDate = LocalDateTime.now(),
      assessmentStatus = "Complete",
    )
  }

  private fun getOasysPredictor(completedDate: LocalDateTime): OasysPredictorsDto {
    return OasysPredictorsDto(
      completedDate = completedDate,
      assessmentCompleted = true,
      assessmentStatus = "COMPLETE",
      rsr = RsrDto(
        rsrPercentageScore = BigDecimal(10),
        rsrStaticOrDynamic = "Dynamic",
        rsrAlgorithmVersion = 10L,
        rsrRiskRecon = RefElementDto(
          code = "LOW",
          description = "Low",
        ),
      ),
      osp = OspDto(
        ospIndecentPercentageScore = BigDecimal(10),
        ospIndecentRiskRecon = RefElementDto(
          code = "LOW",
          description = "Low",
        ),
        ospContactPercentageScore = BigDecimal(10),
        ospContactRiskRecon = RefElementDto(
          code = "LOW",
          description = "Low",
        ),
      ),
    )
  }

  private fun getOffenderPredictorsEntity(completedDate: LocalDateTime): OffenderPredictorsHistoryEntity {
    return OffenderPredictorsHistoryEntity(
      predictorType = PredictorType.RSR,
      algorithmVersion = "12",
      calculatedAt = LocalDateTime.of(2021, 1, 1, 1, 1, 1),
      crn = crn,
      predictorTriggerSource = PredictorSource.OASYS,
      predictorTriggerSourceId = "Source ID",
      createdBy = "Created By",
      assessmentCompletedDate = completedDate,
      scoreType = ScoreType.DYNAMIC,
      predictors = mutableListOf(
        PredictorEntity(
          predictorSubType = PredictorSubType.RSR,
          predictorScore = BigDecimal(20.6),
          predictorLevel = ScoreLevel.MEDIUM,
        ),
      ),
    )
  }
}
