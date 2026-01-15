package com.khchan.petstore.service;

import com.khchan.petstore.repository.PetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service demonstrating different transaction propagation behaviors.
 * Used to show how nested/independent transactions work.
 */
@Service
public class InventoryService {

    private final PetRepository petRepository;

    @Autowired
    public InventoryService(PetRepository petRepository) {
        this.petRepository = petRepository;
    }

    /**
     * REQUIRES_NEW: Always creates a new transaction.
     * - Suspends any existing transaction
     * - Commits/rollbacks independently of the caller
     * - Useful for audit logs that must persist even if main transaction fails
     *
     * Transaction flow:
     * 1. Caller's transaction is suspended
     * 2. New transaction started
     * 3. This method executes
     * 4. New transaction commits/rollbacks
     * 5. Caller's transaction resumes
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void decrementInventory(Long categoryId) {
        // This runs in its own independent transaction
        // Even if the calling transaction rolls back, this commit remains
        System.out.println("Decrementing inventory for category: " + categoryId);

        // Simulate some inventory work
        long count = petRepository.count();
        System.out.println("Current pet count: " + count);
    }

    /**
     * REQUIRES_NEW with exception handling.
     * Demonstrates that inner transaction failure doesn't affect outer.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void decrementInventoryWithFailure(Long categoryId, boolean shouldFail) {
        System.out.println("Processing inventory for category: " + categoryId);

        if (shouldFail) {
            throw new RuntimeException("Inventory update failed!");
        }

        System.out.println("Inventory updated successfully");
    }

    /**
     * NESTED: Creates a savepoint within the existing transaction.
     * - Rollback only affects this nested portion
     * - If outer transaction rolls back, nested work is also rolled back
     * - Requires JDBC 3.0+ driver with savepoint support
     *
     * Note: Not all databases/drivers support NESTED. H2 does support it.
     *
     * Transaction flow:
     * 1. Savepoint created in current transaction
     * 2. This method executes
     * 3. On success: savepoint released
     * 4. On failure: rollback to savepoint (not entire transaction)
     */
    @Transactional(propagation = Propagation.NESTED)
    public void updateInventoryNested(Long categoryId, boolean shouldFail) {
        System.out.println("Nested update for category: " + categoryId);

        // Work within the nested savepoint
        petRepository.count();

        if (shouldFail) {
            // This will rollback to savepoint, not entire transaction
            throw new RuntimeException("Nested operation failed - rolling back to savepoint");
        }
    }

    /**
     * MANDATORY: Must be called within an existing transaction.
     * Throws exception if no transaction exists.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void updateInventoryMandatory(Long categoryId) {
        System.out.println("Mandatory transaction update for: " + categoryId);
        petRepository.count();
    }

    /**
     * SUPPORTS: Uses existing transaction if available, otherwise non-transactional.
     */
    @Transactional(propagation = Propagation.SUPPORTS)
    public long getInventoryCount() {
        return petRepository.count();
    }

    /**
     * NOT_SUPPORTED: Always executes non-transactionally.
     * Suspends any existing transaction.
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void logInventoryNonTransactional(Long categoryId) {
        // Runs outside of any transaction
        System.out.println("Non-transactional log for category: " + categoryId);
    }

    /**
     * NEVER: Must NOT be called within a transaction.
     * Throws exception if a transaction exists.
     */
    @Transactional(propagation = Propagation.NEVER)
    public void checkInventoryNever() {
        System.out.println("Checking inventory (must not be in transaction)");
    }
}
