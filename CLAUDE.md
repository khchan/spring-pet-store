# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build and Test Commands

```bash
# Build the project
./mvnw clean package

# Run all tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=PetServiceTest

# Run a single test method
./mvnw test -Dtest=PetServiceTest#testFindAllPets

# Run the application (starts on port 8080)
./mvnw spring-boot:run
```

## Architecture Overview

This is a Spring Boot 3.5 pet store application using Java 17, JPA/Hibernate with H2 in-memory database.

### Layer Structure

- **Controllers** (`controller/`): REST endpoints. Currently only `PetController` exposing `/pets` and `/pet/{id}` endpoints.
- **Services** (`service/`): Business logic layer with two patterns:
  - Simple services (e.g., `PetService`, `OwnerService`) - CRUD operations with repository delegation
  - Orchestration services (e.g., `PetCareOrchestrationService`) - coordinate multiple services in a single transaction
- **Repositories** (`repository/`): Spring Data JPA interfaces extending `JpaRepository`
- **Domain** (`domain/`): JPA entities with Lombok annotations
- **DTOs** (`dto/`): Data transfer objects for API responses (e.g., `Pet`, `Tag`)
- **Transformers** (`service/PetTransformer`): Convert between entities and DTOs

### Domain Model

The `PetEntity` is the central entity with relationships to:
- `Owner` (ManyToOne, no cascade from pet side)
- `Breed` (ManyToOne, reference entity, no cascade)
- `Category` (ManyToOne)
- `PetInsurance` (OneToOne, CASCADE.ALL + orphanRemoval)
- `MedicalRecord`, `Vaccination` (OneToMany, CASCADE.ALL + orphanRemoval)
- `Appointment` (OneToMany, CASCADE.PERSIST/MERGE only)
- `TagEntity` (ManyToMany, CASCADE.MERGE)

All relationships use `FetchType.LAZY`. Entity classes use Lombok `@Data`, `@Builder`, and helper methods for bidirectional relationship management.

### Test Infrastructure

The project includes a custom query tracking system for N+1 detection:

- **`JpaQueryTrackingRule`**: JUnit 5 extension for tracking SQL queries and transactions
- **`QueryCounter`**: Counts SELECT/INSERT/UPDATE/DELETE queries
- **`TransactionTracker`**: Tracks commit/rollback events
- **`DataSourceProxyConfig`**: Test configuration that wraps the datasource for query interception

Usage in tests:
```java
@SpringBootTest
@Import(DataSourceProxyConfig.class)
class MyTest {
    @RegisterExtension
    JpaQueryTrackingRule queryTracking = new JpaQueryTrackingRule();

    @Test
    void test() {
        queryTracking.reset();
        // ... execute code ...
        queryTracking.assertSelectCount(2);
        queryTracking.assertNoNPlusOne(3);
    }
}
```

### Transaction Patterns

Services use `@Transactional` for transaction management. `PetCareOrchestrationService` demonstrates nested transactions using `TransactionTemplate` with `PROPAGATION_REQUIRES_NEW`.
