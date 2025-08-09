This is the documentation of the backend.

# 1. Project Architecture
This project uses the hexagonal architecture:

![Hexagonal architecture schema](resources/hexagonal_architecture.png)

The main goal of this architecture is to separate business code from technical code.

Technical code is split between driver code (User interface) and driven code (Infrastructure)

# 2. Patterns used
## 2.1 Request-Endpoint-Response (REPR)
Extract from: https://deviq.com/design-patterns/repr-design-pattern
> Using this approach, your API is designed around individual endpoint classes. Each one has a single Handle method that acts just like a single Controller action (because it is, under the covers). Each endpoint can define an optional Request type and an optional Response type. All together, you define endpoints using just these types:
> 
> - EndpointRequest
> - Endpoint
> - EndpointResponse

## 2.2 Nullability checks
See [Nullability checks](nullability_check.md)


## 2.3 Immutable by Default, Mutable by Necessity
Immutability is a key pattern. Benefits include:

- Improved readability: Data passed into methods is guaranteed not to change unless explicitly designed to, reducing the need to trace code for unexpected modifications.
- Reduced cloning: Immutable objects can be reused confidently, avoiding unnecessary copies and improving performance.
- Explicit intent: Mutability is only allowed when necessary and clearly defined, ensuring changes are deliberate and controlled.
- Concurrency simplicity: Immutable objects eliminate the need for locks, making concurrent programming simpler and less error-prone.

### 2.3.1 Object immutability
This is achieved through the use of records that only expose getters without setters. Note that it only provides shallow immutablility (the outer object is immutable, but its internal elements can still be modified), so the record is as immutable as the types of its attributes.

### 2.3.2 Collection immutability
Java provides immutable collections, but they implement the classic interfaces (List, Set, Map), so they expose mutation methods, like add(), which throw at runtime.
This is not an acceptable solution, as it doesn't help to benefit from true immutability (see [contractual vs structural vs verifiable immutability](https://medium.com/javarevisited/immutable-collections-in-java-using-sealed-types-ae8eb580fc1e)).

We use [Eclipse Collections](https://eclipse.dev/collections/) that provides immutable collection interfaces in addition so some nice features (see [Top Ten Reasons to use Eclipse collections](https://medium.com/oracledevs/ten-reasons-to-use-eclipse-collections-91593104af9d)).

As a consequence, built-in java collection interfaces are not allowed in method signatures (arguments and return type) and class/record attributes. You must use Eclipse Collections interfaces:
- Mutable collection interfaces: `MutableList`, `MutableSet`, `MutableMap`, `MutableBag`, ...
- Immutable collection interfaces: `ImmutableList`, `ImmutableSet`, `ImmutableMap`, `ImmutableBag`, ...

Two collection utility classes, `Mutable` and `Immutable`, are provided in the boilerplate to ease the creation of mutable and immutable collections. They must be used instead of the default factory methods that comes with Eclipse Collections (`Lists`, `Sets`, `Maps`, ...).

Unfortunately, Hibernate does not support Eclipse Collections yet, so built-in java collection interfaces are still allowed in @Entity classes.

Tutorials:
- [Mutable collections](https://medium.com/@donraab/as-a-matter-of-factory-part-1-mutable-75cc2c5d72d9)
- [Immutable collections](https://medium.com/javarevisited/as-a-matter-of-factory-part-2-immutable-8cb72ff897ee)

# 3. Project Structure
See [Project structure](project_structure.md)

# 4. Dependency Injection
Domain classes that need to be injected cannot be annotated with `@Component`, `@Service`, ... because the domain cannot depend on Spring. The solution is to use the `@ComponentScan` annotation in the api layer to help Spring find those classes.

This configuration is done in the file `./common/api/beans/DomainBeanConfiguration.java`, it will register all classes under the `domain.usecases` packages. 

For all other classes outside the domain, Spring annotations are used.

# 5. Testing
See [Testing](testing.md)

# 6. Walkthrough for exposing a new use case via an endpoint
Let's suppose we want to create a new use case to buy an item in the `orders` subdomain, we will name it `OrderItemUseCase`. 

Below are the step to follow:
## 6.1. Create the use case
1. create the use case folder `features/orders/domain/usecases/orderitem/` under the `main` folder.
2. in that new folder, create the command or query record if relevent, which in our case is a `OrderItemCommand` record, then create the `OrderItemUseCase` class with an empty public `handle` method that takes the created command or query as argument. Make sure to follow [the use case rules](project_structure.md#112-usecases).
3. in the folder `features/orders/domain/usecases/` under the `test` folder, create the `OrderItemUseCaseUnitTests` class
4. for each defined behavior of the new use case (TDD-style):
    1. write a corresponding unit test method in `OrderItemUseCaseUnitTests`
    2. make it work. If the behavior requires an out-of-process communication, it must be abstracted behind a port:
        1. if the port and a fake adapter both already exist, use the fake.
        2. otherwise, mock the port in the test
    3. refactor
5. if you had to mock a port that you didn't create, create a fake adapter and replace port usage in all tests with the fake. See Fakes over mocks in [the testing strategy](testing.md#1-testing-strategy).
6. If you created a new port, let's say `StockAvailabilityPort`:
    1. create an abstract test class `StockAvailabilityPortContractTests` where you will define the specification of the port from the point of view of the domain. Add an abstract method that is responsible for returning the actual instance of the port adapter to be tested.
    2. for each port adapter, create a subclass of the contract tests class that implement the abstract method so that it returns an instance of the adapter. For more details, see [Contract tests](testing.md#33-contract-tests).
7. if you created new domain exceptions, make sure to add them in the `provideExceptions` method of the `GlobalExceptionHandlerIntegrationTests` class along with the expected json response.
## 6.2. Create the endpoint
1. create the endpoint folder `features/orders/api/endpoints/orderitem/` under the `main` folder.
2. in that new folder, create the request and/or response records if relevant, which in our case are `OrderItemEndpointRequest` and `OrderItemEndpointResponse` records, then create the `OrderItemEndpoint` class with an empty public `handle` method that use the request and/or response records in its signature. Make sure to follow [Request and Response rules](project_structure.md#122-endpoints).
3. in the folder `features/orders/api/endpoints/` under the `test` folder, create a `OrderItemEndpointIntegrationTests` test class that extends `BaseWebMvcIntegrationTests`.
4. create just enough tests to verify that the endpoint returns the right response for the main request types. DO NOT test all the use case behaviors, it's the responsability of the unit tests. Make sure to follow [the integration tests rules](testing.md#14-endpoint-domain-integration-tests)
