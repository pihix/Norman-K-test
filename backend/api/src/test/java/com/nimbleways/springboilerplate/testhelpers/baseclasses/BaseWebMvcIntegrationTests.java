package com.nimbleways.springboilerplate.testhelpers.baseclasses;

import static com.nimbleways.springboilerplate.testhelpers.helpers.Mapper.toUserPrincipal;
import static com.nimbleways.springboilerplate.testhelpers.utils.JsonUtils.jsonIgnoreArrayOrder;
import static com.nimbleways.springboilerplate.testhelpers.utils.StringUtils.urlEncode;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import com.nimbleways.springboilerplate.Application;
import com.nimbleways.springboilerplate.common.api.LoggingEventListener;
import com.nimbleways.springboilerplate.common.api.beans.ObjectMapperConfiguration;
import com.nimbleways.springboilerplate.common.api.security.StandaloneJwtAuthentication;
import com.nimbleways.springboilerplate.common.api.security.WebSecurityConfiguration;
import com.nimbleways.springboilerplate.common.domain.ports.TimeProviderPort;
import com.nimbleways.springboilerplate.common.infra.adapters.SpringEventPublisher;
import com.nimbleways.springboilerplate.common.infra.adapters.fakes.FakeTokenClaimsCodec;
import com.nimbleways.springboilerplate.features.authentication.domain.entities.UserPrincipal;
import com.nimbleways.springboilerplate.features.authentication.domain.properties.TokenProperties;
import com.nimbleways.springboilerplate.features.authentication.domain.usecases.helpers.UserTokensHelpers;
import com.nimbleways.springboilerplate.features.authentication.domain.valueobjects.AccessToken;
import com.nimbleways.springboilerplate.features.users.domain.entities.User;
import com.nimbleways.springboilerplate.testhelpers.baseclasses.BaseWebMvcIntegrationTests.WebMvcIntegrationTestConfiguration;
import com.nimbleways.springboilerplate.testhelpers.configurations.PropertiesTestConfiguration;
import com.nimbleways.springboilerplate.testhelpers.configurations.TimeTestConfiguration;
import com.nimbleways.springboilerplate.testhelpers.utils.ClearableProxiedThreadScope;
import jakarta.servlet.http.Cookie;
import lombok.Getter;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.*;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

@Import(
    {
        WebSecurityConfiguration.class,
        StandaloneJwtAuthentication.class,
        FakeTokenClaimsCodec.class,
        TimeTestConfiguration.class,
        PropertiesTestConfiguration.class,
        LoggingEventListener.class,
        WebMvcIntegrationTestConfiguration.class,
        ObjectMapperConfiguration.class,
    }
)
@ExtendWith(BaseWebMvcIntegrationTests.WebMvcIntegrationTestExtension.class)
@Getter
public abstract class BaseWebMvcIntegrationTests implements UserTokensHelpers {

    private static final String SCOPE_NAME = "perCall";
    private static final ClearableProxiedThreadScope SCOPE = new ClearableProxiedThreadScope();

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    private TimeProviderPort timeProvider;

    @Autowired
    private FakeTokenClaimsCodec tokenClaimsCodec;

    @Autowired
    private TokenProperties tokenProperties;

    protected BaseWebMvcIntegrationTests() {}

    protected Cookie getAccessTokenCookie(User user) {
        return getAccessTokenCookie(toUserPrincipal(user));
    }

    protected Cookie getAccessTokenCookie(UserPrincipal userPrincipal) {
        AccessToken accessToken = newAccessToken(userPrincipal);
        return new Cookie("accessToken", urlEncode(accessToken.value()));
    }

    protected ResultMatcher jsonStrictArrayOrder(String jsonContent) {
        return content().json(jsonContent, JsonCompareMode.STRICT);
    }

    protected ResultMatcher jsonIgnoreArrayOrder(String jsonContent) {
        return content().json(jsonContent, jsonIgnoreArrayOrder);
    }

    protected static ResultMatcher noContent() {
        return content().string("");
    }

    static final class WebMvcIntegrationTestExtension implements AfterEachCallback {

        @Override
        public void afterEach(ExtensionContext context) {
            SCOPE.clear();
        }
    }

    @TestConfiguration
    static class WebMvcIntegrationTestConfiguration implements BeanFactoryPostProcessor {

        @Bean
        public ILoggerFactory loggerFactory() {
            return LoggerFactory.getILoggerFactory();
        }

        @Bean
        @Primary
        public SpringEventPublisher springEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
            return new SpringEventPublisher(applicationEventPublisher);
        }

        @Override
        public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
            beanFactory.registerScope(SCOPE_NAME, SCOPE);
            for (String bean : beanFactory.getBeanDefinitionNames()) {
                BeanDefinition beanDefinition = beanFactory.getBeanDefinition(bean);
                String beanClassName = beanDefinition.getBeanClassName();
                if (beanClassName != null && beanClassName.startsWith(Application.BASE_PACKAGE_NAME)) {
                    beanDefinition.setScope(SCOPE_NAME);
                }
            }
        }
    }
}
