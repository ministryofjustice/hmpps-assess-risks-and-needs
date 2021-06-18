package uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient

import org.springframework.http.MediaType
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId
import org.springframework.web.reactive.function.client.WebClient

class AuthenticatingRestClient(
  private val webClient: WebClient,
  private val oauthClient: String,
  private val disableAuth: Boolean
) {
  fun get(path: String): WebClient.RequestHeadersSpec<*> {
    val request = webClient
      .get()
      .uri(path)
      .accept(MediaType.APPLICATION_JSON)
    return if (disableAuth)
      request
    else
      request.attributes(clientRegistrationId(oauthClient))
  }

  fun post(path: String, body: Any): WebClient.RequestHeadersSpec<*> {
    val request = webClient
      .post()
      .uri(path)
      .accept(MediaType.APPLICATION_JSON)
    val authed = if (disableAuth)
      request
    else
      request.attributes(clientRegistrationId(oauthClient))
    return authed.bodyValue(body)
  }

  fun put(path: String, body: Any): WebClient.RequestHeadersSpec<*> {
    val request = webClient
      .put()
      .uri(path)
      .accept(MediaType.APPLICATION_JSON)
    val authed = if (disableAuth)
      request
    else
      request.attributes(clientRegistrationId(oauthClient))
    return authed.bodyValue(body)
  }
}
