package uk.gov.justice.digital.hmpps.assessrisksandneeds.services

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.slot
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.MDC
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AssessmentStatus
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.CurrentOffence
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.CurrentOffences
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.DynamicScoringOffences
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.EmploymentType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.Gender
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.OffenderAndOffencesDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.PredictorSource
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.PredictorSubType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.PredictorType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.PreviousOffences
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ProblemsLevel
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RsrPredictorDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.Score
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ScoreLevel
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ScoreType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.config.RequestData
import uk.gov.justice.digital.hmpps.assessrisksandneeds.jpa.entities.OffenderPredictorsHistoryEntity
import uk.gov.justice.digital.hmpps.assessrisksandneeds.jpa.entities.PredictorEntity
import uk.gov.justice.digital.hmpps.assessrisksandneeds.jpa.respositories.OffenderPredictorsHistoryRepository
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.AssessmentApiRestClient
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.OasysPredictorsDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.OasysRSRPredictorsDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.RefElementDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.RsrDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.exceptions.IncorrectInputParametersException
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.exceptions.PredictorCalculationError
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@ExtendWith(MockKExtension::class)
@DisplayName("Risk Predictors Service RSR Tests")
class RiskPredictorServiceRsrTest {

  private val assessmentApiClient: AssessmentApiRestClient = mockk()
  private val offenderPredictorsHistoryRepository: OffenderPredictorsHistoryRepository = mockk()
  private val objectMapper: ObjectMapper = mockk()

  private val riskPredictorsService =
    RiskPredictorService(assessmentApiClient, offenderPredictorsHistoryRepository, objectMapper)


  @Test
  fun `gets all RSR scores history from OASys and ARN`() {

    val crn = "TEST_CRN"

    every { assessmentApiClient.getPredictorScoresForOffender(crn) } returns listOf(
      OasysPredictorsDto(
        completedDate = LocalDateTime.of(2021, 1, 1, 1, 1, 1),
        assessmentStatus = "Complete",
        rsr = RsrDto(
          rsrPercentageScore = BigDecimal(10),
          rsrStaticOrDynamic = "Dynamic",
          rsrAlgorithmVersion = 10L,
          rsrRiskRecon = RefElementDto(
            code = "LOW"
          )
        )
      )
    )

    every { offenderPredictorsHistoryRepository.findAllByCrn(crn) } returns listOf(
      OffenderPredictorsHistoryEntity(
        predictorType = PredictorType.RSR,
        algorithmVersion = "12",
        calculatedAt = LocalDateTime.of(2020, 1, 1, 1, 1, 1),
        crn = crn,
        predictorTriggerSource = PredictorSource.OASYS,
        predictorTriggerSourceId = "",
        createdBy = "",
        predictors = mutableListOf(
          PredictorEntity(
            predictorSubType = PredictorSubType.RSR,
            predictorScore = BigDecimal(20.6),
            predictorLevel = ScoreLevel.MEDIUM
          )
        )
      )
    )

    val rsrHistory:List<RsrPredictorDto> = riskPredictorsService.getAllRsrHistory(crn)

    assertThat(rsrHistory).isEqualTo(
      listOf(
        RsrPredictorDto(
          source = PredictorSource.OASYS,
          status = AssessmentStatus.COMPLETED
        )
      )
    )


  }
}
