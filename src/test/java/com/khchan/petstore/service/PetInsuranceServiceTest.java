package com.khchan.petstore.service;

import com.khchan.petstore.domain.InsuranceStatus;
import com.khchan.petstore.domain.PetEntity;
import com.khchan.petstore.domain.Status;
import com.khchan.petstore.repository.PetInsuranceRepository;
import com.khchan.petstore.repository.PetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class PetInsuranceServiceTest {

    @Autowired
    private PetInsuranceService petInsuranceService;

    @Autowired
    private PetManagementService petManagementService;

    @Autowired
    private PetInsuranceRepository petInsuranceRepository;

    @Autowired
    private PetRepository petRepository;

    @BeforeEach
    void clearData() {
        petInsuranceRepository.deleteAll();
        petRepository.deleteAll();
    }

    @Test
    void enrollPet_persistsInsurance() {
        PetEntity pet = petManagementService.createPet("Juno", Status.AVAILABLE, null, null);

        petInsuranceService.enrollPet(
            pet.getId(),
            "POL-100",
            "CarePlus",
            new BigDecimal("5000.00"),
            new BigDecimal("40.00"),
            LocalDate.now(),
            LocalDate.now().plusYears(1)
        );

        assertThat(petInsuranceRepository.count()).isEqualTo(1);
    }

    @Test
    void enrollPet_rejectsDuplicatePolicy() {
        PetEntity pet = petManagementService.createPet("Loki", Status.AVAILABLE, null, null);
        petInsuranceService.enrollPet(
            pet.getId(),
            "POL-101",
            "CarePlus",
            new BigDecimal("4000.00"),
            new BigDecimal("35.00"),
            LocalDate.now(),
            LocalDate.now().plusYears(1)
        );

        PetEntity secondPet = petManagementService.createPet("Mona", Status.AVAILABLE, null, null);
        assertThrows(IllegalArgumentException.class, () ->
            petInsuranceService.enrollPet(
                secondPet.getId(),
                "POL-101",
                "CarePlus",
                new BigDecimal("3000.00"),
                new BigDecimal("30.00"),
                LocalDate.now(),
                LocalDate.now().plusYears(1)
            ));
    }

    @Test
    void cancelPolicy_updatesStatusAndEndDate() {
        PetEntity pet = petManagementService.createPet("Zoe", Status.AVAILABLE, null, null);
        petInsuranceService.enrollPet(
            pet.getId(),
            "POL-102",
            "CarePlus",
            new BigDecimal("6000.00"),
            new BigDecimal("45.00"),
            LocalDate.now(),
            LocalDate.now().plusYears(1)
        );
        var insurance = petInsuranceRepository.findByPolicyNumber("POL-102").orElseThrow();

        var cancelled = petInsuranceService.cancelPolicy(insurance.getId());

        assertThat(cancelled.getStatus()).isEqualTo(InsuranceStatus.CANCELLED);
        assertThat(cancelled.getEndDate()).isEqualTo(LocalDate.now());
    }
}
