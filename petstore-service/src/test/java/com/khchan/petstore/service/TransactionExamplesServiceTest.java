package com.khchan.petstore.service;

import com.khchan.petstore.domain.PetEntity;
import com.khchan.petstore.domain.Status;
import com.khchan.petstore.repository.PetRepository;
import com.khchan.petstore.test.DataSourceProxyConfig;
import com.khchan.petstore.test.JpaQueryTrackingRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Import(DataSourceProxyConfig.class)
public class TransactionExamplesServiceTest {

    @RegisterExtension
    JpaQueryTrackingRule tracking = new JpaQueryTrackingRule()
        .printQueriesOnFailure(true);

    @Autowired
    private TransactionExamplesService transactionExamplesService;

    @Autowired
    private PetRepository petRepository;

    private Long petId;
    private Long petId2;

    @BeforeEach
    void setUp() {
        petRepository.deleteAll();
        petId = petRepository.save(buildPet("Alpha")).getId();
        petId2 = petRepository.save(buildPet("Bravo")).getId();
        tracking.reset();
    }

    @Test
    void requiredRollsBackOnException() {
        assertThrows(IllegalStateException.class, () ->
            transactionExamplesService.updatePetStatusRequired(petId, Status.SOLD, true));

        tracking.assertSelectCountAtLeast(1);
        assertThat(tracking.getUpdateCount()).isBetween(0, 1);
        tracking.assertInsertCount(0);
        tracking.assertDeleteCount(0);
        tracking.assertCommitCount(0);
        tracking.assertRollbackCount(1);
        tracking.resetTransactionTracking();
        tracking.resetQueryCounters();

        PetEntity reloaded = petRepository.findById(petId).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(Status.AVAILABLE);
    }

    @Test
    void requiresNewCommitsInnerWhenOuterRollsBack() {
        assertThrows(IllegalStateException.class, () ->
            transactionExamplesService.updateTwoPetsWithRequiresNew(
                petId,
                petId2,
                Status.SOLD,
                Status.PENDING,
                true
            ));

        tracking.assertSelectCountAtLeast(2);
        assertThat(tracking.getUpdateCount()).isBetween(1, 2);
        tracking.assertInsertCount(0);
        tracking.assertDeleteCount(0);
        tracking.assertCommitCount(1);
        tracking.assertRollbackCount(1);
        tracking.resetTransactionTracking();
        tracking.resetQueryCounters();

        PetEntity outer = petRepository.findById(petId).orElseThrow();
        PetEntity inner = petRepository.findById(petId2).orElseThrow();
        assertThat(outer.getStatus()).isEqualTo(Status.AVAILABLE);
        assertThat(inner.getStatus()).isEqualTo(Status.PENDING);
    }

    @Test
    void mandatoryRequiresExistingTransaction() {
        assertThrows(Exception.class, () -> transactionExamplesService.mandatoryCountOutsideTransaction());

        long count = transactionExamplesService.mandatoryCountWithinTransaction();
        assertThat(count).isEqualTo(2);
        tracking.assertSelectCount(1);
        tracking.assertInsertCount(0);
        tracking.assertUpdateCount(0);
        tracking.assertDeleteCount(0);
        tracking.assertCommitCount(1);
    }

    @Test
    void neverFailsInsideTransaction() {
        assertThrows(Exception.class, () -> transactionExamplesService.neverWithinTransaction());
        tracking.assertRollbackCount(1);
    }

    @Test
    void notSupportedDoesNotStartInnerTransaction() {
        transactionExamplesService.requiredWithNotSupportedRead();
        tracking.assertSelectCount(2);
        tracking.assertInsertCount(0);
        tracking.assertUpdateCount(0);
        tracking.assertDeleteCount(0);
        tracking.assertCommitCount(2);
        tracking.assertRollbackCount(0);
    }

    @Test
    void supportsWithoutTransactionDoesNotCreateCommit() {
        transactionExamplesService.supportsWithoutTransaction();
        tracking.assertSelectCount(1);
        tracking.assertInsertCount(0);
        tracking.assertUpdateCount(0);
        tracking.assertDeleteCount(0);
        tracking.assertCommitCount(1);
        tracking.assertRollbackCount(0);
    }

    @Test
    void readOnlyStillCommitsTransaction() {
        transactionExamplesService.readOnlyCount();
        tracking.assertSelectCount(1);
        tracking.assertInsertCount(0);
        tracking.assertUpdateCount(0);
        tracking.assertDeleteCount(0);
        tracking.assertCommitCount(1);
        tracking.assertRollbackCount(0);
    }

    @Test
    void transactionTemplateRequiresNewCommitsInner() {
        assertThrows(IllegalStateException.class, () ->
            transactionExamplesService.templateRequiresNewWithRollback(
                petId,
                petId2,
                Status.SOLD,
                Status.PENDING,
                true
            ));

        tracking.assertSelectCountAtLeast(2);
        assertThat(tracking.getUpdateCount()).isBetween(1, 2);
        tracking.assertInsertCount(0);
        tracking.assertDeleteCount(0);
        tracking.assertCommitCount(1);
        tracking.assertRollbackCount(1);
        tracking.resetTransactionTracking();
        tracking.resetQueryCounters();

        PetEntity outer = petRepository.findById(petId).orElseThrow();
        PetEntity inner = petRepository.findById(petId2).orElseThrow();
        assertThat(outer.getStatus()).isEqualTo(Status.AVAILABLE);
        assertThat(inner.getStatus()).isEqualTo(Status.PENDING);
    }

    @Test
    void nestedSavepoint_rollsBackNestedOrThrowsWhenUnsupported() {
        boolean supported = true;
        try {
            transactionExamplesService.nestedSavepointExample(petId, petId2, true);
        } catch (Exception ex) {
            supported = false;
            assertThat(ex.getMessage()).containsIgnoringCase("savepoint");
        }

        PetEntity outer = petRepository.findById(petId).orElseThrow();
        PetEntity inner = petRepository.findById(petId2).orElseThrow();

        if (supported) {
            assertThat(outer.getStatus()).isEqualTo(Status.SOLD);
            assertThat(inner.getStatus()).isEqualTo(Status.AVAILABLE);
        } else {
            assertThat(outer.getStatus()).isEqualTo(Status.AVAILABLE);
            assertThat(inner.getStatus()).isEqualTo(Status.AVAILABLE);
        }
    }

    private PetEntity buildPet(String name) {
        PetEntity pet = new PetEntity();
        pet.setName(name);
        pet.setStatus(Status.AVAILABLE);
        return pet;
    }
}
