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
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.IdentifierType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RsrPredictorDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RsrPredictorVersioned
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RsrPredictorVersionedDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RsrPredictorVersionedLegacyDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RsrScoreSource
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ScoreLevel
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ScoreType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.config.RequestData
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.CommunityApiRestClient
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.OasysApiRestClient
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.AllRisksOasysRiskPredictorsDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.AllRisksPredictorAssessmentDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.OasysNewAllPredictorDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.OasysOgpDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.OasysOgrDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.OasysOspDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.OasysOvpDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.OasysRsrDto
import java.math.BigDecimal
import java.time.LocalDateTime

@ExtendWith(MockKExtension::class)
@DisplayName("Risk Predictors Service RSR Tests")
class RiskPredictorServiceRsrTest {

  private val communityApiRestClient: CommunityApiRestClient = mockk()
  private val oasysApiRestClient: OasysApiRestClient = mockk()
  private val auditService: AuditService = mockk()
  private val riskPredictorsService = RiskPredictorService(
    oasysApiRestClient,
    communityApiRestClient,
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
  fun `get all RSR scores from OASys and ARN`() {
    val oasysRsrRiskPredictorsDto = AllRisksOasysRiskPredictorsDto(
      listOf(
        getOasysPredictor(LocalDateTime.of(2020, 1, 1, 1, 1, 1), "5", true),
        getOasysPredictor(LocalDateTime.of(2021, 2, 1, 1, 1, 1), "6", false),
        getOasysPredictor(LocalDateTime.of(2021, 4, 1, 1, 1, 1), "6", true),
      ),
    )

    every { oasysApiRestClient.getRiskPredictorsForCompletedAssessments(crn) } returns oasysRsrRiskPredictorsDto

    val rsrScores: List<RsrPredictorVersioned<Any>> = riskPredictorsService.getAllRsrScores(IdentifierType.CRN, crn)

    assertThat(rsrScores).hasSize(3)

    assertThat(rsrScores[0].outputVersion).isEqualTo("2")
    val versionOneRsrDynamicScore = rsrScores[0] as RsrPredictorVersionedDto
    with(versionOneRsrDynamicScore) {
      assertThat(completedDate).isEqualTo(LocalDateTime.of(2021, 4, 1, 1, 1, 1))
      assertThat(source).isEqualTo(RsrScoreSource.OASYS)
      assertThat(status).isEqualTo(AssessmentStatus.COMPLETE)

      assertThat(output?.seriousViolentReoffendingPredictor?.score).isEqualTo(BigDecimal(10))
      assertThat(output?.seriousViolentReoffendingPredictor?.band).isEqualTo(ScoreLevel.LOW)
      assertThat(output?.seriousViolentReoffendingPredictor?.staticOrDynamic).isEqualTo(ScoreType.DYNAMIC)

      assertThat(output?.directContactSexualReoffendingPredictor?.score).isEqualTo(BigDecimal(10))
      assertThat(output?.directContactSexualReoffendingPredictor?.band).isEqualTo(ScoreLevel.LOW)

      assertThat(output?.indirectImageContactSexualReoffendingPredictor?.score).isEqualTo(BigDecimal(10))
      assertThat(output?.indirectImageContactSexualReoffendingPredictor?.band).isEqualTo(ScoreLevel.LOW)

      assertThat(output?.combinedSeriousReoffendingPredictor?.score).isEqualTo(BigDecimal(10))
      assertThat(output?.combinedSeriousReoffendingPredictor?.band).isEqualTo(ScoreLevel.LOW)
      assertThat(output?.combinedSeriousReoffendingPredictor?.staticOrDynamic).isEqualTo(ScoreType.DYNAMIC)
      assertThat(output?.combinedSeriousReoffendingPredictor?.algorithmVersion).isEqualTo("6")
    }

    assertThat(rsrScores[1].outputVersion).isEqualTo("2")
    val versionOneRsrStaticScore = rsrScores[1] as RsrPredictorVersionedDto
    with(versionOneRsrStaticScore) {
      assertThat(completedDate).isEqualTo(LocalDateTime.of(2021, 2, 1, 1, 1, 1))
      assertThat(source).isEqualTo(RsrScoreSource.OASYS)
      assertThat(status).isEqualTo(AssessmentStatus.COMPLETE)

      assertThat(output?.seriousViolentReoffendingPredictor?.score).isEqualTo(BigDecimal(10))
      assertThat(output?.seriousViolentReoffendingPredictor?.band).isEqualTo(ScoreLevel.LOW)
      assertThat(output?.seriousViolentReoffendingPredictor?.staticOrDynamic).isEqualTo(ScoreType.STATIC)

      assertThat(output?.directContactSexualReoffendingPredictor?.score).isEqualTo(BigDecimal(10))
      assertThat(output?.directContactSexualReoffendingPredictor?.band).isEqualTo(ScoreLevel.LOW)

      assertThat(output?.indirectImageContactSexualReoffendingPredictor?.score).isEqualTo(BigDecimal(10))
      assertThat(output?.indirectImageContactSexualReoffendingPredictor?.band).isEqualTo(ScoreLevel.LOW)

      assertThat(output?.combinedSeriousReoffendingPredictor?.score).isEqualTo(BigDecimal(10))
      assertThat(output?.combinedSeriousReoffendingPredictor?.band).isEqualTo(ScoreLevel.LOW)
      assertThat(output?.combinedSeriousReoffendingPredictor?.staticOrDynamic).isEqualTo(ScoreType.STATIC)
      assertThat(output?.combinedSeriousReoffendingPredictor?.algorithmVersion).isEqualTo("6")
    }

    assertThat(rsrScores[2].outputVersion).isEqualTo("1")
    val versionOneRsrScore = rsrScores[2] as RsrPredictorVersionedLegacyDto
    with(versionOneRsrScore) {
      assertThat(completedDate).isEqualTo(LocalDateTime.of(2020, 1, 1, 1, 1, 1))
      assertThat(source).isEqualTo(RsrScoreSource.OASYS)
      assertThat(status).isEqualTo(AssessmentStatus.COMPLETE)
      assertThat(output?.rsrPercentageScore).isEqualTo(BigDecimal(10))
      assertThat(output?.rsrScoreLevel).isEqualTo(ScoreLevel.LOW)
      assertThat(output?.staticOrDynamic).isEqualTo(ScoreType.DYNAMIC)
      assertThat(output?.algorithmVersion).isEqualTo("5")
    }
  }

  @Test
  fun `get all RSR scores are sorted by completed date`() {
    val oasysRsrRiskPredictorsDto = AllRisksOasysRiskPredictorsDto(
      listOf(
        getOasysPredictor(LocalDateTime.of(2020, 1, 1, 1, 1, 1), "5", true),
        getOasysPredictor(LocalDateTime.of(2021, 4, 1, 1, 1, 1), "6", true),
      ),
    )

    every { oasysApiRestClient.getRiskPredictorsForCompletedAssessments(crn) } returns oasysRsrRiskPredictorsDto

    val rsrScores: List<RsrPredictorVersioned<Any>> = riskPredictorsService.getAllRsrScores(IdentifierType.CRN, crn)

    assertThat(rsrScores).hasSize(2)
    assertThat(rsrScores[0].completedDate).isEqualTo(LocalDateTime.of(2021, 4, 1, 1, 1, 1))
    assertThat(rsrScores[1].completedDate).isEqualTo(LocalDateTime.of(2020, 1, 1, 1, 1, 1))
  }

  @Test
  fun `get all RSR scores does not include non-rsr predictor scores`() {
    every { oasysApiRestClient.getRiskPredictorsForCompletedAssessments(crn) } returns getOasysPredictorNoRsr()

    val rsrScores: List<RsrPredictorVersioned<Any>> = riskPredictorsService.getAllRsrScores(IdentifierType.CRN, crn)

    assertThat(rsrScores).isEmpty()
  }

  @Test
  fun `get all RSR scores history from OASys and ARN`() {
    val oasysRsrRiskPredictorsDto = AllRisksOasysRiskPredictorsDto(
      listOf(
        getOasysPredictor(LocalDateTime.of(2020, 1, 1, 1, 1, 1), "5", true),
      ),
    )

    every { oasysApiRestClient.getRiskPredictorsForCompletedAssessments(crn) } returns oasysRsrRiskPredictorsDto

    val rsrHistory: List<RsrPredictorDto> = riskPredictorsService.getAllRsrHistory(crn)

    assertThat(rsrHistory).hasSize(1)
    with(rsrHistory[0]) {
      assertThat(rsrPercentageScore).isEqualTo(BigDecimal(10))
      assertThat(rsrScoreLevel).isEqualTo(ScoreLevel.LOW)
      assertThat(calculatedDate).isNull()
      assertThat(completedDate).isEqualTo(LocalDateTime.of(2020, 1, 1, 1, 1, 1))
      assertThat(staticOrDynamic).isEqualTo(ScoreType.DYNAMIC)
      assertThat(source).isEqualTo(RsrScoreSource.OASYS)
      assertThat(status).isEqualTo(AssessmentStatus.COMPLETE)
      assertThat(algorithmVersion).isEqualTo("5")
    }
  }

  @Test
  fun `get all RSR scores history are sorted by completed date`() {
    val oasysRsrRiskPredictorsDto = AllRisksOasysRiskPredictorsDto(
      listOf(
        getOasysPredictor(LocalDateTime.of(2020, 1, 1, 1, 1, 1), "5", true),
        getOasysPredictor(LocalDateTime.of(2021, 4, 1, 1, 1, 1), "6", true),
      ),
    )

    every { oasysApiRestClient.getRiskPredictorsForCompletedAssessments(crn) } returns oasysRsrRiskPredictorsDto

    val rsrHistory: List<RsrPredictorDto> = riskPredictorsService.getAllRsrHistory(crn)

    assertThat(rsrHistory).hasSize(2)
    assertThat(rsrHistory[0].completedDate).isEqualTo(LocalDateTime.of(2021, 4, 1, 1, 1, 1))
    assertThat(rsrHistory[1].completedDate).isEqualTo(LocalDateTime.of(2020, 1, 1, 1, 1, 1))
  }

  @Test
  fun `get all RSR scores history does not include non-rsr predictor scores`() {
    every { oasysApiRestClient.getRiskPredictorsForCompletedAssessments(crn) } returns getOasysPredictorNoRsr()

    val rsrHistory: List<RsrPredictorDto> = riskPredictorsService.getAllRsrHistory(crn)

    assertThat(rsrHistory).isEmpty()
  }

  private fun getOasysPredictorNoRsr(): AllRisksOasysRiskPredictorsDto = AllRisksOasysRiskPredictorsDto(
    listOf(
      AllRisksPredictorAssessmentDto(
        dateCompleted = LocalDateTime.parse("2024-12-25T12:00:00"),
        assessmentType = "LAYER3",
        assessmentStatus = AssessmentStatus.COMPLETE,
        ovpScoreDto = OasysOvpDto(),
        ospScoreDto = OasysOspDto(),
        ogpScoreDto = OasysOgpDto(),
        ogrScoreDto = OasysOgrDto(),
        rsrScoreDto = OasysRsrDto(),
        newAllPredictorScoresDto = OasysNewAllPredictorDto(),
      ),
    ),
  )

  private fun getOasysPredictor(
    completedDate: LocalDateTime,
    rsrAlgorithmVersion: String,
    isDynamic: Boolean,
  ): AllRisksPredictorAssessmentDto = AllRisksPredictorAssessmentDto(
    dateCompleted = completedDate,
    assessmentType = "LAYER3",
    assessmentStatus = AssessmentStatus.COMPLETE,
    rsrScoreDto = OasysRsrDto(
      rsrPercentageScore = BigDecimal(10),
      rsrStaticOrDynamic = if (isDynamic) ScoreType.DYNAMIC else ScoreType.STATIC,
      rsrAlgorithmVersion = rsrAlgorithmVersion,
      scoreLevel = ScoreLevel.LOW.type,
    ),
    ospScoreDto = OasysOspDto(
      ospImagePercentageScore = BigDecimal(10),
      ospImageScoreLevel = ScoreLevel.LOW.type,
      ospContactPercentageScore = BigDecimal(10),
      ospContactScoreLevel = ScoreLevel.LOW.type,
      ospIndirectImagesChildrenPercentageScore = BigDecimal(10),
      ospDirectContactPercentageScore = BigDecimal(10),
      ospIndirectImagesChildrenScoreLevel = ScoreLevel.LOW.type,
      ospDirectContactScoreLevel = ScoreLevel.LOW.type,
    ),
    ogpScoreDto = OasysOgpDto(),
    ovpScoreDto = OasysOvpDto(),
    ogrScoreDto = OasysOgrDto(),
    newAllPredictorScoresDto = OasysNewAllPredictorDto(
      snsvStaticYr2 = BigDecimal(10),
      snsvDynamicYr2 = BigDecimal(10),
      snsvStaticYr2Band = ScoreLevel.LOW.type,
      snsvDynamicYr2Band = ScoreLevel.LOW.type,
      snsvStaticCalculated = if (isDynamic) "N" else "Y",
      snsvDynamicCalculated = if (isDynamic) "Y" else "N",
    ),
  )
}
