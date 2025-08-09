package com.nimbleways.springboilerplate.testhelpers.utils;

import com.nimbleways.springboilerplate.common.utils.collections.Mutable;
import java.net.URI;
import java.util.Collection;
import org.eclipse.collections.api.map.MutableMap;
import org.springframework.http.ResponseCookie;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;

public class CookieStore {

    private final MutableMap<String, ResponseCookie> cookiesStore = Mutable.map.empty();

    public ExchangeFilterFunction getFilter() {
        return (request, next) -> {
            ClientRequest.Builder requestBuilder = ClientRequest.from(request);
            injectMatchingCookies(requestBuilder, request.url());
            return next.exchange(requestBuilder.build()).doOnNext(this::storeNewCookies);
        };
    }

    private void injectMatchingCookies(ClientRequest.Builder requestBuilder, URI url) {
        cookiesStore
            .values()
            .stream()
            .filter(cookie -> mustInject(url, cookie))
            .forEach(c -> requestBuilder.cookie(c.getName(), c.getValue()));
    }

    private static boolean mustInject(URI url, ResponseCookie cookie) {
        return cookie.getPath() != null && url.getPath().startsWith(cookie.getPath());
    }

    private void storeNewCookies(ClientResponse clientResponse) {
        clientResponse
            .cookies()
            .values()
            .stream()
            .flatMap(Collection::stream)
            .forEach(cookie -> {
                String cookieKey = cookie.getName() + ':' + cookie.getPath();
                if (cookie.getMaxAge().isZero()) {
                    cookiesStore.remove(cookieKey);
                    return;
                }
                cookiesStore.put(cookieKey, cookie);
            });
    }
}
