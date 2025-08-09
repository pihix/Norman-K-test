package com.nimbleways.springboilerplate.common.infra.database.jparepositories;

import static org.assertj.core.api.Assertions.assertThat;

import com.nimbleways.springboilerplate.common.infra.adapters.fakes.FakeTimeProvider;
import com.nimbleways.springboilerplate.common.infra.database.entities.UserDbEntity;
import com.nimbleways.springboilerplate.testhelpers.annotations.SetupDatabase;
import com.nimbleways.springboilerplate.testhelpers.configurations.TimeTestConfiguration;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** This class test is an example on how to manage transactions
 * Inside @SetupDatabase annotation we have @Transaction that rollback everything after each test
 * Which means the database is cleared after each test
 */
@SetupDatabase
@DataJpaTest(showSql = false)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Import(TimeTestConfiguration.class)
class TransactionalExampleIntegrationTests {

    @Autowired
    private JpaUserRepository userRepository;

    @Autowired
    private FakeTimeProvider timeProvider;

    /** This test method is an example on how to disable @transactional by overriding the propagation attribute by NEVER
     * Database will be populated with one user after this test
     */
    @Test
    @Order(1)
    @Transactional(propagation = Propagation.NEVER)
    void add_user_without_transaction() {
        // Act
        userRepository.save(createUser("user1", "pwd1"));

        assertThat(userRepository.findAll()).hasSize(1);
    }

    /** Inside @SetupDatabase annotation we have @Transactional, so every transaction made inside this test will be rollback
     * Database will have only one user after this test ( the one persisted in the first test method )
     */
    @Test
    @Order(2)
    void add_a_second_user_with_transaction() {
        // Act
        userRepository.save(createUser("user2", "pwd2"));

        assertThat(userRepository.findAll()).hasSize(2);
    }

    @Test
    @Order(3)
    void add_another_second_user_with_transaction() {
        // Act
        userRepository.save(createUser("user3", "pwd3"));

        assertThat(userRepository.findAll()).hasSize(2);
    }

    @Test
    @Order(4)
    @Transactional(propagation = Propagation.NEVER)
    void delete_all_users_without_transaction() {
        // Act
        userRepository.deleteAll();

        assertThat(userRepository.findAll()).hasSize(0);
    }

    @Test
    @Order(5)
    @Transactional(propagation = Propagation.NEVER)
    void ensure_there_are_no_users() {
        assertThat(userRepository.findAll()).hasSize(0);
    }

    @NotNull private UserDbEntity createUser(String username, String password) {
        UserDbEntity user = new UserDbEntity();
        user.username(username);
        user.password(password);
        user.name(username);
        user.createdAt(timeProvider.instant());
        return user;
    }
}
