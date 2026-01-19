# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build and Test Commands

```bash
# Build the project
./mvnw clean package

# Run all tests
./mvnw test

# Run tests for a specific module
./mvnw test -pl petstore-service

# Run a single test class
./mvnw test -pl petstore-service -Dtest=PetServiceTest

# Run a single test method
./mvnw test -pl petstore-service -Dtest=PetServiceTest#testFindAllPets

# Run the application (starts on port 8080)
./mvnw spring-boot:run -pl petstore-web
```

## Architecture Overview

This is a Spring Boot 3.5 pet store application using Java 17, JPA/Hibernate with H2 in-memory database.

### Multi-Module Structure

```
petstore-parent/                     (Parent POM)
├── petstore-domain/                 (JPA entities, DTOs, enums)
├── petstore-data/                   (Spring Data JPA repositories)
├── petstore-service/                (Business logic layer)
├── petstore-web/                    (REST API, Spring Boot app)
└── petstore-test-support/           (Shared test infrastructure)
```

Module dependencies flow: `domain → data → service → web`

### Layer Structure

- **petstore-domain**: JPA entities with Lombok annotations, DTOs for API responses
- **petstore-data**: Spring Data JPA interfaces extending `JpaRepository`
- **petstore-service**: Business logic layer with two patterns:
  - Simple services (e.g., `PetService`, `OwnerService`) - CRUD operations with repository delegation
  - Orchestration services (e.g., `PetCareOrchestrationService`) - coordinate multiple services in a single transaction
- **petstore-web**: REST endpoints and Spring Boot application entry point
- **petstore-test-support**: Shared test utilities (query tracking, transaction tracking)

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

The project includes a custom query tracking system for N+1 detection in `petstore-test-support`:

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
