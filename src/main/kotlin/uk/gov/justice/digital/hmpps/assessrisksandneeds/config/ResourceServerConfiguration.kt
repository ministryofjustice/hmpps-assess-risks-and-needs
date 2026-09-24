package uk.gov.justice.digital.hmpps.assessrisksandneeds.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class ResourceServerConfiguration {
  @Bean
  fun filterChain(http: HttpSecurity): SecurityFilterChain = http.sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
    .csrf { it.disable() }
    .authorizeHttpRequests {
      it.requestMatchers(
        "/webjars/**",
        "/favicon.ico",
        "/csrf",
        "/health/**",
        "/info/**",
        "/v3/api-docs/**",
        "/swagger-ui/**",
        "/v3/api-docs.yaml",
        "/swagger-ui.html",
        "/actuator/**",
      ).permitAll().anyRequest().authenticated()
    }.oauth2ResourceServer {
      it.jwt { jwt -> jwt.jwtAuthenticationConverter(AuthAwareTokenConverter()) }
    }.build()
}
