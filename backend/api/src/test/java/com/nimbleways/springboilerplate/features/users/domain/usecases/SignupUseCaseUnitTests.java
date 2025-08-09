package com.nimbleways.springboilerplate.features.users.domain.usecases;

import static com.nimbleways.springboilerplate.features.users.domain.entities.UserBuilder.aUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nimbleways.springboilerplate.common.domain.valueobjects.EncodedPassword;
import com.nimbleways.springboilerplate.common.domain.valueobjects.Role;
import com.nimbleways.springboilerplate.common.domain.valueobjects.Username;
import com.nimbleways.springboilerplate.common.utils.collections.Immutable;
import com.nimbleways.springboilerplate.features.users.domain.entities.User;
import com.nimbleways.springboilerplate.features.users.domain.usecases.signup.SignupCommand;
import com.nimbleways.springboilerplate.features.users.domain.usecases.suts.SignupSut;
import com.nimbleways.springboilerplate.testhelpers.annotations.UnitTest;
import com.nimbleways.springboilerplate.testhelpers.utils.Instance;
import java.util.List;
import java.util.UUID;
import org.eclipse.collections.api.list.ImmutableList;
import org.junit.jupiter.api.Test;

@UnitTest
class SignupUseCaseUnitTests {

    private final SignupSut sut = Instance.create(SignupSut.class);

    @Test
    void signup_create_a_new_user_in_repository() {
        SignupCommand signupCommand = createSignupCommand();

        // Act
        User user = sut.signup(signupCommand);

        ImmutableList<User> users = sut.infra().userRepository().findAll();
        assertEquals(List.of(user), users);
    }

    @Test
    void signup_returns_the_created_user() {
        SignupCommand signupCommand = createSignupCommand();
        User expectedUser = getUser(signupCommand);

        // Act
        User user = sut.signup(signupCommand);

        assertThat(user).usingRecursiveComparison().ignoringFields("id", "roles.id").isEqualTo(expectedUser);
    }

    @Test
    void signup_create_usercredentials_in_repository() {
        SignupCommand signupCommand = createSignupCommand();

        // Act
        sut.signup(signupCommand);

        EncodedPassword encodedPassword = getPasswordFromRepository(signupCommand.username());
        boolean passwordMatches = sut.infra().passwordEncoder().matches(signupCommand.plainPassword(), encodedPassword);
        assertTrue(passwordMatches);
    }

    private EncodedPassword getPasswordFromRepository(Username username) {
        return sut.infra().userRepository().findUserCredentialByUsername(username).orElseThrow().encodedPassword();
    }

    private User getUser(SignupCommand signupCommand) {
        return new User(
            UUID.randomUUID(),
            signupCommand.name(),
            signupCommand.username(),
            sut.infra().timeProvider().instant(),
            signupCommand.roles()
        );
    }

    private static SignupCommand createSignupCommand() {
        User inputUser = aUser().roles(Immutable.set.of(Role.ADMIN)).build();
        return new SignupCommand(inputUser.name(), inputUser.username(), "password", inputUser.roles());
    }
}
