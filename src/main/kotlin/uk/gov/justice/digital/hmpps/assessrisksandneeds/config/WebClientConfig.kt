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
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.AuthenticatingRestClient
import java.util.concurrent.TimeUnit

@Configuration
class WebClientConfig {

  @Value("\${assessment-api.base-url}")
  private lateinit var assessmentApiBaseUrl: String

  @Value("\${community-api.base-url}")
  private lateinit var communityApiBaseUrl: String

  @Value("\${feature.flags.auth-enabled:true}")
  private val authenticationEnabled = true

  @Value("\${web.client.connect-timeout-ms}")
  private val connectTimeoutMs: Int? = null

  @Value("\${web.client.read-timeout-ms}")
  private val readTimeoutMs: Long = 0

  @Value("\${web.client.write-timeout-ms}")
  private val writeTimeoutMs: Long = 0

  @Value("\${web.client.byte-buffer-size}")
  val bufferByteSize: Int = Int.MAX_VALUE

  @Bean
  fun authorizedClientManager(
    clientRegistrationRepository: ClientRegistrationRepository?,
    authorizedClientRepository: OAuth2AuthorizedClientRepository?,
  ): OAuth2AuthorizedClientManager {
    val authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder()
      .clientCredentials()
      .build()

    val authorizedClientManager = DefaultOAuth2AuthorizedClientManager(
      clientRegistrationRepository,
      authorizedClientRepository,
    )

    authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider)

    return authorizedClientManager
  }

  @Bean
  fun offenderAssessmentApiWebClient(authorizedClientManager: OAuth2AuthorizedClientManager): AuthenticatingRestClient {
    return AuthenticatingRestClient(
      webClientFactory(assessmentApiBaseUrl, authorizedClientManager, bufferByteSize),
      "assessment-api-client",
      authenticationEnabled,
    )
  }

  @Bean
  fun communityApiWebClient(authorizedClientManager: OAuth2AuthorizedClientManager): AuthenticatingRestClient {
    return AuthenticatingRestClient(
      webClientFactory(communityApiBaseUrl, authorizedClientManager, bufferByteSize),
      "community-api-client",
      authenticationEnabled,
    )
  }

  private fun webClientFactory(
    baseUrl: String,
    authorizedClientManager: OAuth2AuthorizedClientManager,
    bufferByteCount: Int,
  ): WebClient {
    val oauth2Client = ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager)

    val httpClient = HttpClient.create()
      .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMs)
      .doOnConnected {
        it.addHandlerLast(ReadTimeoutHandler(readTimeoutMs, TimeUnit.MILLISECONDS))
          .addHandlerLast(WriteTimeoutHandler(writeTimeoutMs, TimeUnit.MILLISECONDS))
      }

    return WebClient
      .builder()
      .clientConnector(ReactorClientHttpConnector(httpClient))
      .codecs { it.defaultCodecs().maxInMemorySize(bufferByteCount) }
      .baseUrl(baseUrl)
      .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
      .apply(oauth2Client.oauth2Configuration())
      .build()
  }
}
