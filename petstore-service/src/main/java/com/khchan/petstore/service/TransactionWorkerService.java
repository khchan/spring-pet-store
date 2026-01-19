package com.khchan.petstore.service;

import com.khchan.petstore.domain.PetEntity;
import com.khchan.petstore.domain.Status;
import com.khchan.petstore.repository.PetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionWorkerService {

    private final PetRepository petRepository;

    @Autowired
    public TransactionWorkerService(PetRepository petRepository) {
        this.petRepository = petRepository;
    }

    /**
     * REQUIRES_NEW creates a brand-new transaction even if one already exists.
     * Useful for audit logs, outbox events, or side-effects that must commit
     * even when the caller rolls back. Beware: it can increase lock contention.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PetEntity updatePetStatusRequiresNew(Long petId, Status status) {
        PetEntity pet = petRepository.findById(petId)
            .orElseThrow(() -> new IllegalArgumentException("Pet not found: " + petId));
        pet.setStatus(status);
        return petRepository.save(pet);
    }

    /**
     * MANDATORY fails if no transaction exists.
     * Use when the caller must control the transaction boundary.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public long mandatoryCount() {
        return petRepository.count();
    }

    /**
     * SUPPORTS joins a transaction if present, otherwise runs non-transactionally.
     * Good for read operations that should not require a transaction.
     */
    @Transactional(propagation = Propagation.SUPPORTS)
    public long supportsCount() {
        return petRepository.count();
    }

    /**
     * NOT_SUPPORTED suspends any existing transaction and runs without one.
     * Use to avoid holding locks for slow operations, but be careful about
     * read consistency and partial failures.
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public long notSupportedCount() {
        return petRepository.count();
    }

    /**
     * NEVER throws if called within a transaction.
     * Useful for operations that must remain outside transactional context
     * (e.g., certain external calls).
     */
    @Transactional(propagation = Propagation.NEVER)
    public long neverCount() {
        return petRepository.count();
    }

    /**
     * NESTED creates a savepoint within an existing transaction.
     * This requires savepoint support in the configured JpaDialect and JDBC driver.
     */
    @Transactional(propagation = Propagation.NESTED)
    public PetEntity updatePetStatusNested(Long petId, Status status) {
        PetEntity pet = petRepository.findById(petId)
            .orElseThrow(() -> new IllegalArgumentException("Pet not found: " + petId));
        pet.setStatus(status);
        return petRepository.save(pet);
    }

    /**
     * NESTED example that can fail to demonstrate savepoint rollback.
     */
    @Transactional(propagation = Propagation.NESTED)
    public void updatePetStatusNestedWithFailure(Long petId, Status status, boolean fail) {
        PetEntity pet = petRepository.findById(petId)
            .orElseThrow(() -> new IllegalArgumentException("Pet not found: " + petId));
        pet.setStatus(status);
        petRepository.save(pet);
        if (fail) {
            throw new IllegalStateException("Forced nested failure");
        }
    }
}
