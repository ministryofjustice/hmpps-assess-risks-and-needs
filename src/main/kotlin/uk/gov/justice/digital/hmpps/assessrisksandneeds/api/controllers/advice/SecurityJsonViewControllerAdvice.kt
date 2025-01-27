package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.controllers.advice

import org.springframework.core.MethodParameter
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJacksonValue
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.AbstractMappingJacksonResponseBodyAdvice
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.View
import java.util.stream.Collectors

@RestControllerAdvice
internal class SecurityJsonViewControllerAdvice : AbstractMappingJacksonResponseBodyAdvice() {
  override fun beforeBodyWriteInternal(
    bodyContainer: MappingJacksonValue,
    contentType: MediaType?,
    returnType: MethodParameter?,
    request: ServerHttpRequest?,
    response: ServerHttpResponse?,
  ) {
    if (SecurityContextHolder.getContext().authentication != null &&
      SecurityContextHolder.getContext().authentication.authorities != null
    ) {
      val authorities = SecurityContextHolder.getContext().authentication.authorities
      val jsonViews = authorities.stream()
        .map { obj: GrantedAuthority -> obj.authority }
        .filter { it.equals(View.Role.ROLE_CRS_PROVIDER.name) || it.equals(View.Role.ROLE_PROBATION.name) }
        .map<Any>(View.Role::valueOf)
        .map<Any>(View.roleMap::get)
        .collect(Collectors.toList())
      if (jsonViews.size == 1) {
        bodyContainer.serializationView = jsonViews[0] as Class<*>?
        return
      }
    }
  }
}
