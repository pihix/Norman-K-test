# Project Structure
- Project root
  - features
    - {subdomain_name}
      - api
      - domain
    - ...
  - common
    - api
    - domain
    - infra
    - utils

To understand domain vs subdomain, please read this comment: https://stackoverflow.com/a/73080428
## 1 `features`
The `features` folder contains one folder per subdomain, which itself contains the api and domain code in seperate folders.
### 1.1 `domain`
The `domain` folder contains the business logic of the subdomain.
#### 1.1.1 Overview
- domain
  - entities
  - events
  - exceptions
  - ports
  - properties
  - services
  - usecases
    - {usecase_name}
      - {usecase_name}UseCase
      - {usecase_name}Command
    - ...
  - valueobjects
### 1.1.2 `usecases`
- `{usecase_name}UseCase`:
  - A class containing a single public method that takes at most one argument. It returns void, domain entities, domain value objects, or predefined Java types.
  - It may depend on ports and domain services but should not depend on other use cases. Code sharing among use cases should preferably be done through domain services.
- `{usecase_name}Command`:
  - A record or class containing the information needed by the UseCase.
  - It should not be reused between use cases.
### 1.1.3 `services`
This folder contains domain-specific logic shared between use cases of the same subdomain.
### 1.1.4 `entities`
This folder contains records or classes that represent [entities in the context of DDD](https://kisztof.medium.com/entity-ddd-series-part-7-2095ba4ac0f3)
### 1.1.5 `valueobjects`
This folder contains records or classes that represent [value objects according to DDD principles](https://medium.com/swlh/value-objects-to-the-rescue-28c563ad97c6).

Value objects are useful for avoiding a [stringly typed](https://devcards.io/stringly-typed) codebase and enforcing certain business rules.
### 1.1.6 `exceptions`
Avoid passing message strings to exception constructors. Exception names should be explicit and specific, with any error-related information passed to the constructors. Each exception can then generate its own message using this information.

Ex:
```java
public class BadUserCredentialException extends AbstractAuthenticationDomainException {
    public BadUserCredentialException(final Username username) {
        super(String.format("Bad password provided for username: %s", username.value()));
    }
}
```
### 1.1.7 `events`
The domain can raise events that can be intercepted by other parts of the application, aiding in decoupling the main logic from side effects.

Events are types whose names represent events that have occurred, containing any relevant event data. For example, `UserLoggedInEvent` is raised when a user successfully logs in, and it contains a `userPrincipal` property describing that user.

The boilerplate includes a default event listener, `LoggingEventListener`, capturing all events implementing the `common.domain.events.Event` interface and logging them using the event's `toString()`. Thus, events must override their `toString()` to provide a meaningful textual representation.

To prevent the `LoggingEventListener` class from being identified as the origin of all logs in the application, events should declare the `sourceType` that raised them, that will be used as the origin in the logs.

Ex:
```java
public record UserLoggedInEvent(UserPrincipal userPrincipal, Class<?> sourceType) implements Event {
    @Override
    public String toString() {
        return String.format("Login attempt successful for user with id: %s", userPrincipal.id());
    }
}
```
### 1.2 `api`
#### 1.2.1 Overview
- api
  - endpoints
    - {endpoint_name}
      - {endpoint_name}Endpoint
      - {endpoint_name}EndpointRequest
      - {endpoint_name}EndpointResponse
    - ...
  - schedules
    - {schedule_name}
      - {schedule_name}ScheduledTask
    - ...
  - services
#### 1.2.2 `endpoints`
Endpoint design follows the [Request-Endpoint-Response (REPR) pattern](https://deviq.com/design-patterns/repr-design-pattern):
- `{endpoint_name}Endpoint`:
  - A controller class containing a single endpoint, its only public method. Why ? This ensures explicit and minimal dependencies for each endpoint.
  - It should not contain any business logic but instead call a UseCase from the domain.
- `{endpoint_name}EndpointRequest`:
  - A record or class representing the request for this specific endpoint.
  - It should not be reused between endpoints. Why? [DRY](https://codeconservatory.com/blog/post/dry/) can lead to endpoints coupling and accidental API changes.
  - It should only contain predefined nullable types from Java or types defined within the request type. This excludes value (primitive) types like `boolean` or `int`, as validation annotations such as `@NotNull` and `@NotBlank` won't work on them, defaulting to their default value if the property is missing in the request JSON.
  - Custom types defined outside the request type should not be included to avoid unnecessary coupling and accidental API changes.
  - All validations performed in the controller or request type must be a subset of validations done by the domain (entities, value objects, or use cases). For example, if a rating integer must range between 1 and 5, it should not be solely checked in the request with annotations; the domain remains primarily responsible for all business rules and must also validate it. For this example, a value object representing a rating could be created to validate the value in its constructor.
- `{endpoint_name}EndpointResponse`:
  - A record or class representing the response for this specific endpoint.
  - It should not be reused between endpoints.
  - It should only contain predefined types from Java or types defined within the response type. Value types (like `boolean`, `int`, etc.) can be used since there's no need to validate API responses.
#### 1.2.3 `schedules`
- `{schedule_name}ScheduledTask`:
  - A class that contains related scheduled methods. All scheduled methods in the same class should have the same dependencies (ie, avoid having a dependency not used by all the scheduled methods in the class). Why ? The dependencies injected during each scheduled method execution are minimal.
#### 1.2.4 `services`
This folder contains shared api-specific logic between *some* endpoints. Ex: logic to handle cookies in responses that is used by both login and refreshtoken endpoints.
## 2 `common`
### 2.1 `api`
This folder contains:
- dependency injection setup
- cross-cutting concerns like:
  - security
  - exception handling
  - logging
- other configurations
### 2.2 `domain`
This folder contains common domain elements like:
- base classes for events, exceptions, ...
- shared objects between subdomains (value objects, ports, ...)
It must not contains domain entities.
### 2.3 `infra`
For small applications, implementations for all subdomains' ports can be grouped in this folder.

Its hierarchy appears as follows:
- `infra`
  - `adapters`: classes implementing domain ports
  - `database`
    - `entities`: entities mapped to the database tables
    - `jparepositories`: JPA repositories provided by Spring
  - `properties`: configuration properties needed by the other classes in `infra`
### 2.4 `utils`
This folder contains utility methods and classes used by all code in the application.

A utility method is a method that is domain-agnostic and can be used in any project, e.g., a method that converts a String array to a CSV line.

Do not confuse with helper methods that are domain-specific, e.g., a method that takes an array of `Rating` value objects and returns the average. Helper methods must not be placed in the `utils` folder.
## 3 Rules
- Subdomains can only depend on standard Java classes, the common domain, and the common utils
- Common domain can only depend on standard Java classes and the common utils
- Common utils can only depend on standard Java classes
- Domain should raise events when actions are done with success or failure
- Domain must not perform any I/O (input/output) operation, like reading/writing from/to disk, calling an API, or communicating with another process. All I/O operations must be abstracted with a port and an appropriate adapter.
- In the API layer, requests and responses must not be shared between endpoints. This favors tailoring the request and response for the specific needs of each endpoint.