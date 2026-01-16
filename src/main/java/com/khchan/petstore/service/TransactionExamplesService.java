package com.khchan.petstore.service;

import com.khchan.petstore.domain.PetEntity;
import com.khchan.petstore.domain.Status;
import com.khchan.petstore.repository.PetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.NestedTransactionNotSupportedException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Examples of transactional settings and when to use them.
 *
 * Pitfalls to watch:
 * - Self-invocation won't go through Spring proxies, so transactional annotations are ignored.
 * - Read-only is a hint; it does not prevent writes unless your DB enforces it.
 * - REQUIRES_NEW can increase lock contention and unexpected partial commits.
 * - NESTED requires savepoint support; many JPA setups do not support it.
 */
@Service
public class TransactionExamplesService {

    private final PetRepository petRepository;
    private final TransactionWorkerService workerService;
    private final TransactionTemplate transactionTemplate;
    private final TransactionTemplate requiresNewTemplate;

    @Autowired
    public TransactionExamplesService(PetRepository petRepository,
                                      TransactionWorkerService workerService,
                                      PlatformTransactionManager transactionManager) {
        this.petRepository = petRepository;
        this.workerService = workerService;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.requiresNewTemplate = new TransactionTemplate(transactionManager);
        this.requiresNewTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    /**
     * REQUIRED is the default: joins an existing transaction or starts a new one.
     * Use for typical service methods that should be atomic.
     */
    @Transactional
    public PetEntity updatePetStatusRequired(Long petId, Status status, boolean failAfter) {
        PetEntity pet = petRepository.findById(petId)
            .orElseThrow(() -> new IllegalArgumentException("Pet not found: " + petId));
        pet.setStatus(status);
        petRepository.save(pet);
        if (failAfter) {
            throw new IllegalStateException("Forced rollback in REQUIRED transaction");
        }
        return pet;
    }

    /**
     * REQUIRES_NEW inside REQUIRED: the inner update commits even if the outer fails.
     * Use when an operation must commit regardless of the caller (audit/outbox).
     */
    @Transactional
    public void updateTwoPetsWithRequiresNew(Long outerPetId,
                                             Long innerPetId,
                                             Status outerStatus,
                                             Status innerStatus,
                                             boolean failAfter) {
        PetEntity outer = petRepository.findById(outerPetId)
            .orElseThrow(() -> new IllegalArgumentException("Pet not found: " + outerPetId));
        outer.setStatus(outerStatus);
        petRepository.save(outer);

        workerService.updatePetStatusRequiresNew(innerPetId, innerStatus);

        if (failAfter) {
            throw new IllegalStateException("Forced rollback in outer transaction");
        }
    }

    /**
     * Read-only transactions can be optimized by the DB.
     * Use for read-heavy methods where no writes should occur.
     */
    @Transactional(readOnly = true)
    public long readOnlyCount() {
        return petRepository.count();
    }

    /**
     * Isolation controls read phenomena (dirty/non-repeatable/phantom reads).
     * Use higher isolation only when necessary; it can reduce concurrency.
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ, readOnly = true)
    public long repeatableReadCount() {
        return petRepository.count();
    }

    /**
     * Timeout guards long-running operations.
     * Use to fail fast when a transaction should not exceed a given duration.
     */
    @Transactional(timeout = 3)
    public PetEntity updateWithTimeout(Long petId, Status status) {
        PetEntity pet = petRepository.findById(petId)
            .orElseThrow(() -> new IllegalArgumentException("Pet not found: " + petId));
        pet.setStatus(status);
        return petRepository.save(pet);
    }

    /**
     * MANDATORY requires an active transaction.
     * Useful for enforcing a caller-defined transactional boundary.
     */
    public long mandatoryCountOutsideTransaction() {
        return workerService.mandatoryCount();
    }

    /**
     * MANDATORY succeeds when called inside a REQUIRED transaction.
     */
    @Transactional
    public long mandatoryCountWithinTransaction() {
        return workerService.mandatoryCount();
    }

    /**
     * NEVER throws when invoked inside a transaction.
     * Useful for methods that must stay outside transactional context.
     */
    @Transactional
    public void neverWithinTransaction() {
        workerService.neverCount();
    }

    /**
     * NOT_SUPPORTED suspends the current transaction and runs without one.
     * Good for slow read-only operations that should not hold locks.
     */
    @Transactional
    public long requiredWithNotSupportedRead() {
        long count = workerService.notSupportedCount();
        petRepository.count();
        return count;
    }

    /**
     * SUPPORTS without a surrounding transaction runs non-transactionally.
     */
    public long supportsWithoutTransaction() {
        return workerService.supportsCount();
    }

    /**
     * TransactionTemplate is a programmatic alternative to @Transactional.
     * Use when you need dynamic propagation or conditional rollback behavior.
     */
    public void templateUpdatePet(Long petId, Status status) {
        transactionTemplate.execute(statusTxn -> {
            PetEntity pet = petRepository.findById(petId)
                .orElseThrow(() -> new IllegalArgumentException("Pet not found: " + petId));
            pet.setStatus(status);
            petRepository.save(pet);
            return null;
        });
    }

    /**
     * Nested TransactionTemplate with REQUIRES_NEW: inner commit survives outer rollback.
     */
    public void templateRequiresNewWithRollback(Long outerPetId,
                                                Long innerPetId,
                                                Status outerStatus,
                                                Status innerStatus,
                                                boolean failAfter) {
        transactionTemplate.execute(statusTxn -> {
            PetEntity outer = petRepository.findById(outerPetId)
                .orElseThrow(() -> new IllegalArgumentException("Pet not found: " + outerPetId));
            outer.setStatus(outerStatus);
            petRepository.save(outer);

            requiresNewTemplate.execute(innerStatusTxn -> {
                PetEntity inner = petRepository.findById(innerPetId)
                    .orElseThrow(() -> new IllegalArgumentException("Pet not found: " + innerPetId));
                inner.setStatus(innerStatus);
                petRepository.save(inner);
                return null;
            });

            if (failAfter) {
                throw new IllegalStateException("Forced rollback in outer template");
            }
            return null;
        });
    }

    /**
     * NESTED uses a savepoint inside an existing transaction.
     * Use when you want to roll back a sub-operation without aborting the outer work.
     * Pitfall: savepoints are not supported by all JPA dialects and will throw.
     */
    @Transactional
    public void nestedSavepointExample(Long outerPetId, Long nestedPetId, boolean failNested) {
        PetEntity outer = petRepository.findById(outerPetId)
            .orElseThrow(() -> new IllegalArgumentException("Pet not found: " + outerPetId));
        outer.setStatus(Status.PENDING);
        petRepository.save(outer);

        try {
            workerService.updatePetStatusNestedWithFailure(nestedPetId, Status.SOLD, failNested);
        } catch (NestedTransactionNotSupportedException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            if (!failNested) {
                throw ex;
            }
        }

        outer.setStatus(Status.SOLD);
        petRepository.save(outer);
    }
}
