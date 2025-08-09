# Nullability Check
## Stubs
## Troubleshooting
### nullness:return
```java
    // ❌ the method can return "null" by its implicitly annotated as not null
    public static String toStringOrNull(@Nullable Object valueObject) {
        return valueObject != null ? valueObject.toString() : null;
    }
```
```diff
    // ✅ fix by adding "@Nullable" to the return type
-   public static String toStringOrNull(@Nullable Object valueObject) {
+   public static @Nullable String toStringOrNull(@Nullable Object valueObject) {
        return valueObject != null ? valueObject.toString() : null;
    }
```

### nullness:nulltest.redundant
```java
    public static Language fromString(String language) {
        // ❌ we test language for null, but it's impllicity not null
        if (language == null) {
            return null;
        }
        ...
    }
```
```diff
    public static Language fromString(String language) {
        // ✅ fix 1: remove the check if the passed "language" is always non null
-       if (language == null) {
-           return null;
-       }
        ...
    }
```
```diff
-   public static Language fromString(String language) {
+   public static Language fromString(@Nullable String language) {
        // ✅ fix 2: add "@Nullable" if "language" can be null
        if (language == null) {
            return null;
        }
        ...
    }
```

### nullness:initialization.fields.uninitialized
```java
public class CustomersDbEntity {
    Collection<NewCardRequestDbEntity> newCardRequests;

    // ❌ the constructor does not initialize fields: newCardRequests
    //    which is implicitly non null
    public CustomersDbEntity(Customer customer) {
        ...
    }
}
```
```diff
public class CustomersDbEntity {
    Collection<NewCardRequestDbEntity> newCardRequests;

    public CustomersDbEntity(Customer customer) {
        // ✅ fix: initialize the field in the constructor or its definition.
+       this.newCardRequests = new ArrayList<>();
    }
}
```

### nullness:assignment
```java
public class CardDbEntity {
    ...
    @Column(name="opposition_date")
    private LocalDateTime oppositionDate;
    ...
    public CardDbEntity(@NotNull Card card) {
        // ❌ incompatible types in assignment (nullable to non nullable)
        this.oppositionDate = card.oppositionDate(); // this value is marked @Nullable
    }
}
```
```diff
public class CardDbEntity {
    ...
    @Column(name="opposition_date")
    // ✅ fix: if the entity's column can be null, marked it as @Nullable
+   @Nullable
    private LocalDateTime oppositionDate;
    ...
}
```
### nullness:argument
```java
    ArrayList<String> list = new ArrayList<>();
    // ❌ vehicleBrandId() is nullable, but the list contains non nullable by default
    list.add(customer.vehicleBrandId());
```
```diff
    // ✅ fix: mark the list element as nullable (if this is the required specs)
-   ArrayList<String> list = new ArrayList<>();
+   ArrayList<@Nullable String> list = new ArrayList<>();
    list.add(customer.vehicleBrandId());
```

### nullness and wildcard generics
```java
// ❌ data can be of a nullable type
public void process(Stream<?> data) {
    data.forEach(item -> {
        System.out.println(item.toString());
    });
}
```
```diff
// ✅ fix: as you cannot put annotations on "?", use the extends Object workaround
-public void process(Stream<?> data) {
+public void process(Stream<? extends @NotNull Object> data) {
    data.forEach(item -> {
        System.out.println(item.toString());
    });
}
```

### nullness:dereference.of.nullable
```java
    if (customer.card() != null && customer.card().isOpposed()) {
        cardReactivationDeadline = customer
                .card()
                .oppositionDate()
                // ❌ possible NPE because oppositionDate is nullable
                .plusDays(reactivateCardDeadlineParameter
                        .getReactivateCardDeadline()
                        .days());
    }
    ...
public Boolean isOpposed() {
    var hasStatusOpposed = status == CardStatus.OPPOSED;
    var hasOppositionDate = oppositionDate != null;
    if (hasStatusOpposed != hasOppositionDate) {
        throw new IllegalStateException("Invalid card state: opposition status and date mismatch");
    }
    return hasStatusOpposed;
}
```
```diff
    // ✅ fix: declare that if isOpposed() returns true, then oppositionDate() is non null
    // see https://checkerframework.org/manual/#type-refinement-purity
+   @EnsuresNonNullIf(expression = "oppositionDate()", result=true)
    public Boolean isOpposed() {
        ...
    }
```
---
```java
return new GetCurrentCustomerResponse(
        ...
        // ❌ customer is declared nullable but it's guarenteed to be non null when reaching this line
        current.customer().name(),
        ...
)
```
```diff
return new GetCurrentCustomerResponse(
        ...
        // ✅ fix: use 'castNonNull' to suppress the warning.
        //    THIS IS A LAST RESORT SOLUTION. Please try hard to find a cleaner way or andon.
-       current.customer().name(),
+       castNonNull(current.customer()).name(),
        ...
)
```
### nullness:dereference.of.nullable and lambdas
```java
    if (request.oldCard() != null) {
        var oldCard = cardRepository
                .findById(request.oldCard().number().value())
                .orElseThrow(() -> new CardNotFoundException(
                    // ❌ request.oldCard() might return null at the time the lambda is executed.
                    //    This occurs because the lambda is deferred and could be invoked after request is reassigned,
                    //    making oldCard null in the new object. This breaks the assumption that request.oldCard() 
                    //    is non-null during lambda execution.
                    request.oldCard().number()));
        ...
    }
```
```diff
    if (request.oldCard() != null) {
        // ✅ oldCard is assigned to a *constant* local variable ...
+       final Card oldCardFromRequest = request.oldCard();
        var oldCard = cardRepository
                .findById(request.oldCard().number().value())
                .orElseThrow(() -> new CardNotFoundException(
                    // ✅ ... and used inside the lambda
-                   request.oldCard().number()));
+                   oldCardFromRequest.number()));
        ...
    }
```

### validation
```java
    private void validateCustomerCard(Customer customer) {
        if (customer.card() == null) {
            throw new CardNotAttributedException(customer);
        }
    }
    public void handle() {
        validateCustomerCard(customer);
        // ❌ the previous method guarentees that customer.card() is not null
        //    but CheckFramework cannot deduct it
        replaceCard(customer.card(), command.cardNumber());
    }
```
```diff
    // ✅ Use "@EnsuresNonNull" to hint CheckFramework that card() is non null
    // https://checkerframework.org/api/org/checkerframework/checker/nullness/qual/EnsuresNonNull.html
+   @EnsuresNonNull("#1.card()")
    private void validateCustomerCard(Customer customer) {
        if (customer.card() == null) {
            throw new CardNotAttributedException(customer);
        }
    }
```

### nullness:override.param
```java
public class PageableRequest {
    @Override
    // ❌ "equals" from the superclass "Object" is supposed to accept a nullable argument
    //    so any class overriding this method should respect this contract.
    public boolean equals(Object obj) {
        ...
    }
}
```
```diff
public class PageableRequest {
    @Override
    // ✅ fix: add nullable annotation
-   public boolean equals(Object obj) {
+   public boolean equals(@Nullable Object obj) {
        ...
    }
}
```

### nullness:type.argument: incompatible type argument for type parameter E of List.
```java
    // type of 'data' is 'List<GetAllCardsResult>'
    // ❌ error: incompatible type argument for type parameter E of List
    //    I don't understand why. Probably a bug in CheckerFramework.
    var data = cardsPage.data().stream().map(card -> {
        CashbackBalance balance = null;
        return new GetAllCardsResult(
                card,
                balance
        );
    }).toList();
```
```diff
    // ✅ extract the lambda in a method
+   var data = cardsPage.data().stream().map(GetAllCardsUseCase::getGetAllCardsResult)
-   var data = cardsPage.data().stream().map(card -> {
-       CashbackBalance balance = null;
-       return new GetAllCardsResult(
-               card,
-               balance
-       );
    }).toList();
    ...
+private static GetAllCardsResult getGetAllCardsResult(Card card) {
+    CashbackBalance balance = null;
+    return new GetAllCardsResult(
+            card,
+            balance
+    );
+}
```

### The Checker Framework crashed
You may get crash errors, hopefully not many. Here are some examples and how they were solved. Andon if needed.

#### MethodReferenceExpr is not a supported expression
```java
// ❌ the error
// java: AsSuperVisitor: type is not an erased subtype of supertype.
//  type: @KeyFor("[error for expression: ma.winxo.fidelite.features.cashback.domain.utils.TransactionUtils.getGains(accTransactions).stream().collect(java.util.stream.Collectors.groupingBy(Gain::productKey)); error: Invalid 'Gain::productKey' because class org.checkerframework.com.github.javaparser.ast.expr.MethodReferenceExpr is not a supported expression]") Object
//  superType: ProductKey
//  ; The Checker Framework crashed.  Please report the crash.  Version: Checker Framework 3.48.3. 

// the code
List<GainsSummary> gainSummaries = TransactionUtils
        .getGains(accTransactions).stream()
        .collect(Collectors.groupingBy(Gain::productKey))
        .entrySet().stream()
        ...
```
```diff
List<GainsSummary> gainSummaries = TransactionUtils
        .getGains(accTransactions).stream()
        // ✅ use lambda instead of method reference
-       .collect(Collectors.groupingBy(Gain::productKey))
+       .collect(Collectors.groupingBy(gain -> gain.productKey()))
        .entrySet().stream()
```