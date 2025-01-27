package uk.gov.justice.digital.hmpps.assessrisksandneeds.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.util.matcher.AntPathRequestMatcher

@Configuration
@EnableWebSecurity
class ResourceServerConfiguration {
  @Bean
  fun filterChain(http: HttpSecurity): SecurityFilterChain = http.sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
    .csrf { it.disable() }
    .authorizeHttpRequests {
      it.requestMatchers(
        AntPathRequestMatcher("/webjars/**"),
        AntPathRequestMatcher("/favicon.ico"),
        AntPathRequestMatcher("/csrf"),
        AntPathRequestMatcher("/health/**"),
        AntPathRequestMatcher("/info/**"),
        AntPathRequestMatcher("/v3/api-docs/**"),
        AntPathRequestMatcher("/swagger-ui/**"),
        AntPathRequestMatcher("/v3/api-docs.yaml"),
        AntPathRequestMatcher("/swagger-ui.html"),
        AntPathRequestMatcher("/actuator/**"),
      ).permitAll().anyRequest().authenticated()
    }.oauth2ResourceServer {
      it.jwt { jwt -> jwt.jwtAuthenticationConverter(AuthAwareTokenConverter()) }
    }.build()
}
