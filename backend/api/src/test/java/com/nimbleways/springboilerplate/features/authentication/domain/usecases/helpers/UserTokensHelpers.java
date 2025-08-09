package com.nimbleways.springboilerplate.features.authentication.domain.usecases.helpers;

import static com.nimbleways.springboilerplate.testhelpers.fixtures.SimpleFixture.aUserTokens;
import static com.nimbleways.springboilerplate.testhelpers.helpers.Mapper.toUserPrincipal;

import com.nimbleways.springboilerplate.common.domain.ports.TimeProviderPort;
import com.nimbleways.springboilerplate.features.authentication.domain.entities.TokenClaims;
import com.nimbleways.springboilerplate.features.authentication.domain.entities.UserPrincipal;
import com.nimbleways.springboilerplate.features.authentication.domain.ports.TokenClaimsCodecPort;
import com.nimbleways.springboilerplate.features.authentication.domain.properties.TokenProperties;
import com.nimbleways.springboilerplate.features.authentication.domain.valueobjects.AccessToken;
import com.nimbleways.springboilerplate.features.authentication.domain.valueobjects.UserTokens;
import com.nimbleways.springboilerplate.features.users.domain.entities.User;
import java.time.Instant;

public interface UserTokensHelpers {
    TokenClaimsCodecPort tokenClaimsCodec();
    TimeProviderPort timeProvider();
    TokenProperties tokenProperties();

    default UserTokens newUserTokens(User user) {
        UserPrincipal userPrincipal = toUserPrincipal(user);
        AccessToken accessToken = newAccessToken(userPrincipal);
        return aUserTokens(accessToken);
    }

    default AccessToken newAccessToken(UserPrincipal userPrincipal) {
        Instant now = timeProvider().instant();
        TokenClaims tokenClaims = new TokenClaims(
            userPrincipal,
            now,
            now.plus(tokenProperties().accessTokenValidityDuration())
        );
        return tokenClaimsCodec().encode(tokenClaims);
    }
}
