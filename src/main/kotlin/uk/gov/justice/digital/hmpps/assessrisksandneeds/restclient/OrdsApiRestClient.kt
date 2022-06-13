package uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component
class OrdsApiRestClient {
  @Autowired
  @Qualifier("ordsApiWebClient")
  internal lateinit var webClient: AuthenticatingRestClient

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  /*
    TODO: remove me - just for testing
   */
  fun getTestConfig(): String? {
    val path = "/authtest/config2"

    return webClient.get(path).retrieve().bodyToMono(String::class.java).block()
  }
}
