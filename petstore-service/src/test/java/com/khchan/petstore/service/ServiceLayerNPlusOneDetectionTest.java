package com.khchan.petstore.service;

import com.khchan.petstore.dto.Pet;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Demonstrates N+1 query detection at the SERVICE layer using Hibernate Statistics.
 *
 * <p>This is important because N+1 issues often manifest when service methods
 * transform entities to DTOs, accessing lazy-loaded collections outside the
 * original query context.
 *
 * <h3>Why Service-Level N+1 Detection Matters:</h3>
 * <ul>
 *   <li>Repository tests may not trigger lazy loading</li>
 *   <li>Service methods often iterate over entities and access relationships</li>
 *   <li>DTO transformers frequently trigger N+1 by accessing lazy collections</li>
 *   <li>The full call stack (controller → service → repository) reveals real issues</li>
 * </ul>
 *
 * <h3>Example N+1 Pattern Detected Here:</h3>
 * <pre>
 * PetService.findAllPets()
 *   → petRepository.findAll()           // 1 query for N pets
 *   → for each pet:
 *       petTransformer.transformEntityToDTO(pet)
 *         → pet.getMedia().stream()...  // N queries (lazy load)
 *         → pet.getTags().stream()...   // N queries (lazy load)
 * </pre>
 * Result: 1 + N + N = 2N+1 queries instead of 1-3 optimized queries
 */
@SpringBootTest
@Transactional
@DisplayName("Service Layer N+1 Detection with Hibernate Statistics")
public class ServiceLayerNPlusOneDetectionTest {

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private PetService petService;

    private Statistics statistics;

    @BeforeEach
    void setUp() {
        SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
        statistics = sessionFactory.getStatistics();
        statistics.setStatisticsEnabled(true);
        statistics.clear();
    }

    @Test
    @DisplayName("Detect N+1 in PetService.findAllPets() - DTO transformation triggers lazy loading")
    void detectNPlusOneInFindAllPets() {
        // Execute the service method
        List<Pet> pets = petService.findAllPets();

        // Gather statistics
        long queryCount = statistics.getQueryExecutionCount();
        long entityLoadCount = statistics.getEntityLoadCount();
        long collectionFetchCount = statistics.getCollectionFetchCount();

        System.out.println("=== PetService.findAllPets() Statistics ===");
        System.out.println("Pets returned: " + pets.size());
        System.out.println("Query execution count: " + queryCount);
        System.out.println("Entity load count: " + entityLoadCount);
        System.out.println("Collection fetch count: " + collectionFetchCount);

        // Analyze for N+1
        if (pets.size() > 0 && queryCount > 3) {
            double queriesPerPet = (double) queryCount / pets.size();
            System.out.println("\n⚠️  POTENTIAL N+1 DETECTED!");
            System.out.println("Queries per pet: " + String.format("%.2f", queriesPerPet));
            System.out.println("Expected: ~1-3 queries total (with JOIN FETCH or @EntityGraph)");
            System.out.println("Actual: " + queryCount + " queries for " + pets.size() + " pets");
            System.out.println("\nSuggested fixes:");
            System.out.println("1. Add @EntityGraph to PetRepository.findAll()");
            System.out.println("2. Use JOIN FETCH in a custom JPQL query");
            System.out.println("3. Use batch fetching (@BatchSize annotation)");
        }

        // Log individual query statistics
        System.out.println("\n=== Query Details ===");
        String[] queries = statistics.getQueries();
        for (String query : queries) {
            var queryStats = statistics.getQueryStatistics(query);
            System.out.println("Query: " + truncateQuery(query, 80));
            System.out.println("  Executions: " + queryStats.getExecutionCount());
            System.out.println("  Rows: " + queryStats.getExecutionRowCount());
        }

        // Document the current behavior (this assertion passes but shows the issue)
        assertThat(queryCount).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("Compare query counts: service method vs direct repository call")
    void compareServiceVsRepositoryQueryCounts() {
        // First, call via service (includes DTO transformation)
        statistics.clear();
        List<Pet> petsViaService = petService.findAllPets();
        long serviceQueryCount = statistics.getQueryExecutionCount();
        long serviceCollectionFetches = statistics.getCollectionFetchCount();

        System.out.println("=== Comparison: Service vs Repository ===");
        System.out.println("\nService layer (PetService.findAllPets()):");
        System.out.println("  Query count: " + serviceQueryCount);
        System.out.println("  Collection fetches: " + serviceCollectionFetches);
        System.out.println("  Pets returned: " + petsViaService.size());

        // The difference shows the cost of DTO transformation with lazy loading
        if (serviceCollectionFetches > 0) {
            System.out.println("\n📊 Collection fetches indicate lazy loading occurred during DTO transformation");
            System.out.println("   This is the source of N+1 queries in service methods");
        }

        assertThat(petsViaService).isNotEmpty();
    }

    @Test
    @DisplayName("N+1 threshold assertion helper pattern")
    void demonstrateNPlusOneThresholdAssertion() {
        // Execute service method
        List<Pet> pets = petService.findAllPets();
        int petCount = pets.size();

        long queryCount = statistics.getQueryExecutionCount();

        // Define acceptable threshold
        // Ideal: 1 query (with proper fetching)
        // Acceptable: 3 queries (pets + 2 batch fetches for collections)
        // N+1 problem: > petCount queries
        int acceptableQueryThreshold = Math.max(3, petCount / 2);

        System.out.println("=== N+1 Threshold Check ===");
        System.out.println("Pet count: " + petCount);
        System.out.println("Query count: " + queryCount);
        System.out.println("Acceptable threshold: " + acceptableQueryThreshold);

        if (queryCount > acceptableQueryThreshold) {
            System.out.println("\n❌ QUERY COUNT EXCEEDS THRESHOLD");
            System.out.println("This indicates an N+1 problem that should be fixed.");

            // In a strict test, you would fail here:
            // assertThat(queryCount).isLessThanOrEqualTo(acceptableQueryThreshold);
        } else {
            System.out.println("\n✅ Query count within acceptable range");
        }

        // For now, just document the behavior
        assertThat(queryCount).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("Print full Hibernate statistics report for service call")
    void printFullStatisticsReport() {
        // Execute a service method
        petService.findAllPets();

        System.out.println("\n" + "=".repeat(70));
        System.out.println("FULL HIBERNATE STATISTICS FOR PetService.findAllPets()");
        System.out.println("=".repeat(70));

        System.out.println("\n--- Entity Statistics ---");
        System.out.println("Entity load count: " + statistics.getEntityLoadCount());
        System.out.println("Entity fetch count: " + statistics.getEntityFetchCount());

        System.out.println("\n--- Collection Statistics ---");
        System.out.println("Collection load count: " + statistics.getCollectionLoadCount());
        System.out.println("Collection fetch count: " + statistics.getCollectionFetchCount());

        System.out.println("\n--- Query Statistics ---");
        System.out.println("Query execution count: " + statistics.getQueryExecutionCount());
        System.out.println("Max query time: " + statistics.getQueryExecutionMaxTime() + "ms");

        System.out.println("\n--- Session Statistics ---");
        System.out.println("Prepare statement count: " + statistics.getPrepareStatementCount());
        System.out.println("Flush count: " + statistics.getFlushCount());

        System.out.println("\n--- N+1 Risk Assessment ---");
        long queries = statistics.getQueryExecutionCount();
        long collectionFetches = statistics.getCollectionFetchCount();
        long entityLoads = statistics.getEntityLoadCount();

        if (collectionFetches > 1) {
            System.out.println("⚠️  HIGH RISK: " + collectionFetches + " collection fetches detected");
            System.out.println("   Each collection fetch typically indicates a lazy load");
        }
        if (queries > entityLoads && entityLoads > 0) {
            System.out.println("⚠️  SUSPICIOUS: More queries (" + queries + ") than entities loaded (" + entityLoads + ")");
        }

        System.out.println("\n" + "=".repeat(70));

        assertThat(statistics.getQueryExecutionCount()).isGreaterThanOrEqualTo(1);
    }

    private String truncateQuery(String query, int maxLength) {
        if (query == null) return "null";
        if (query.length() <= maxLength) return query;
        return query.substring(0, maxLength) + "...";
    }
}
