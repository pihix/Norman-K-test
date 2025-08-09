package com.nimbleways.springboilerplate.common.api.security;

import com.nimbleways.springboilerplate.features.authentication.api.services.AuthenticationResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.SecurityConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.stereotype.Component;

@Component
public class StandaloneJwtAuthentication {

    private final JwtDecoder jwtDecoder;

    public StandaloneJwtAuthentication(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    public void configure(HttpSecurity http) throws Exception {
        http.oauth2ResourceServer(this::localMode);
        // if the user sends a request with an invalid accessToken (expired for example) to
        // an endpoint marked as permitall, JwtAuthenticationProvider will fail.
        // if JwtAuthenticationProvider is the last provider in ProviderManager, it will refuse
        // the user's request
        // That's why we need to have a fallback authentication provider, registered after
        // JwtAuthenticationProvider, that accept the user as anonymous.
        http.apply(new AnonymousFallbackConfigurer());
    }

    private void localMode(OAuth2ResourceServerConfigurer<HttpSecurity> oauth2) {
        oauth2
            .bearerTokenResolver(AuthenticationResponseEntity::retrieveAccessToken)
            .jwt(jwtConfigurer -> {
                jwtConfigurer.jwtAuthenticationConverter(getConverterWithRoleAuthorityPrefix());
                jwtConfigurer.decoder(jwtDecoder);
            });
    }

    private static JwtAuthenticationConverter getConverterWithRoleAuthorityPrefix() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }

    private static final class AnonymousFallbackConfigurer
        implements SecurityConfigurer<DefaultSecurityFilterChain, HttpSecurity> {

        @Override
        public void init(HttpSecurity builder) {
            builder.authenticationProvider(new AnonymousFallbackProvider());
        }

        @Override
        public void configure(HttpSecurity builder) {}

        private static final class AnonymousFallbackProvider implements AuthenticationProvider {

            @Override
            public Authentication authenticate(Authentication authentication) {
                return new AnonymousAuthenticationToken(
                    "key",
                    "anonymous",
                    AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")
                );
            }

            @Override
            public boolean supports(Class<?> authentication) {
                return true;
            }
        }
    }
}
