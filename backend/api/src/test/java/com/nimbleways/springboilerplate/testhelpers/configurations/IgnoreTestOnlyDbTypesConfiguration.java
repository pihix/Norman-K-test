package com.nimbleways.springboilerplate.testhelpers.configurations;

import static com.nimbleways.springboilerplate.Application.BASE_PACKAGE_NAME;

import com.nimbleways.springboilerplate.Application;
import com.nimbleways.springboilerplate.testhelpers.annotations.IncludeTestOnlyDbTypes;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.persistenceunit.ManagedClassNameFilter;

@ConditionalOnProperty(value = IncludeTestOnlyDbTypes.PROPERTY_NAME, havingValue = "false", matchIfMissing = true)
@TestConfiguration
@EnableJpaRepositories(
    basePackageClasses = Application.class,
    excludeFilters = { @ComponentScan.Filter(type = FilterType.ASPECTJ, pattern = BASE_PACKAGE_NAME + "..TestOnly*") }
)
@Import(IgnoreTestOnlyDbTypesConfiguration.IgnoreFilter.class)
public class IgnoreTestOnlyDbTypesConfiguration {

    static class IgnoreFilter implements ManagedClassNameFilter {

        @Override
        public boolean matches(String className) {
            return !className.contains(".TestOnly");
        }
    }
}
