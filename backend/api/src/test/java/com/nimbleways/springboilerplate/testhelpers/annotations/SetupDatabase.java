package com.nimbleways.springboilerplate.testhelpers.annotations;

import com.nimbleways.springboilerplate.common.infra.configurations.HibernateEventListenersRegistrarBeanPostProcessor;
import com.nimbleways.springboilerplate.testhelpers.configurations.IgnoreTestOnlyDbTypesConfiguration;
import com.nimbleways.springboilerplate.testhelpers.junitextensions.SetupTestDatabaseExtension;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(SetupTestDatabaseExtension.class)
@ContextConfiguration(initializers = SetupTestDatabaseExtension.Initializer.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@TestPropertySource(
    properties = {
        "spring.datasource.hikari.maximum-pool-size=10",
        "spring.datasource.hikari.minimum-idle=1",
        "spring.datasource.hikari.idle-timeout=1000",
    }
)
@Import({ HibernateEventListenersRegistrarBeanPostProcessor.class, IgnoreTestOnlyDbTypesConfiguration.class })
public @interface SetupDatabase {
}
