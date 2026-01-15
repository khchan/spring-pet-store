package com.khchan.petstore.service;

import com.khchan.petstore.domain.PetEntity;
import com.khchan.petstore.domain.Status;
import com.khchan.petstore.repository.PetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

/**
 * Example service demonstrating various transaction patterns.
 * Use this with JpaQueryTrackingRule to observe how transactions behave.
 */
@Service
public class OrderService {

    private final PetRepository petRepository;
    private final PlatformTransactionManager transactionManager;
    private final TransactionTemplate txTemplate;
    private final InventoryService inventoryService;

    @Autowired
    public OrderService(PetRepository petRepository,
                        PlatformTransactionManager transactionManager,
                        InventoryService inventoryService) {
        this.petRepository = petRepository;
        this.transactionManager = transactionManager;
        this.txTemplate = new TransactionTemplate(transactionManager);
        this.inventoryService = inventoryService;
    }

    // ==================== @Transactional Examples ====================

    /**
     * Simple transactional method - one commit at the end.
     */
    @Transactional
    public PetEntity purchasePet(Long petId) {
        PetEntity pet = petRepository.findOne(petId);
        if (pet == null) {
            throw new IllegalArgumentException("Pet not found: " + petId);
        }
        if (pet.getStatus() != Status.AVAILABLE) {
            throw new IllegalStateException("Pet is not available for purchase");
        }
        pet.setStatus(Status.SOLD);
        return petRepository.save(pet);
    }

    /**
     * Transactional with rollback on exception.
     * The transaction will rollback if validation fails.
     */
    @Transactional(rollbackFor = Exception.class)
    public PetEntity purchasePetWithValidation(Long petId, boolean forceRollback) {
        PetEntity pet = petRepository.findOne(petId);
        pet.setStatus(Status.SOLD);
        petRepository.save(pet);

        if (forceRollback) {
            throw new RuntimeException("Forced rollback for testing");
        }
        return pet;
    }

    /**
     * Nested transaction using REQUIRES_NEW - creates independent transaction.
     * The inner transaction can commit/rollback independently.
     */
    @Transactional
    public void purchaseWithAudit(Long petId) {
        // Main transaction: update pet status
        PetEntity pet = petRepository.findOne(petId);
        pet.setStatus(Status.SOLD);
        petRepository.save(pet);

        // Nested transaction: update inventory (REQUIRES_NEW)
        // This commits independently even if outer transaction fails
        inventoryService.decrementInventory(pet.getCategory().getId());
    }

    /**
     * Demonstrates NESTED propagation - savepoint based.
     * Rollback only affects the nested portion.
     */
    @Transactional
    public void purchaseWithNestedSavepoint(Long petId, boolean failNested) {
        // Outer transaction work
        PetEntity pet = petRepository.findOne(petId);
        pet.setStatus(Status.PENDING);
        petRepository.save(pet);

        try {
            // Nested savepoint - can rollback independently
            inventoryService.updateInventoryNested(pet.getCategory().getId(), failNested);
        } catch (Exception e) {
            // Nested rolled back, but we continue
            System.out.println("Nested transaction rolled back: " + e.getMessage());
        }

        // Continue with outer transaction
        pet.setStatus(Status.SOLD);
        petRepository.save(pet);
    }

    // ==================== TransactionTemplate Examples ====================

    /**
     * Programmatic transaction using TransactionTemplate.
     * Equivalent to @Transactional but with explicit control.
     */
    public PetEntity purchasePetProgrammatic(Long petId) {
        return txTemplate.execute(status -> {
            PetEntity pet = petRepository.findOne(petId);
            pet.setStatus(Status.SOLD);
            return petRepository.save(pet);
        });
    }

    /**
     * TransactionTemplate with manual rollback.
     */
    public void purchaseWithManualRollback(Long petId, boolean shouldRollback) {
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                PetEntity pet = petRepository.findOne(petId);
                pet.setStatus(Status.SOLD);
                petRepository.save(pet);

                if (shouldRollback) {
                    // Mark for rollback - no exception needed
                    status.setRollbackOnly();
                }
            }
        });
    }

    /**
     * Multiple independent transactions using TransactionTemplate.
     * Each execute() block is a separate transaction.
     */
    public void batchPurchase(List<Long> petIds) {
        for (Long petId : petIds) {
            // Each iteration is a separate transaction
            txTemplate.execute(status -> {
                PetEntity pet = petRepository.findOne(petId);
                if (pet != null && pet.getStatus() == Status.AVAILABLE) {
                    pet.setStatus(Status.SOLD);
                    petRepository.save(pet);
                }
                return null;
            });
        }
    }

    /**
     * Nested TransactionTemplates with REQUIRES_NEW.
     * Inner transaction commits independently.
     */
    public void purchaseWithIndependentAudit(Long petId) {
        // Outer transaction
        txTemplate.execute(outerStatus -> {
            PetEntity pet = petRepository.findOne(petId);
            pet.setStatus(Status.SOLD);
            petRepository.save(pet);

            // Inner independent transaction using REQUIRES_NEW
            TransactionTemplate innerTx = new TransactionTemplate(transactionManager);
            innerTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

            innerTx.execute(innerStatus -> {
                // This runs in a separate transaction
                // Audit logging, notification, etc.
                PetEntity auditPet = petRepository.findOne(petId);
                System.out.println("Audit: Pet " + auditPet.getName() + " purchased");
                return null;
            });

            return pet;
        });
    }

    /**
     * Low-level transaction control using PlatformTransactionManager directly.
     * Maximum flexibility for complex scenarios.
     */
    public void purchaseWithLowLevelControl(Long petId) {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("PurchaseTransaction");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        def.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);

        TransactionStatus status = transactionManager.getTransaction(def);

        try {
            PetEntity pet = petRepository.findOne(petId);
            pet.setStatus(Status.SOLD);
            petRepository.save(pet);

            transactionManager.commit(status);
        } catch (Exception e) {
            transactionManager.rollback(status);
            throw e;
        }
    }

    /**
     * Read-only transaction for query optimization.
     */
    @Transactional(readOnly = true)
    public List<PetEntity> findAvailablePets() {
        // Read-only transactions can be optimized by the database
        return petRepository.findAll().stream()
                .filter(p -> p.getStatus() == Status.AVAILABLE)
                .toList();
    }

    /**
     * Transaction with timeout.
     */
    @Transactional(timeout = 5)  // 5 second timeout
    public void longRunningPurchase(Long petId) {
        PetEntity pet = petRepository.findOne(petId);
        // Simulate long operation
        pet.setStatus(Status.SOLD);
        petRepository.save(pet);
    }
}
