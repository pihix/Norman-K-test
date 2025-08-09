package com.nimbleways.springboilerplate;

import com.nimbleways.springboilerplate.common.api.LocalDevPropertySourceInjecter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@ConfigurationPropertiesScan
// why exclude? see https://stackoverflow.com/a/51948296
@SpringBootApplication(exclude = { UserDetailsServiceAutoConfiguration.class })
@EnableWebSecurity
@EnableScheduling
public class Application {

    public static final String BASE_PACKAGE_NAME = "com.nimbleways.springboilerplate";

    public static void main(final String[] args) {
        final SpringApplication application = new SpringApplication(Application.class);
        application.addListeners(new LocalDevPropertySourceInjecter());
        application.run(args);
    }
}
