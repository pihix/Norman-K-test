package com.nimbleways.springboilerplate.testhelpers.helpers;

import com.nimbleways.springboilerplate.features.authentication.domain.valueobjects.UserTokens;
import com.nimbleways.springboilerplate.testhelpers.utils.StringUtils;

public interface TokenHelpers {
    static String urlEncodeAccessToken(UserTokens userTokens) {
        return StringUtils.urlEncode(userTokens.accessToken().value());
    }

    static String urlEncodeRefreshToken(UserTokens userTokens) {
        return StringUtils.urlEncode(userTokens.refreshToken().value());
    }
}
