package com.khchan.petstore.service;

import com.khchan.petstore.domain.Category;
import com.khchan.petstore.domain.PetEntity;
import com.khchan.petstore.domain.Status;
import com.khchan.petstore.repository.PetRepository;
import com.khchan.petstore.test.DataSourceProxyConfig;
import com.khchan.petstore.test.JpaQueryTrackingRule;
import com.khchan.petstore.test.TransactionTracker;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;
import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Comprehensive tests demonstrating transaction tracking with various patterns.
 * Shows how to verify transaction commits, rollbacks, and nested transaction behavior.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Import(DataSourceProxyConfig.class)
public class TransactionTrackingTest {

    @Rule
    public JpaQueryTrackingRule tracking = new JpaQueryTrackingRule()
            .printQueriesOnFailure(true);

    @Autowired
    private OrderService orderService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private PlatformTransactionManager txManager;

    @Autowired
    private EntityManager entityManager;

    private Long testPetId;
    private Long categoryId;

    @Before
    public void setup() {
        // Create test data in its own transaction
        TransactionTemplate tx = new TransactionTemplate(txManager);
        testPetId = tx.execute(status -> {
            Category cat = new Category();
            cat.setName("Test Category");
            entityManager.persist(cat);
            categoryId = cat.getId();

            PetEntity pet = PetEntity.builder()
                    .name("Test Pet")
                    .status(Status.AVAILABLE)
                    .category(cat)
                    .build();
            return petRepository.save(pet).getId();
        });

        // Reset tracking after setup
        tracking.reset();
    }

    // ==================== @Transactional Tests ====================

    @Test
    public void purchasePet_singleTransaction_shouldCommitOnce() {
        // Execute
        orderService.purchasePet(testPetId);

        // Verify: Single transaction committed
        tracking.assertCommitCount(1);
        tracking.assertRollbackCount(0);

        // Verify queries: SELECT + UPDATE
        tracking.assertSelectCountAtLeast(1);
        System.out.println("Purchase transaction: " + tracking.getTransactionSummary());
    }

    @Test
    public void purchasePetWithValidation_onException_shouldRollback() {
        try {
            orderService.purchasePetWithValidation(testPetId, true);
            fail("Should have thrown exception");
        } catch (RuntimeException e) {
            // Expected
        }

        // Verify: Transaction rolled back
        tracking.assertTransactionRolledBack();
        tracking.assertCommitCount(0);

        System.out.println("Rollback transaction: " + tracking.getTransactionSummary());
        tracking.printReport();
    }

    @Test
    public void purchaseWithAudit_nestedRequiresNew_shouldHaveTwoCommits() {
        // Execute: Outer transaction + REQUIRES_NEW inner transaction
        orderService.purchaseWithAudit(testPetId);

        // Verify: Two independent commits
        // - One for the main purchase transaction
        // - One for the REQUIRES_NEW inventory update
        tracking.assertCommitCount(2);
        tracking.assertRollbackCount(0);

        System.out.println("Nested REQUIRES_NEW: " + tracking.getTransactionSummary());
        tracking.printReport();
    }

    @Test
    public void purchaseWithNestedSavepoint_innerFails_outerCommits() {
        // Execute: Outer succeeds, nested savepoint fails but is caught
        orderService.purchaseWithNestedSavepoint(testPetId, true);

        // Verify: Outer transaction still commits despite nested failure
        tracking.assertCommitCount(1);

        // The pet should be SOLD (outer transaction completed)
        PetEntity pet = petRepository.findOne(testPetId);
        assertEquals(Status.SOLD, pet.getStatus());

        System.out.println("Nested savepoint: " + tracking.getTransactionSummary());
    }

    // ==================== TransactionTemplate Tests ====================

    @Test
    public void purchasePetProgrammatic_shouldCommitOnce() {
        // Execute using TransactionTemplate
        orderService.purchasePetProgrammatic(testPetId);

        // Verify: Single commit
        tracking.assertCommitCount(1);
        tracking.assertNoRollback();

        System.out.println("Programmatic transaction: " + tracking.getTransactionSummary());
    }

    @Test
    public void purchaseWithManualRollback_shouldRollback() {
        // Execute with manual rollback flag
        orderService.purchaseWithManualRollback(testPetId, true);

        // Verify: Transaction rolled back (no exception thrown)
        tracking.assertTransactionRolledBack();

        // Pet should still be AVAILABLE (rolled back)
        TransactionTemplate readTx = new TransactionTemplate(txManager);
        readTx.setReadOnly(true);
        PetEntity pet = readTx.execute(s -> petRepository.findOne(testPetId));
        assertEquals(Status.AVAILABLE, pet.getStatus());

        System.out.println("Manual rollback: " + tracking.getTransactionSummary());
    }

    @Test
    public void batchPurchase_multipleTransactions_shouldCommitEach() {
        // Create additional pets
        TransactionTemplate tx = new TransactionTemplate(txManager);
        Long pet2Id = tx.execute(s -> {
            PetEntity pet = PetEntity.builder()
                    .name("Pet 2")
                    .status(Status.AVAILABLE)
                    .build();
            return petRepository.save(pet).getId();
        });
        Long pet3Id = tx.execute(s -> {
            PetEntity pet = PetEntity.builder()
                    .name("Pet 3")
                    .status(Status.AVAILABLE)
                    .build();
            return petRepository.save(pet).getId();
        });

        tracking.reset();

        // Execute batch purchase
        orderService.batchPurchase(Arrays.asList(testPetId, pet2Id, pet3Id));

        // Verify: 3 separate commits (one per pet)
        tracking.assertCommitCount(3);
        tracking.assertRollbackCount(0);

        System.out.println("Batch purchase: " + tracking.getTransactionSummary());
    }

    @Test
    public void purchaseWithIndependentAudit_nestedTransactionTemplate_twoCommits() {
        // Execute: Outer + inner REQUIRES_NEW via TransactionTemplate
        orderService.purchaseWithIndependentAudit(testPetId);

        // Verify: Two commits (outer + inner independent)
        tracking.assertCommitCount(2);

        System.out.println("Independent audit: " + tracking.getTransactionSummary());
        tracking.printReport();
    }

    @Test
    public void purchaseWithLowLevelControl_manualTransaction() {
        // Execute using PlatformTransactionManager directly
        orderService.purchaseWithLowLevelControl(testPetId);

        // Verify: Explicitly committed
        tracking.assertCommitCount(1);

        System.out.println("Low-level control: " + tracking.getTransactionSummary());
    }

    // ==================== Propagation Behavior Tests ====================

    @Test
    public void inventoryService_requiresNew_independentTransaction() {
        TransactionTemplate tx = new TransactionTemplate(txManager);

        tx.execute(status -> {
            // Outer transaction
            petRepository.findOne(testPetId);

            // Reset to measure just the inner transaction
            tracking.resetTransactionTracking();

            // REQUIRES_NEW - new independent transaction
            inventoryService.decrementInventory(categoryId);

            // Inner transaction should have committed already
            assertEquals("Inner REQUIRES_NEW should commit before outer",
                    1, tracking.getCommitCount());

            return null;
        });

        // Now outer also committed
        assertEquals(2, tracking.getCommitCount());
    }

    @Test
    public void inventoryService_mandatory_requiresExistingTransaction() {
        // Without transaction - should fail
        try {
            inventoryService.updateInventoryMandatory(categoryId);
            fail("Should throw exception when no transaction exists");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("No existing transaction"));
        }

        tracking.reset();

        // With transaction - should succeed
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.execute(status -> {
            inventoryService.updateInventoryMandatory(categoryId);
            return null;
        });

        tracking.assertCommitCount(1);
    }

    // ==================== Query + Transaction Combined Tests ====================

    @Test
    public void complexOperation_trackBothQueriesAndTransactions() {
        TransactionTemplate tx = new TransactionTemplate(txManager);

        tx.execute(status -> {
            // Multiple operations
            PetEntity pet = petRepository.findOne(testPetId);
            pet.setStatus(Status.PENDING);
            petRepository.save(pet);

            petRepository.findAll();

            pet.setStatus(Status.SOLD);
            petRepository.save(pet);

            return null;
        });

        // Verify transaction
        tracking.assertCommitCount(1);
        tracking.assertNoRollback();

        // Verify queries
        tracking.assertSelectCountAtLeast(2);  // findOne + findAll
        System.out.println("\n=== Complex Operation Report ===");
        tracking.printReport();
    }

    @Test
    public void readOnlyTransaction_shouldStillTrack() {
        // Execute read-only query
        orderService.findAvailablePets();

        // Even read-only transactions are tracked
        tracking.assertCommitCount(1);
        tracking.assertSelectCountAtLeast(1);

        System.out.println("Read-only: " + tracking.getTransactionSummary());
    }
}
