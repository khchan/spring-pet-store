package com.khchan.petstore.repository;

import com.khchan.petstore.domain.PetEntity;
import com.khchan.petstore.domain.Status;
import jakarta.persistence.EntityManager;
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
 * Demonstrates Hibernate Session Statistics for query tracking.
 *
 * <p>This test class shows an alternative approach to query tracking using
 * Hibernate's built-in statistics instead of a DataSource proxy. This is useful when:
 * <ul>
 *   <li>You want entity-level metrics (loads, inserts, updates, deletes)</li>
 *   <li>You need cache hit/miss statistics</li>
 *   <li>You want query timing/performance data</li>
 *   <li>You don't want to add a DataSource proxy</li>
 * </ul>
 *
 * <h3>Comparison with DataSource Proxy Approach:</h3>
 * <table>
 *   <tr><th>Feature</th><th>Hibernate Statistics</th><th>DataSource Proxy</th></tr>
 *   <tr><td>Entity load/insert/update/delete counts</td><td>Yes</td><td>No</td></tr>
 *   <tr><td>Collection fetch counts</td><td>Yes</td><td>No</td></tr>
 *   <tr><td>Cache hit/miss statistics</td><td>Yes</td><td>No</td></tr>
 *   <tr><td>Query timing</td><td>Yes</td><td>No</td></tr>
 *   <tr><td>Actual SQL query text</td><td>Limited</td><td>Yes</td></tr>
 *   <tr><td>Native query tracking</td><td>Limited</td><td>Yes</td></tr>
 *   <tr><td>Works with non-Hibernate JPA</td><td>No</td><td>Yes</td></tr>
 * </table>
 *
 * <h3>Configuration:</h3>
 * <pre>
 * # application.properties
 * spring.jpa.properties.hibernate.generate_statistics=true
 * </pre>
 *
 * @see com.khchan.petstore.test.JpaQueryTrackingRule for the DataSource proxy approach
 */
@SpringBootTest
@Transactional
@DisplayName("Hibernate Session Statistics Examples")
public class HibernateSessionStatisticsExampleTest {

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private BreedRepository breedRepository;

    @Autowired
    private OwnerRepository ownerRepository;

    private Statistics statistics;

    @BeforeEach
    void setUp() {
        SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
        statistics = sessionFactory.getStatistics();
        statistics.setStatisticsEnabled(true);
        statistics.clear();
    }

    // ========== Basic Query Statistics ==========

    @Test
    @DisplayName("Basic query execution count tracking")
    void demonstrateBasicQueryStatistics() {
        // Execute a query
        petRepository.findAll();

        // Verify query was executed
        long queryCount = statistics.getQueryExecutionCount();
        System.out.println("Query execution count: " + queryCount);

        assertThat(queryCount).isGreaterThanOrEqualTo(1);
    }

    // ========== Entity Load Statistics ==========

    @Test
    @DisplayName("Entity load count tracking")
    void demonstrateEntityLoadStatistics() {
        // Load pets from repository
        List<PetEntity> pets = petRepository.findAll();

        // Get entity load statistics
        long entityLoadCount = statistics.getEntityLoadCount();
        long petLoadCount = statistics.getEntityStatistics(PetEntity.class.getName()).getLoadCount();

        System.out.println("Total entity load count: " + entityLoadCount);
        System.out.println("PetEntity load count: " + petLoadCount);

        assertThat(petLoadCount).isEqualTo(pets.size());
    }

    // ========== Entity Insert Statistics ==========

    @Test
    @DisplayName("Entity insert count tracking")
    void demonstrateEntityInsertStatistics() {
        // Create and save a new pet
        PetEntity pet = PetEntity.builder()
            .name("Statistics Test Pet")
            .status(Status.AVAILABLE)
            .build();
        petRepository.save(pet);
        entityManager.flush();

        // Verify insert was tracked
        long insertCount = statistics.getEntityInsertCount();
        long petInsertCount = statistics.getEntityStatistics(PetEntity.class.getName()).getInsertCount();

        System.out.println("Total entity insert count: " + insertCount);
        System.out.println("PetEntity insert count: " + petInsertCount);

        assertThat(petInsertCount).isEqualTo(1);
    }

    // ========== Entity Update Statistics ==========

    @Test
    @DisplayName("Entity update count tracking")
    void demonstrateEntityUpdateStatistics() {
        // Create and save a pet
        PetEntity pet = PetEntity.builder()
            .name("Update Test Pet")
            .status(Status.AVAILABLE)
            .build();
        petRepository.save(pet);
        entityManager.flush();

        // Clear statistics to isolate the update
        statistics.clear();

        // Update the pet
        pet.setName("Updated Pet Name");
        petRepository.save(pet);
        entityManager.flush();

        // Verify update was tracked
        long updateCount = statistics.getEntityUpdateCount();
        long petUpdateCount = statistics.getEntityStatistics(PetEntity.class.getName()).getUpdateCount();

        System.out.println("Total entity update count: " + updateCount);
        System.out.println("PetEntity update count: " + petUpdateCount);

        assertThat(petUpdateCount).isEqualTo(1);
    }

    // ========== Entity Delete Statistics ==========

    @Test
    @DisplayName("Entity delete count tracking")
    void demonstrateEntityDeleteStatistics() {
        // Create and save a pet
        PetEntity pet = PetEntity.builder()
            .name("Delete Test Pet")
            .status(Status.AVAILABLE)
            .build();
        petRepository.save(pet);
        entityManager.flush();
        Long petId = pet.getId();

        // Clear statistics to isolate the delete
        statistics.clear();

        // Delete the pet
        petRepository.deleteById(petId);
        entityManager.flush();

        // Verify delete was tracked
        long deleteCount = statistics.getEntityDeleteCount();
        long petDeleteCount = statistics.getEntityStatistics(PetEntity.class.getName()).getDeleteCount();

        System.out.println("Total entity delete count: " + deleteCount);
        System.out.println("PetEntity delete count: " + petDeleteCount);

        assertThat(petDeleteCount).isEqualTo(1);
    }

    // ========== N+1 Detection via Collection Fetch Count ==========

    @Test
    @DisplayName("Detect N+1 problems via collection fetch count")
    void detectNPlusOneWithHibernateStatistics() {
        // Load all pets and trigger lazy loading of collections
        List<PetEntity> pets = petRepository.findAll();
        for (PetEntity pet : pets) {
            // Force lazy loading of tags collection
            if (pet.getTags() != null) {
                pet.getTags().size();
            }
        }

        // Check collection fetch statistics
        long collectionFetchCount = statistics.getCollectionFetchCount();
        long queryCount = statistics.getQueryExecutionCount();

        System.out.println("Collection fetch count: " + collectionFetchCount);
        System.out.println("Query execution count: " + queryCount);

        // A high collection fetch count relative to entity count indicates N+1
        if (collectionFetchCount > 1 && pets.size() > 0) {
            double fetchRatio = (double) collectionFetchCount / pets.size();
            System.out.println("WARNING: Collection fetch ratio: " + fetchRatio);
            System.out.println("Consider using JOIN FETCH or @EntityGraph to reduce lazy loading");
        }

        // Document current behavior (may indicate N+1)
        assertThat(collectionFetchCount).isGreaterThanOrEqualTo(0);
    }

    // ========== Cache Statistics ==========

    @Test
    @DisplayName("Second-level cache hit/miss statistics (demonstration)")
    void demonstrateCacheStatistics() {
        // Note: This requires second-level cache to be configured
        // For demonstration purposes, we show how to access these metrics

        long cacheHitCount = statistics.getSecondLevelCacheHitCount();
        long cacheMissCount = statistics.getSecondLevelCacheMissCount();
        long cachePutCount = statistics.getSecondLevelCachePutCount();

        System.out.println("=== Second-Level Cache Statistics ===");
        System.out.println("Cache hit count: " + cacheHitCount);
        System.out.println("Cache miss count: " + cacheMissCount);
        System.out.println("Cache put count: " + cachePutCount);

        // Query cache statistics
        long queryCacheHitCount = statistics.getQueryCacheHitCount();
        long queryCacheMissCount = statistics.getQueryCacheMissCount();
        long queryCachePutCount = statistics.getQueryCachePutCount();

        System.out.println("\n=== Query Cache Statistics ===");
        System.out.println("Query cache hit count: " + queryCacheHitCount);
        System.out.println("Query cache miss count: " + queryCacheMissCount);
        System.out.println("Query cache put count: " + queryCachePutCount);

        // These assertions verify the statistics API is accessible
        // Actual values depend on cache configuration
        assertThat(cacheHitCount).isGreaterThanOrEqualTo(0);
        assertThat(cacheMissCount).isGreaterThanOrEqualTo(0);
    }

    // ========== Session Statistics ==========

    @Test
    @DisplayName("Session open/close, flush, and connect statistics")
    void demonstrateSessionStatistics() {
        // Perform some operations
        PetEntity pet = PetEntity.builder()
            .name("Session Stats Pet")
            .status(Status.AVAILABLE)
            .build();
        petRepository.save(pet);
        entityManager.flush();

        petRepository.findAll();

        // Access session statistics
        long sessionOpenCount = statistics.getSessionOpenCount();
        long sessionCloseCount = statistics.getSessionCloseCount();
        long flushCount = statistics.getFlushCount();
        long connectCount = statistics.getConnectCount();
        long prepareStatementCount = statistics.getPrepareStatementCount();
        long closeStatementCount = statistics.getCloseStatementCount();

        System.out.println("=== Session Statistics ===");
        System.out.println("Session open count: " + sessionOpenCount);
        System.out.println("Session close count: " + sessionCloseCount);
        System.out.println("Flush count: " + flushCount);
        System.out.println("Connect count: " + connectCount);
        System.out.println("Prepare statement count: " + prepareStatementCount);
        System.out.println("Close statement count: " + closeStatementCount);

        assertThat(flushCount).isGreaterThanOrEqualTo(1);
        assertThat(prepareStatementCount).isGreaterThanOrEqualTo(1);
    }

    // ========== Slowest Queries Analysis ==========

    @Test
    @DisplayName("Query timing and slowest query analysis")
    void demonstrateSlowestQueries() {
        // Execute various queries to generate timing data
        petRepository.findAll();
        petRepository.count();
        breedRepository.findAll();
        ownerRepository.findAll();

        // Access query timing statistics
        long queryExecutionMaxTime = statistics.getQueryExecutionMaxTime();
        String slowestQuery = statistics.getQueryExecutionMaxTimeQueryString();

        System.out.println("=== Query Timing Statistics ===");
        System.out.println("Max query execution time: " + queryExecutionMaxTime + "ms");
        System.out.println("Slowest query: " + slowestQuery);

        // Log all query statistics
        String[] queries = statistics.getQueries();
        System.out.println("\n=== All Tracked Queries ===");
        for (String query : queries) {
            var queryStats = statistics.getQueryStatistics(query);
            System.out.println("Query: " + query);
            System.out.println("  Execution count: " + queryStats.getExecutionCount());
            System.out.println("  Total time: " + queryStats.getExecutionTotalTime() + "ms");
            System.out.println("  Average time: " + queryStats.getExecutionAvgTime() + "ms");
            System.out.println("  Max time: " + queryStats.getExecutionMaxTime() + "ms");
            System.out.println("  Rows fetched: " + queryStats.getExecutionRowCount());
        }

        assertThat(queryExecutionMaxTime).isGreaterThanOrEqualTo(0);
    }

    // ========== Combined Statistics Report ==========

    @Test
    @DisplayName("Print comprehensive statistics report")
    void demonstrateFullStatisticsReport() {
        // Execute a mix of operations
        PetEntity pet = PetEntity.builder()
            .name("Report Test Pet")
            .status(Status.AVAILABLE)
            .build();
        petRepository.save(pet);
        entityManager.flush();

        List<PetEntity> pets = petRepository.findAll();
        for (PetEntity p : pets) {
            if (p.getTags() != null) {
                p.getTags().size();
            }
        }

        pet.setStatus(Status.SOLD);
        petRepository.save(pet);
        entityManager.flush();

        // Print the comprehensive report
        System.out.println("\n" + "=".repeat(60));
        System.out.println("HIBERNATE STATISTICS REPORT");
        System.out.println("=".repeat(60));

        System.out.println("\n--- Entity Operations ---");
        System.out.println("Entity load count: " + statistics.getEntityLoadCount());
        System.out.println("Entity insert count: " + statistics.getEntityInsertCount());
        System.out.println("Entity update count: " + statistics.getEntityUpdateCount());
        System.out.println("Entity delete count: " + statistics.getEntityDeleteCount());
        System.out.println("Entity fetch count: " + statistics.getEntityFetchCount());

        System.out.println("\n--- Collection Operations ---");
        System.out.println("Collection load count: " + statistics.getCollectionLoadCount());
        System.out.println("Collection fetch count: " + statistics.getCollectionFetchCount());
        System.out.println("Collection update count: " + statistics.getCollectionUpdateCount());
        System.out.println("Collection remove count: " + statistics.getCollectionRemoveCount());
        System.out.println("Collection recreate count: " + statistics.getCollectionRecreateCount());

        System.out.println("\n--- Query Execution ---");
        System.out.println("Query execution count: " + statistics.getQueryExecutionCount());
        System.out.println("Max query execution time: " + statistics.getQueryExecutionMaxTime() + "ms");

        System.out.println("\n--- Session ---");
        System.out.println("Flush count: " + statistics.getFlushCount());
        System.out.println("Connect count: " + statistics.getConnectCount());
        System.out.println("Prepare statement count: " + statistics.getPrepareStatementCount());

        System.out.println("\n--- Transactions ---");
        System.out.println("Successful transaction count: " + statistics.getSuccessfulTransactionCount());
        System.out.println("Transaction count: " + statistics.getTransactionCount());

        System.out.println("\n" + "=".repeat(60));

        // Assertions to verify test ran successfully
        assertThat(statistics.getEntityInsertCount()).isGreaterThanOrEqualTo(1);
        assertThat(statistics.getEntityUpdateCount()).isGreaterThanOrEqualTo(1);
        assertThat(statistics.getQueryExecutionCount()).isGreaterThanOrEqualTo(1);
    }
}
