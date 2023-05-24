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
  fun filterChain(http: HttpSecurity): SecurityFilterChain {
    http.headers().frameOptions().sameOrigin().and()
      .sessionManagement()
      .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
      // Can't have CSRF protection as requires session
      .and().csrf().disable()
      .authorizeHttpRequests { auth ->
        auth.requestMatchers(
          "/webjars/**", "/favicon.ico", "/csrf",
          "/health/**", "/info", "/ping",
          "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html",
        ).permitAll()
          .anyRequest()
          .authenticated()
      }.oauth2ResourceServer().jwt().jwtAuthenticationConverter(AuthAwareTokenConverter())

    return http.build()
  }
}
