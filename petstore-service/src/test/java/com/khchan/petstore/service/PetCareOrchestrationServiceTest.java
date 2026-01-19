package com.khchan.petstore.service;

import com.khchan.petstore.domain.Address;
import com.khchan.petstore.domain.Breed;
import com.khchan.petstore.domain.Owner;
import com.khchan.petstore.domain.PetEntity;
import com.khchan.petstore.domain.Size;
import com.khchan.petstore.domain.Status;
import com.khchan.petstore.domain.Veterinarian;
import com.khchan.petstore.repository.AppointmentRepository;
import com.khchan.petstore.repository.BreedRepository;
import com.khchan.petstore.repository.OwnerRepository;
import com.khchan.petstore.repository.PetInsuranceRepository;
import com.khchan.petstore.repository.PetRepository;
import com.khchan.petstore.repository.VeterinarianRepository;
import com.khchan.petstore.test.DataSourceProxyConfig;
import com.khchan.petstore.test.JpaQueryTrackingRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Import(DataSourceProxyConfig.class)
public class PetCareOrchestrationServiceTest {

    @RegisterExtension
    JpaQueryTrackingRule tracking = new JpaQueryTrackingRule()
        .printQueriesOnFailure(true);

    @Autowired
    private PetCareOrchestrationService orchestrationService;

    @Autowired
    private PetManagementService petManagementService;

    @Autowired
    private PetInsuranceService petInsuranceService;

    @Autowired
    private OwnerService ownerService;

    @Autowired
    private BreedRepository breedRepository;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private OwnerRepository ownerRepository;

    @Autowired
    private PetInsuranceRepository petInsuranceRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private VeterinarianRepository veterinarianRepository;

    @BeforeEach
    void clearData() {
        appointmentRepository.deleteAll();
        petInsuranceRepository.deleteAll();
        petRepository.deleteAll();
        ownerRepository.deleteAll();
        veterinarianRepository.deleteAll();
        breedRepository.deleteAll();
    }

    @Test
    void onboardOwnerWithPetAndInsurance_createsAllRecords() {
        Breed breed = breedRepository.save(new Breed("Spaniel", "Friendly", Size.MEDIUM));

        PetEntity pet = orchestrationService.onboardOwnerWithPetAndInsurance(
            "Ari",
            "Stone",
            "ari@example.com",
            "555-3000",
            new Address("10 Pine", "Boston", "MA", "02110", "USA"),
            "Sunny",
            breed.getId(),
            "POL-200",
            "CarePlus",
            new BigDecimal("7000.00"),
            new BigDecimal("55.00"),
            LocalDate.now(),
            LocalDate.now().plusYears(1)
        );

        assertThat(petRepository.count()).isEqualTo(1);
        assertThat(ownerRepository.count()).isEqualTo(1);
        assertThat(petInsuranceRepository.count()).isEqualTo(1);
        assertThat(pet.getInsurance()).isNotNull();
    }

    @Test
    void onboardOwnerWithPetAndInsurance_rollsBackOnDuplicatePolicy() {
        PetEntity setupPet = petManagementService.createPet("Shadow", Status.AVAILABLE, null, null);
        petInsuranceService.enrollPet(
            setupPet.getId(),
            "POL-DUP",
            "CarePlus",
            new BigDecimal("3000.00"),
            new BigDecimal("25.00"),
            LocalDate.now(),
            LocalDate.now().plusYears(1)
        );

        tracking.reset();

        assertThrows(IllegalArgumentException.class, () ->
            orchestrationService.onboardOwnerWithPetAndInsurance(
                "Jules",
                "Park",
                "jules@example.com",
                "555-3001",
                new Address("11 Pine", "Boston", "MA", "02110", "USA"),
                "Misty",
                null,
                "POL-DUP",
                "CarePlus",
                new BigDecimal("4000.00"),
                new BigDecimal("35.00"),
                LocalDate.now(),
                LocalDate.now().plusYears(1)
            ));

        assertThat(ownerRepository.count()).isZero();
        assertThat(petRepository.count()).isEqualTo(1);
        assertThat(petInsuranceRepository.count()).isEqualTo(1);
    }

    @Test
    void scheduleAppointment_nestedRequiresNew_commitsInner() {
        PetEntity pet = petManagementService.createPet("Finn", Status.AVAILABLE, null, null);
        Veterinarian vet = veterinarianRepository.save(new Veterinarian(
            "Rhea", "Park", "General", "LIC-ORCH-1"));

        tracking.reset();

        assertThrows(IllegalStateException.class, () ->
            orchestrationService.scheduleAppointmentAndUpdateVetSpecialty(
                pet.getId(),
                vet.getId(),
                LocalDateTime.now().plusDays(1),
                "Consult",
                "Surgery",
                true
            ));

        tracking.assertCommitCount(1);
        tracking.assertRollbackCount(1);
        tracking.resetTransactionTracking();

        assertThat(appointmentRepository.count()).isZero();
        assertThat(veterinarianRepository.findById(vet.getId()).orElseThrow().getSpecialty())
            .isEqualTo("Surgery");
    }

    @Test
    void updateOwnerAndPetStatus_requiredRollsBackTogether() {
        Owner owner = ownerService.registerOwner(
            "Rory",
            "Dean",
            "rory@example.com",
            "555-3002",
            new Address("12 Pine", "Boston", "MA", "02110", "USA")
        );
        PetEntity pet = petManagementService.createPet("Olive", Status.AVAILABLE, null, owner.getId());

        tracking.reset();

        assertThrows(IllegalStateException.class, () ->
            orchestrationService.updateOwnerAndPetStatus(
                owner.getId(),
                pet.getId(),
                "555-9999",
                Status.SOLD,
                true
            ));

        tracking.assertCommitCount(0);
        tracking.assertRollbackCount(1);
        tracking.resetTransactionTracking();

        Owner reloadedOwner = ownerRepository.findById(owner.getId()).orElseThrow();
        PetEntity reloadedPet = petRepository.findById(pet.getId()).orElseThrow();
        assertThat(reloadedOwner.getPhone()).isEqualTo("555-3002");
        assertThat(reloadedPet.getStatus()).isEqualTo(Status.AVAILABLE);
    }
}
