package com.nimbleways.springboilerplate.common.api.security;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.savedrequest.NullRequestCache;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@EnableWebSecurity
// Necessary to use @PreAuthorize
// prePostEnabled by default is true.
@EnableMethodSecurity
@Configuration
public class WebSecurityConfiguration {

    @Bean
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    public SecurityFilterChain filterChain(
        final HttpSecurity http,
        final StandaloneJwtAuthentication standaloneJwtAuthentication
    ) throws Exception {
        // Enable CORS and disables CSRF
        http.cors(allowAllOrigins()).csrf(AbstractHttpConfigurer::disable);

        // Disable session creation
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // disable request saving on authentication error
        // https://docs.spring.io/spring-security/reference/servlet/architecture.html#requestcache-prevent-saved-request
        http.requestCache(cache -> cache.requestCache(new NullRequestCache()));

        standaloneJwtAuthentication.configure(http);

        return http.build();
    }

    @Bean
    public FilterRegistrationBean<SimpleBasicAuthenticationFilter> managementFilter(
        @Value("${management.endpoints.web.base-path:/actuator}") String actuatorBasePath,
        @Value("${management.server.user}") String username,
        @Value("${management.server.password}") String password
    ) {
        FilterRegistrationBean<SimpleBasicAuthenticationFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new SimpleBasicAuthenticationFilter("Actuator", username, password));
        registrationBean.setEnabled(true);
        registrationBean.addUrlPatterns(actuatorBasePath.replaceAll("/+$", "") + "/*");
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE); // needed so that errors are returned as ProblemDetails JSON
        return registrationBean;
    }

    @NotNull private static Customizer<CorsConfigurer<HttpSecurity>> allowAllOrigins() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        final CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOriginPattern("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        source.registerCorsConfiguration("/**", config);
        return corsConfigurer -> corsConfigurer.configurationSource(source);
    }
}
