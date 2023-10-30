package uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient

import org.springframework.http.MediaType
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriBuilder

class AuthenticatingRestClient(
  private val webClient: WebClient,
  private val oauthClient: String,
  private val authEnabled: Boolean,
) {
  fun get(path: String): WebClient.RequestHeadersSpec<*> {
    val request = webClient
      .get()
      .uri(path)
      .accept(MediaType.APPLICATION_JSON)
    return if (authEnabled) {
      request.attributes(clientRegistrationId(oauthClient))
    } else {
      request
    }
  }

  fun get(uri: (uriBuilder: UriBuilder) -> UriBuilder): WebClient.RequestHeadersSpec<*> {
    val request = webClient
      .get()
      .uri { uri(it).build() }
      .accept(MediaType.APPLICATION_JSON)
    return if (authEnabled) {
      request.attributes(clientRegistrationId(oauthClient))
    } else {
      request
    }
  }

  fun post(path: String, body: Any): WebClient.RequestHeadersSpec<*> {
    val request = webClient
      .post()
      .uri(path)
      .accept(MediaType.APPLICATION_JSON)
    val authed = if (authEnabled) {
      request.attributes(clientRegistrationId(oauthClient))
    } else {
      request
    }
    return authed.bodyValue(body)
  }

  fun post(body: Any, uri: (uriBuilder: UriBuilder) -> UriBuilder): WebClient.RequestHeadersSpec<*> {
    val request = webClient
      .post()
      .uri { uri(it).build() }
      .accept(MediaType.APPLICATION_JSON)
    val authed = if (authEnabled) {
      request.attributes(clientRegistrationId(oauthClient))
    } else {
      request
    }
    return authed.bodyValue(body)
  }
}
