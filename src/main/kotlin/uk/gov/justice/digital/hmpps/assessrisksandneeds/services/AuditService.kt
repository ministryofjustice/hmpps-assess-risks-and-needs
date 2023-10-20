package uk.gov.justice.digital.hmpps.assessrisksandneeds.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.assessrisksandneeds.config.RequestData
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import java.time.Instant

@Service
class AuditService(
  private val hmppsQueueService: HmppsQueueService,
  private val objectMapper: ObjectMapper,
  @Value("\${spring.application.name}")
  private val serviceName: String,
) {
  private val auditQueue by lazy {
    hmppsQueueService.findByQueueId("audit") ?: throw RuntimeException("Queue with ID 'audit' does not exist'")
  }
  private val sqsClient by lazy { auditQueue.sqsClient }
  private val queueUrl by lazy { auditQueue.queueUrl }

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  fun sendEvent(what: EventType, details: Any) {
    val event = AuditableEvent(
      who = RequestData.getUserName(),
      what = what,
      service = serviceName,
      details = details,
    )

    log.info("Sending audit event ${event.what} for ${event.who}")

    sqsClient.sendMessage {
      it.queueUrl(queueUrl)
        .messageBody(event.toJson())
        .build()
    }
      .get()
      .also { log.info("Audit event ${event.what} for ${event.who} sent") }
  }

  private fun Any.toJson() = objectMapper.writeValueAsString(this)
}

data class AuditableEvent(
  val who: String,
  val what: EventType,
  val `when`: Instant = Instant.now(),
  val service: String,
  val details: Any,
)

enum class EventType
