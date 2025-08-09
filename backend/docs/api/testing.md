# 1. Testing Strategy
Testing strategies aim to maximize confidence in our application's adherence to specifications while minimizing test execution time.

Below is the reasoning behind the testing strategy used in this backend:
- Business logic is usually the code that contains the most possible paths in the application, therefore most tests must be focused on this logic. All these tests must run quickly so unit tests are the most suited for this.
  - **=> Exhaustive unit testing of the domains**
- Exhaustive testing will lead to high coverage, but the tests can be of low quality, not detecting bugs in code. The best way to automatically assess test quality is by introducing many changes in the tested code and check that at least one test fails. Tests will be executed multiple times, so they have to be fast. The best candidate are domain unit tests. 
  - **=> Mutation testing on domain's unit tests**
- Mocks reduce the readability and the ease of maintenance of tests. If a dependency is mocked many times, it must be faked to solve mock problems. (more on fake vs mock [in this article](https://testing.googleblog.com/2024/02/increase-test-fidelity-by-avoiding-mocks.html?m=1))
  - **=> Fakes over mocks**
- Ports materialize a contract between the domain and the adapters. Ports are implemented as interfaces, it enforces the presence of methods with specific signatures, but it's not enough to guarantee the behavior required by the domain. This behavior can be checked with tests, against any adapter of the port, including the fakes.
  - **=> Contract tests for ports**
- We have to make sure that the endpoints effectively call the domain's use case and that the request and response are correctly mapped. For these tests to be as close to reality as possible, mockMvc is used and inputs/outputs are json strings, not objects.
  - **=> Endpoint-domain integration tests**
- Security, logging and global exception handling are cross cutting concerns. In order to respect the Single Responsability Principle, they must have their own tests instead of mixing it with other component tests.
  - **=> Isolated/Autonomous tests for cross-cutting concerns**
- Finally, we need a few tests to make sure that the real components works well together in scenarios that ressemble the real use of the application, namely successive calls to the API running with real dependencies. This is particulary usefull for monolith backend.
  - **=> Flow tests for key scenarios**

In summary, the testing strategy consists of:
- Exhaustive unit testing of the domains
- Mutation testing on domain's unit tests
- Fakes over mocks
- Contract tests for ports
- Endpoint-domain integration tests
- Isolated/Autonomous tests for cross-cutting concerns
- Flow tests for key scenarios

## 1.1. Exhaustive unit testing of the domains
Unit test should only target use cases and value objects containing validation rules. DO NOT test domain services or helpers directly.

Unit tests should be exhaustive and cover all edge cases supported by the use case. These tests must be considered as the source of truth of the specification of the use cases.

Unit tests must be fast. Each test must run in under 10ms.

Each use case must have a test helper class whose role is to instantiate the use case using fake adapters. The fakes are exposed as properties to participate in the test setup. The test helper class name is derived from the use case (ex: `LoginUseCase` -> `LoginSut`).

The quality of the tests of the domain are checked using mutation testing. But remember that it only covers **written code**: a score of 100 in mutation tests doesn't mean you haven't forgotten an edge case in your use case code :)

## 1.2. Fake adapters
Fakes adapters implement the port interface, but can provide any other public method deemed necessary for the tests. Ex: if a port doesn't define a findAll method, but tests need to know what entities are present in the repository for the assertions, the fake can implement it directly.

DO NOT add methods in the port for the whole sake of tests.

## 1.3. Contract tests for ports
Check this article: https://principal-it.eu/2023/02/contract-tests/

For some (all?) ports, we create abstract test classes that define the wanted behavior that adapters must satisfy, from the point of view of the domain. They contain at least one abstract method that return the instance of the adapter to test. For each adapter of the port, a subclass is created mainly to implement abstract methods needed by the contract tests.

Abstract contract test classes should not be aware of any implementation detail of the adapters. Ex: suppose we have a `ImageRepositoryPort` to persist images, if one of the adapters use a local folder to store the image, we cannot have the logic to empty the folder directly in `ImageRepositoryPortContractTests`. We should instead either declare an abstract method `cleanup` that each adapter test class will implement with the appropriate behavior and call it from the abstract class, or just add an `@AfterEach` or `@AfterAll` method in the adapter's sub test class.

## 1.4. Endpoint-domain integration tests
Those tests should be kept to a minimum. The main goal is the make sure that the raw request is correctly mapped and passed to the use case, and that the use case result is correctly mapped to the response.

For endpoint response body assertions:
- Always favor hardcoding the expected body string. It makes it easier to understand what exactly the response should look like, instead of a bunch of `%s` in place of the actual values (when using String.formatted to create the expected response). An exception can be made for generated IDs that cannot be controlled. The consequence of this rule is that entities used in such tests must be deterministic.
- If the endpoint doesn't return a body, the test must explicitly ensure it.

These tests use the real domain use case with fake adapters.

Usually there's no need to test errors because:
- the domain throws exceptions if a business rule is violated, which is checked in the use case's unit tests.
- exception handler tests must verify the json response of the API for each application-defined exception.

# 2. Testing rules
- Make sure that each test method is self-describing, specially the setup/arrange/given phase. Any developer must be able to understand quickly what scenario is being tested without having to navigate to other methods or classes. You can still use helpers (it's actually more than recommanded!) to simplify tests, but their signature must be self-describing so that it's not required to read their implementation to understand what they do.
- The prod (non-test) code MUST NOT be altered just for the sake of the test. Ex: you must not change the visibility of a method or a field for the tests needs.

# 3. Test classes categorization and naming convention
`./mvnw verify` runs the tests in three successive batches:
1. Architecture tests defined under `transverse/architecture`. They are mainly written using [ArchUnit](https://www.archunit.org/)
2. Test classes with names ending with `UnitTests` (excluding architectuure tests)
3. All remaining tests

Why ? To satisfy the Fail Fast principle: as the unit tests run fast, if one of them fails, you don't have to wait for all the slow tests to finish to get the feedback.

Test classes must have one of the following suffixes:
- `UnitTests` for unit tests
- `IntegrationTests` for integration tests
- `ApplicationTests` for application tests that load the whole Spring context (with or without database)
- `ContractTests` for abstract port contract tests.
## 3.1. Integration tests
Test classes that does not extend the `AbstractApplicationTests` class and that are annotated with one of the following annotations are considered integration tests and must have the `IntegrationTests` suffix:
- `@WebMvcTest`
- `@DataJpaTest`
- `@SetupDatabase`
All sub classes of `BaseExceptionHandlerIntegrationTests` must also have `IntegrationTests` suffix.

Additionnaly, if your test triggers out-of-process communication, like reading from or writing to the disk, but it does not need any of the above annotations, you must mark it as an integration test by using the `@IntegrationTest` annotation and the `IntegrationTests` suffix.
## 3.2. Unit tests
Unit tests must not use any of the above annotations.

All tests in the domain must be unit tests.
## 3.3. Port Contract tests
Port contract tests are abstract classes that contains at least one method with the `@Test` annotation. They must have the `ContractTests` suffix.

These classes are not directly executed, but they provide test definitions for their subclasses, which can be either unit or integration tests.

Ex: Given a `ImageRepositoryPort` port for image persisting, and two adapters `FileSystemImageRepository` and `InMemoryImageRepository`, we will have:
- `ImageRepositoryPortContractTests` that defines the required behavior in form of tests
- `FileSystemImageRepositoryIntegrationTests` that inherits from the contract test class and run the tests against the `FileSystemImageRepository` adapter. It's considered as an integration test class because it reads/writes in the host disk.
- `InMemoryImageRepositoryUnitTests` that inherits from the contract test class and run the tests against the `InMemoryImageRepository` adapter. It's considered as a unit test class because it only reads/writes in the RAM.
## 4. Testing Exceptions

### 4.1. Behavior Testing

*   **Use Case Tests:** Unit tests for your use cases should verify that the correct exceptions are thrown under specific conditions.  Use assertions like `assertThrows` to check for expected exceptions.
*   **Port Contract Tests:** Port contract tests should verify that the correct exceptions are thrown under specific conditions.

### 4.2. Api Response Format Testing
The api response for each custom exception must be tested:

*   **`BaseExceptionHandlerIntegrationTests`:**  This abstract class provides a base for testing exception handlers.  Extend this class to create integration tests for your custom exception handlers.
*   Each subclass of `BaseExceptionHandlerIntegrationTests` *must* implement:
    *  a `staticProvideExceptions()` static method that defines a set of test cases, each consisting of:
        *   An exception instance to be thrown.
        *   The expected HTTP status code.
        *   The expected JSON response body.
    * a `getExceptions()` instance method which simply returns the result of `staticProvideExceptions()`
* **Architecture Tests**:
    * Every new custom exception must have a test case inside the `staticProvideExceptions()` method of the corresponding ExceptionHandler.
    * `ExceptionHandlerRulesUnitTests` is responsible for ensuring that all ExceptionHandlers have the required testing structure.

Example:

```java
package com.nimbleways.springboilerplate.features.authentication.api.exceptionhandlers;

class AuthenticationExceptionHandlerIntegrationTests extends BaseExceptionHandlerIntegrationTests {

    @Override
    protected Stream<Arguments> getExceptions() {
        return staticProvideExceptions();
    }

    private static Stream<Arguments> staticProvideExceptions() {
        return Stream.of(
            Arguments.of(
                new BadUserCredentialException(new Username("")),
                HttpStatus.UNAUTHORIZED, """
            {"type":"about:blank","title":"errors.unauthorized","status":401,
            "detail":"errors.unauthorized","instance":"/exception-handling/throw"}"""),

            Arguments.of(
                new CannotCreateUserSessionInRepositoryException(
                    new Username(""),
                    new DataIntegrityViolationException("")),
                HttpStatus.INTERNAL_SERVER_ERROR, """
            {"type":"about:blank","title":"errors.internal_server_error","status":500,
            "detail":"errors.internal_server_error","instance":"/exception-handling/throw"}"""),

            ...
        );
    }
}
```

# 5. Query count verification for database tests
To prevent N+1 query problems and unexpected JPA/Hibernate behavior, all integration tests involving database access must assert the exact number of queries executed.

This is done using the `@AssertQueryCount` annotation at the test class level which works with the `QueryCountingExtension` JUnit extension to automatically verify query counts for each test method.

Usage:
1. Add the `@AssertQueryCount` annotation to your test class
2. For each test method, add an `@Expected` annotation specifying the method name and the exact number of queries it should execute
3. The extension will automatically track and verify query counts

Example:

```java
@SetupDatabase
@Import({UserRepository.class})
@DataJpaTest
@AssertQueryCount({
    @Expected(count = 3, method = "creating_a_new_user_succeed"),
    @Expected(count = 2, method = "creating_a_new_user_returns_the_created_user"),
    @Expected(count = 2, method = "creating_a_new_user_with_an_existing_username_throws_Exception"),
})
public class UserRepositoryIntegrationTests extends UserRepositoryPortContractTests {
    // Test methods...
}
```

If a test method exceeds or uses fewer queries than expected, the test will fail with a clear message.

If a test method is missing from the `@AssertQueryCount` declaration, the test will fail and provide the exact count and code to add:

```
Test method 'new_test_method_name' has no expected query count defined.
Add this line to the existing @AssertQueryCount annotation on your test class (manually check that the count is correct):

@Expected(count = 5, method = "new_test_method_name"),
```

This approach ensures:
- Early detection of N+1 query problems
- Performance regression prevention
- Clear documentation of the expected database interaction footprint
