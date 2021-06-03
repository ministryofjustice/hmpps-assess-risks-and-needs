package uk.gov.justice.digital.hmpps.assessrisksandneeds.config

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.web.servlet.HandlerInterceptor
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class RequestData(excludeUris: String?) : HandlerInterceptor {

  private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS")
  private val excludeUriRegex: Pattern = Pattern.compile(excludeUris)

  override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
    request.setAttribute("startTime", LocalDateTime.now().toString())
    MDC.clear()
    MDC.put(USER_ID_HEADER, initialiseUserId(request))

    if (excludeUriRegex.matcher(request.requestURI).matches()) {
      MDC.put(SKIP_LOGGING, "true")
    }
    if (log.isTraceEnabled && isLoggingAllowed) {
      log.trace("Request: ${request.method} ${request.requestURI}")
    }
    return true
  }

  override fun afterCompletion(request: HttpServletRequest, response: HttpServletResponse, handler: Any, ex: Exception?) {

    val status = response.status
    val start = LocalDateTime.parse(
      request.getAttribute("startTime").toString()
    )
    val duration = Duration.between(start, LocalDateTime.now()).toMillis()

    if (log.isTraceEnabled && isLoggingAllowed) {
      log.trace("Response: ${request.method} ${request.requestURI} - Status $status - Start ${start.format(formatter)}, Duration $duration ms")
    }

    MDC.put(REQUEST_DURATION, duration.toString())
    MDC.put(RESPONSE_STATUS, status.toString())
    MDC.clear()
  }

  private fun initialiseUserId(request: HttpServletRequest): String? {
    val userId: String? = if (request.userPrincipal != null) request.userPrincipal.name else null
    return if (userId.isNullOrEmpty()) null else userId
  }

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
    private const val ANONYMOUS = "anonymous"
    const val SKIP_LOGGING = "skipLogging"
    const val REQUEST_DURATION = "duration"
    const val RESPONSE_STATUS = "status"
    const val USER_ID_HEADER = "userId"
    val isLoggingAllowed: Boolean = "true" != MDC.get(SKIP_LOGGING)
  }
}
