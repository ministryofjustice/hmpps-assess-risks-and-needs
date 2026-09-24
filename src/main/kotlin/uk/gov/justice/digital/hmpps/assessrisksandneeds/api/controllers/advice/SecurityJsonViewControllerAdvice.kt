package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.controllers.advice

import com.fasterxml.jackson.annotation.JsonView
import org.springframework.core.MethodParameter
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.View

@RestControllerAdvice
internal class SecurityJsonViewControllerAdvice : ResponseBodyAdvice<Any> {
  // Applying the active view to endpoints without any @JsonView usage is harmless: with
  // spring.jackson.mapper.default-view-inclusion=true (see application.yml), fields without a @JsonView
  // annotation are always serialised regardless of the active view.
  override fun supports(returnType: MethodParameter, converterType: Class<out HttpMessageConverter<*>>): Boolean = returnType.declaringClass.packageName.startsWith("uk.gov.justice.digital.hmpps.assessrisksandneeds")

  // Not used: the active view is applied via determineWriteHints below, since that's the mechanism honoured by
  // the current Jackson message converter. This method is only required to satisfy the ResponseBodyAdvice contract.
  override fun beforeBodyWrite(
    body: Any?,
    returnType: MethodParameter,
    selectedContentType: MediaType,
    selectedConverterType: Class<out HttpMessageConverter<*>>,
    request: ServerHttpRequest,
    response: ServerHttpResponse,
  ): Any? = body

  // Resolves the active JsonView from the caller's role and surfaces it as a write hint, which the Jackson
  // message converter reads to determine which @JsonView-annotated fields to include in the response.
  override fun determineWriteHints(
    body: Any?,
    returnType: MethodParameter,
    contentType: MediaType,
    converterType: Class<out HttpMessageConverter<*>>,
  ): Map<String, Any> {
    val authentication = SecurityContextHolder.getContext().authentication ?: return emptyMap()
    val jsonViews = authentication.authorities
      .mapNotNull(GrantedAuthority::getAuthority)
      .filter { it == View.Role.ROLE_CRS_PROVIDER.name || it == View.Role.ROLE_PROBATION.name }
      .map { View.Role.valueOf(it) }
      .mapNotNull { View.roleMap[it] }
    return if (jsonViews.size == 1) mapOf(JsonView::class.java.name to jsonViews[0]) else emptyMap()
  }
}
