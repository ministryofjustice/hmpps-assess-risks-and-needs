package uk.gov.justice.digital.hmpps.assessrisksandneeds.services

import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.MDC
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AssessmentStatus
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.CaseAccess
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.PredictorSource
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.PredictorSubType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.PredictorType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RsrPredictorDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RsrScoreSource
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ScoreLevel
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ScoreType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.config.RequestData
import uk.gov.justice.digital.hmpps.assessrisksandneeds.jpa.entities.OffenderPredictorsHistoryEntity
import uk.gov.justice.digital.hmpps.assessrisksandneeds.jpa.entities.PredictorEntity
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
@DisplayName("Risk Predictors Service RSR Tests")
class RiskPredictorServiceRsrTest {

  private val assessmentApiClient: OffenderAssessmentApiRestClient = mockk()
  private val communityApiRestClient: CommunityApiRestClient = mockk()
  private val oasysApiRestClient: OasysApiRestClient = mockk()
  private val offenderPredictorsHistoryRepository: OffenderPredictorsHistoryRepository = mockk()
  private val auditService: AuditService = mockk()
  private val riskPredictorsService = RiskPredictorService(
    assessmentApiClient,
    oasysApiRestClient,
    communityApiRestClient,
    offenderPredictorsHistoryRepository,
    auditService,
  )

  val crn = "TEST_CRN"

  @BeforeEach
  fun setup() {
    MDC.put(RequestData.USER_NAME_HEADER, "User name")

    every { auditService.sendEvent(any(), any()) } returns Unit
    every { communityApiRestClient.verifyUserAccess(any(), any()) } answers {
      CaseAccess(
        it.invocation.args[0] as String,
        userExcluded = false,
        userRestricted = false,
        null,
        null,
      )
    }
  }

  @Test
  fun `get all RSR scores history from OASys and ARN`() {
    every { oasysApiRestClient.getRiskPredictorsForCompletedAssessments(crn) } returns
      getOasysPredictor(LocalDateTime.of(2020, 1, 1, 1, 1, 1))
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
    every { oasysApiRestClient.getRiskPredictorsForCompletedAssessments(crn) } returns
      getOasysPredictor(LocalDateTime.of(2020, 1, 1, 1, 1, 1), LocalDateTime.of(2021, 4, 1, 1, 1, 1))
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
    every { oasysApiRestClient.getRiskPredictorsForCompletedAssessments(crn) } returns getOasysPredictorNoRsr()
    every { offenderPredictorsHistoryRepository.findAllByCrn(crn) } returns emptyList()

    val rsrHistory: List<RsrPredictorDto> = riskPredictorsService.getAllRsrHistory(crn)

    assertThat(rsrHistory).isEmpty()
  }

  private fun getOasysPredictorNoRsr(): OasysRiskPredictorsDto {
    return OasysRiskPredictorsDto(
      listOf(
        RiskPredictorAssessmentDto(
          dateCompleted = LocalDateTime.now(),
          assessmentStatus = AssessmentStatus.COMPLETE,
          ovpScoreDto = OasysOvpDto(),
          ospScoreDto = OasysOspDto(),
          ogpScoreDto = OasysOgpDto(),
          ogrScoreDto = OasysOgrDto(),
          rsrScoreDto = OasysRsrDto(),
        ),
      ),
    )
  }

  private fun getOasysPredictor(vararg completedDate: LocalDateTime): OasysRiskPredictorsDto {
    return OasysRiskPredictorsDto(
      completedDate.map {
        RiskPredictorAssessmentDto(
          dateCompleted = it,
          assessmentStatus = AssessmentStatus.COMPLETE,
          rsrScoreDto = OasysRsrDto(
            rsrPercentageScore = BigDecimal(10),
            rsrStaticOrDynamic = ScoreType.DYNAMIC,
            rsrAlgorithmVersion = "10",
            scoreLevel = ScoreLevel.LOW.type,
          ),
          ospScoreDto = OasysOspDto(
            ospImagePercentageScore = BigDecimal(10),
            ospImageScoreLevel = ScoreLevel.LOW.type,
            ospContactPercentageScore = BigDecimal(10),
            ospContactScoreLevel = ScoreLevel.LOW.type,
          ),
          ogpScoreDto = OasysOgpDto(),
          ovpScoreDto = OasysOvpDto(),
          ogrScoreDto = OasysOgrDto(),
        )
      },
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
