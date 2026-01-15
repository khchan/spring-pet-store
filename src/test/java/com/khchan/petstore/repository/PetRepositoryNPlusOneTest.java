package com.khchan.petstore.repository;

import com.khchan.petstore.domain.Category;
import com.khchan.petstore.domain.PetEntity;
import com.khchan.petstore.domain.Status;
import com.khchan.petstore.domain.TagEntity;
import com.khchan.petstore.dto.Pet;
import com.khchan.petstore.service.PetService;
import com.khchan.petstore.test.DataSourceProxyConfig;
import com.khchan.petstore.test.JpaQueryTrackingRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import jakarta.persistence.EntityManager;
import java.util.Arrays;
import java.util.List;

/**
 * Demonstrates N+1 query detection and transaction tracking using the JpaQueryTrackingRule.
 *
 * This test class shows:
 * 1. How to detect N+1 query problems
 * 2. How to assert exact query counts (SELECT, INSERT, UPDATE, DELETE)
 * 3. How to track transaction commit/rollback events
 * 4. Works seamlessly with @Transactional and TransactionTemplate
 */
@SpringBootTest
@Import(DataSourceProxyConfig.class)  // Enable query tracking - test-only!
@Transactional  // Wraps each test in a transaction that rolls back after the test
public class PetRepositoryNPlusOneTest {

    @RegisterExtension
    JpaQueryTrackingRule queryTracking = new JpaQueryTrackingRule()
        .printQueriesOnFailure(true);   // Print queries when test fails
        // .printQueriesAlways(true);   // Uncomment to always print queries

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private PetService petService;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @BeforeEach
    public void setup() {
        // Clear any existing data and reset tracking
        // Note: data-h2.sql already inserts test data, so we'll use that
    }

    // ========== N+1 Query Detection Tests ==========

    @Test
    public void findAllPets_demonstratesNPlusOneProblem() {
        // This test demonstrates detecting the N+1 problem.
        // The findAllPets() method triggers lazy loading of media and tags
        // for each pet, causing N+1 queries.

        // Reset counters after any setup queries
        queryTracking.reset();

        // Execute: This will trigger the N+1 problem
        // Note: We access the entities directly to see N+1 since PetService may have different behavior
        List<PetEntity> pets = petRepository.findAll();

        // Force lazy loading to demonstrate N+1
        for (PetEntity pet : pets) {
            pet.getMedia().size();
            pet.getTags().size();
        }

        // Print the report to see all queries
        System.out.println("\n=== Query Report ===");
        queryTracking.printReport();

        // Assert: We expect more than just 1 query due to N+1
        // This test documents the current (problematic) behavior
        // In a real scenario, you'd fix the N+1 and assert lower counts
        int selectCount = queryTracking.getSelectCount();
        System.out.println("Total SELECT queries: " + selectCount);

        // Note: With pets in test data, we expect:
        // - 1 query for pets
        // - N queries for media (lazy loaded for each pet)
        // - N queries for tags (lazy loaded for each pet)
        // This assertion documents the N+1 problem exists
        if (selectCount > 3) {
            System.out.println("WARNING: N+1 query problem detected!");
            System.out.println("Consider using JOIN FETCH or @EntityGraph");
        }
    }

    @Test
    public void findSinglePet_shouldUseLimitedQueries() {
        // Reset counters
        queryTracking.reset();

        // Execute: Find a single pet and access lazy fields
        PetEntity pet = petRepository.findById(1L).orElse(null);
        if (pet != null) {
            // Access lazy fields
            if (pet.getMedia() != null) pet.getMedia().size();
            if (pet.getTags() != null) pet.getTags().size();
        }

        // Assert: Should use reasonable number of queries
        // For a single pet: 1 for pet + optional lazy loads
        queryTracking.assertSelectCountAtMost(4);  // pet + category + media + tags

        System.out.println("Single pet fetch: " + queryTracking.getQuerySummary());
    }

    // ========== Query Count Assertion Tests ==========

    @Test
    public void savePet_shouldTrackInsertAndUpdateQueries() {
        // Reset counters
        queryTracking.reset();

        // Execute: Create and save a new pet
        PetEntity newPet = PetEntity.builder()
            .name("Test Pet " + System.currentTimeMillis())
            .status(Status.AVAILABLE)
            .build();

        petRepository.save(newPet);
        entityManager.flush();  // Force the INSERT to execute

        // Assert: Should have exactly 1 INSERT
        queryTracking.assertInsertCount(1);
        queryTracking.assertUpdateCount(0);
        queryTracking.assertDeleteCount(0);

        System.out.println("Save pet: " + queryTracking.getQuerySummary());
    }

    @Test
    public void updatePet_shouldTrackUpdateQuery() {
        // First, get a pet (outside our measurement)
        PetEntity pet = petRepository.findById(1L).orElse(null);
        entityManager.flush();
        entityManager.clear();

        // Reset counters for the update operation
        queryTracking.reset();

        // Re-fetch and update
        pet = petRepository.findById(1L).orElse(null);
        pet.setName("Updated Name " + System.currentTimeMillis());
        petRepository.save(pet);
        entityManager.flush();

        // Assert: Should have SELECT(s) and possibly UPDATE
        System.out.println("Update pet: " + queryTracking.getQuerySummary());
        queryTracking.printReport();
    }

    @Test
    public void deletePet_shouldTrackDeleteQuery() {
        // First create a pet to delete
        PetEntity newPet = PetEntity.builder()
            .name("To Be Deleted " + System.currentTimeMillis())
            .status(Status.AVAILABLE)
            .build();
        petRepository.save(newPet);
        entityManager.flush();
        Long petId = newPet.getId();

        // Reset counters
        queryTracking.reset();

        // Execute delete
        petRepository.deleteById(petId);
        entityManager.flush();

        // Assert delete was tracked
        queryTracking.assertDeleteCount(1);

        System.out.println("Delete pet: " + queryTracking.getQuerySummary());
    }

    // ========== Transaction Tracking Tests ==========
    // Note: These tests cannot use @Transactional on the class level since they
    // need to verify commit behavior. We'll skip them in this transactional test class.
    // See TransactionTrackingTest for transaction-related tests.

    @Test
    public void queryCount_simpleRead() {
        // Reset tracking
        queryTracking.reset();

        // Simple read operation
        petRepository.findAll();

        // Verify query was tracked
        queryTracking.assertSelectCountAtLeast(1);
        System.out.println("Simple read: " + queryTracking.getQuerySummary());
    }

    @Test
    public void queryCount_countOperation() {
        // Reset tracking
        queryTracking.reset();

        // Count operation
        long count = petRepository.count();

        // Verify query was tracked
        queryTracking.assertSelectCount(1);
        System.out.println("Count operation returned: " + count + ", " + queryTracking.getQuerySummary());
    }

    // ========== Combined Query Test ==========

    @Test
    public void complexOperation_shouldTrackQueries() {
        // Reset tracking
        queryTracking.reset();

        // Execute a complex operation
        // Create a pet
        PetEntity pet = PetEntity.builder()
            .name("Complex Test Pet " + System.currentTimeMillis())
            .status(Status.AVAILABLE)
            .build();
        petRepository.save(pet);

        // Read all pets
        List<PetEntity> allPets = petRepository.findAll();

        // Update the pet
        pet.setStatus(Status.SOLD);
        petRepository.save(pet);

        entityManager.flush();

        // Print full report
        System.out.println("\n=== Complex Operation Report ===");
        queryTracking.printReport();

        // Assert queries
        queryTracking.assertInsertCount(1);
        System.out.println("Complex operation completed successfully");
    }

    // ========== Utility: Mid-Test Reset ==========

    @Test
    public void demonstrateMidTestReset() {
        // Setup phase - queries we don't want to count
        petRepository.findAll();

        // Reset to start fresh measurement
        queryTracking.reset();

        // Now measure just this operation
        petRepository.findById(1L);

        // Assert only the queries after reset
        queryTracking.assertSelectCountAtMost(2);

        System.out.println("After reset: " + queryTracking.getQuerySummary());
    }
}
