package com.nimbleways.springboilerplate.common.infra.database;

import static com.nimbleways.springboilerplate.Application.BASE_PACKAGE_NAME;
import static org.junit.jupiter.api.Assertions.*;

import com.nimbleways.springboilerplate.testhelpers.configurations.IgnoreTestOnlyDbTypesConfiguration;
import com.nimbleways.springboilerplate.testhelpers.junitextensions.FailFastExtension;
import com.nimbleways.springboilerplate.testhelpers.junitextensions.SetupTestDatabaseExtension;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.Optional;
import javax.sql.DataSource;
import javax.xml.parsers.ParserConfigurationException;
import liquibase.CatalogAndSchema;
import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.ObjectChangeFilter;
import liquibase.diff.output.StandardObjectChangeFilter;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.integration.commandline.CommandLineUtils;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@DataJpaTest
@ExtendWith(FailFastExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Import(IgnoreTestOnlyDbTypesConfiguration.class)
@SuppressWarnings("PMD.ExcessiveImports")
class LiquibaseMigrationIntegrationTests {

    @Container
    private static final JdbcDatabaseContainer<?> testDatabase =
        SetupTestDatabaseExtension.createJdbcDatabaseContainer();

    private static final ResourceAccessor resourceAccessor = new ClassLoaderResourceAccessor();
    private static final String changeLogFile = "db/changelog-master.yaml";
    private static final String hibernateReferenceUrl =
        "hibernate:spring:" + BASE_PACKAGE_NAME + "?dialect=org.hibernate.dialect.PostgreSQLDialect";

    @Autowired
    private DataSource dataSource;

    @TempDir
    private Path tempDir;

    @Autowired
    private SessionFactory sessionFactory;

    @Test
    @Order(1)
    void liquibase_generate_non_empty_diff_against_fresh_database() throws Exception {
        Optional<String> diffAsYamlOptional;
        try (Connection connection = dataSource.getConnection()) {
            Database targetDatabase = getDatabase(connection);
            Database referenceDatabase = getHibernateReferenceDatabase();
            diffAsYamlOptional = getDiffAsYaml(referenceDatabase, targetDatabase);
        }
        assertTrue(
            diffAsYamlOptional.isPresent(),
            "diff on fresh database is expected to have content, but it was empty."
        );
    }

    @Test
    @Order(2)
    void liquibase_generate_empty_diff_after_applying_all_migrations() throws Exception {
        Optional<String> diffAsYamlOptional;
        try (Connection connection = dataSource.getConnection()) {
            Database targetDatabase = getDatabase(connection);
            applyMigrations(targetDatabase);
            Database referenceDatabase = getHibernateReferenceDatabase();
            diffAsYamlOptional = getDiffAsYaml(referenceDatabase, targetDatabase);
        }
        assertTrue(
            diffAsYamlOptional.isEmpty(),
            () ->
                "diff after applying all migrations is expected to be empty, but found this instead:\n\n" +
                diffAsYamlOptional.orElseThrow()
        );
    }

    @Test
    @Order(3)
    void hibernate_successfully_validate_schema_created_by_liquibase() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            Database targetDatabase = getDatabase(connection);
            applyMigrations(targetDatabase);
        }

        assertDoesNotThrow(
            () -> sessionFactory.getSchemaManager().validateMappedObjects(),
            "hibernate schema validation failed after applying all migrations"
        );
    }

    @DynamicPropertySource
    private static void registerDatasourceProperties(DynamicPropertyRegistry registry) {
        if (testDatabase.isCreated()) {
            testDatabase.close();
        }
        testDatabase.start();
        registry.add("spring.datasource.url", testDatabase::getJdbcUrl);
        registry.add("spring.datasource.username", testDatabase::getUsername);
        registry.add("spring.datasource.password", testDatabase::getPassword);
        registry.add("spring.liquibase.enabled", () -> "false");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
        registry.add("spring.sql.init.mode", () -> "never");
    }

    private static Database getHibernateReferenceDatabase() throws DatabaseException {
        return DatabaseFactory.getInstance().openDatabase(hibernateReferenceUrl, null, null, null, resourceAccessor);
    }

    private static Database getDatabase(Connection connection) throws DatabaseException {
        return DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
    }

    private static void applyMigrations(Database targetDatabase) throws LiquibaseException {
        Liquibase liquibase = new Liquibase(changeLogFile, resourceAccessor, targetDatabase);
        liquibase.update(new Contexts());
    }

    private Optional<String> getDiffAsYaml(Database referenceDatabase, Database targetDatabase)
        throws LiquibaseException, IOException, ParserConfigurationException {
        Path diffFile = tempDir.resolve("diff.sql");
        DiffOutputControl diffOutputControl = new DiffOutputControl(false, false, false, null).addIncludedSchema(
            new CatalogAndSchema(null, null)
        );
        ObjectChangeFilter objectChangeFilter = new StandardObjectChangeFilter(
            StandardObjectChangeFilter.FilterType.EXCLUDE,
            "table:testonly_.+"
        );
        CommandLineUtils.doDiffToChangeLog(
            diffFile.toAbsolutePath().toString(),
            referenceDatabase,
            targetDatabase,
            null,
            diffOutputControl,
            objectChangeFilter,
            null,
            null,
            "none",
            "none"
        );
        if (Files.notExists(diffFile)) {
            return Optional.empty();
        }
        return Optional.of(Files.readString(diffFile));
    }
}
