package uk.gov.justice.digital.hmpps.assessrisksandneeds.config

import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.AuthenticatingRestClient
import java.util.concurrent.TimeUnit

@Configuration
class WebClientConfig {

  @Value("\${community-api.base-url}")
  private lateinit var communityApiBaseUrl: String

  @Value("\${oasys-api.base-url}")
  private lateinit var oasysApiBaseUrl: String

  @Value("\${feature.flags.auth-enabled:true}")
  private val authenticationEnabled = true

  @Value("\${web.client.connect-timeout-ms}")
  private val connectTimeoutMs: Long = 0

  @Value("\${web.client.read-timeout-ms}")
  private val readTimeoutMs: Long = 0

  @Value("\${web.client.write-timeout-ms}")
  private val writeTimeoutMs: Long = 0

  @Value("\${web.client.byte-buffer-size}")
  private val bufferByteSize: Int = Int.MAX_VALUE

  @Bean
  fun oasysApiWebClient(
    authorizedClientManager: OAuth2AuthorizedClientManager,
    builder: WebClient.Builder,
  ): AuthenticatingRestClient = AuthenticatingRestClient(
    webClientFactory(oasysApiBaseUrl, authorizedClientManager, builder),
    "oasys-api-client",
    authenticationEnabled,
  )

  @Bean
  fun communityApiWebClient(
    authorizedClientManager: OAuth2AuthorizedClientManager,
    builder: WebClient.Builder,
  ): AuthenticatingRestClient = AuthenticatingRestClient(
    webClientFactory(communityApiBaseUrl, authorizedClientManager, builder),
    "community-api-client",
    authenticationEnabled,
  )

  private fun webClientFactory(
    baseUrl: String,
    authorizedClientManager: OAuth2AuthorizedClientManager,
    builder: WebClient.Builder,
  ): WebClient {
    val httpClient = HttpClient.create()
      .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMs.toInt())
      .doOnConnected {
        it.addHandlerLast(ReadTimeoutHandler(readTimeoutMs, TimeUnit.MILLISECONDS))
          .addHandlerLast(WriteTimeoutHandler(writeTimeoutMs, TimeUnit.MILLISECONDS))
      }

    val configuredBuilder = builder.clone()
      .codecs { it.defaultCodecs().maxInMemorySize(bufferByteSize) }
      .clientConnector(ReactorClientHttpConnector(httpClient))
      .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)

    return if (authenticationEnabled) {
      val oauth2Client = ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager)
      configuredBuilder
        .baseUrl(baseUrl)
        .apply(oauth2Client.oauth2Configuration())
        .build()
    } else {
      configuredBuilder.baseUrl(baseUrl).build()
    }
  }
}
