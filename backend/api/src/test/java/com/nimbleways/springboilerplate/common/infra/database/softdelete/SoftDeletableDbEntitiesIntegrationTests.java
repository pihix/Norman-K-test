package com.nimbleways.springboilerplate.common.infra.database.softdelete;

import static com.nimbleways.springboilerplate.common.infra.database.softdelete.TestOnlySoftDeletableMainDbEntity.TABLE_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.nimbleways.springboilerplate.testhelpers.annotations.IncludeTestOnlyDbTypes;
import com.nimbleways.springboilerplate.testhelpers.annotations.SetupDatabase;
import jakarta.persistence.*;
import jakarta.transaction.Transactional;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import org.eclipse.collections.api.list.ImmutableList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@SetupDatabase
@DataJpaTest
@Transactional
@IncludeTestOnlyDbTypes
class SoftDeletableDbEntitiesIntegrationTests {

    @Autowired
    private TestOnlyJpaSoftDeletableMainRepository jpaSoftDeletableTestRepository;

    @Autowired
    private TestOnlyJpaSoftDeletableDependentRepository jpaDependentEntityTestRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeAll
    static void createIndex(@Autowired DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            statement.execute(
                """
                CREATE UNIQUE INDEX testonly_index_name
                    ON %s(value)
                    WHERE deleted_at IS NULL
                """.formatted(TABLE_NAME)
            );
        }
    }

    @Test
    void soft_deleting_then_creating_a_new_entity_does_not_throw_when_using_jpa_repositories() {
        TestOnlySoftDeletableMainDbEntity entity = insertNewEntityWithValueEqualsFive();
        jpaSoftDeletableTestRepository.delete(entity);

        Assertions.assertDoesNotThrow(this::insertNewEntityWithValueEqualsFive);

        assertExistingMainDbEntities();
    }

    @Test
    void soft_deleting_then_creating_a_new_entity_does_not_throw_when_using_entity_manager() {
        TestOnlySoftDeletableMainDbEntity entity = new TestOnlySoftDeletableMainDbEntity(5);
        entityManager.persist(entity);
        entityManager.remove(entity);

        Assertions.assertDoesNotThrow(this::insertNewEntityWithValueEqualsFive);

        assertExistingMainDbEntities();
    }

    @Test
    void soft_deleting_in_cascade_then_creating_a_new_entity_does_not_throw_when_using_jpa_repositories() {
        // Create mainEntity
        TestOnlySoftDeletableMainDbEntity mainEntity = insertNewEntityWithValueEqualsFive();

        // Create then delete dependentEntity referencing mainEntity
        TestOnlySoftDeletableDependentDbEntity dependentEntity = new TestOnlySoftDeletableDependentDbEntity(mainEntity);
        jpaDependentEntityTestRepository.saveAndFlush(dependentEntity);
        jpaDependentEntityTestRepository.delete(dependentEntity);

        Assertions.assertDoesNotThrow(this::insertNewEntityWithValueEqualsFive);

        assertExistingMainDbEntities();
        assertExistingDependentDbEntities();
    }

    @Test
    void soft_deleting_in_cascade_then_creating_a_new_entity_does_not_throw_when_using_entity_manager() {
        // Create mainEntity
        TestOnlySoftDeletableMainDbEntity mainEntity = new TestOnlySoftDeletableMainDbEntity(5);
        entityManager.persist(mainEntity);

        // Create then delete dependentEntity referencing mainEntity
        TestOnlySoftDeletableDependentDbEntity dependentEntity = new TestOnlySoftDeletableDependentDbEntity(mainEntity);
        entityManager.persist(dependentEntity);
        entityManager.flush();
        entityManager.remove(dependentEntity);

        Assertions.assertDoesNotThrow(this::insertNewEntityWithValueEqualsFive);

        assertExistingMainDbEntities();
        assertExistingDependentDbEntities();
    }

    private TestOnlySoftDeletableMainDbEntity insertNewEntityWithValueEqualsFive() {
        TestOnlySoftDeletableMainDbEntity entity = new TestOnlySoftDeletableMainDbEntity(5);
        jpaSoftDeletableTestRepository.saveAndFlush(entity);
        return entity;
    }

    private void assertExistingDependentDbEntities() {
        ImmutableList<TestOnlySoftDeletableDependentDbEntity> dependentEntities =
            jpaDependentEntityTestRepository.findAllBypassRestriction();
        assertEquals(1, dependentEntities.size());
        assertThat(dependentEntities).anyMatch(dependent -> dependent.deletedAt() != null);
    }

    private void assertExistingMainDbEntities() {
        ImmutableList<TestOnlySoftDeletableMainDbEntity> mainEntities =
            jpaSoftDeletableTestRepository.findAllBypassRestriction();
        assertEquals(2, mainEntities.size());
        assertThat(mainEntities).anyMatch(main -> main.deletedAt() == null);
        assertThat(mainEntities).anyMatch(main -> main.deletedAt() != null);
    }
}
