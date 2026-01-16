package com.khchan.petstore.service;

import com.khchan.petstore.domain.PetEntity;
import com.khchan.petstore.domain.Status;
import com.khchan.petstore.domain.Vaccination;
import com.khchan.petstore.domain.Veterinarian;
import com.khchan.petstore.repository.AppointmentRepository;
import com.khchan.petstore.repository.PetRepository;
import com.khchan.petstore.repository.VaccinationRepository;
import com.khchan.petstore.repository.VeterinarianRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class VaccinationServiceTest {

    @Autowired
    private VaccinationService vaccinationService;

    @Autowired
    private PetManagementService petManagementService;

    @Autowired
    private VaccinationRepository vaccinationRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private VeterinarianRepository veterinarianRepository;

    @BeforeEach
    void clearData() {
        vaccinationRepository.deleteAll();
        appointmentRepository.deleteAll();
        petRepository.deleteAll();
        veterinarianRepository.deleteAll();
    }

    @Test
    void recordVaccination_persistsRecord() {
        PetEntity pet = petManagementService.createPet("Nova", Status.AVAILABLE, null, null);
        Veterinarian vet = veterinarianRepository.save(new Veterinarian("Jamie", "Kim", "General", "LIC-VACC-1"));

        vaccinationService.recordVaccination(
            pet.getId(),
            vet.getId(),
            "Rabies",
            LocalDate.now().minusDays(1),
            LocalDate.now().plusMonths(12)
        );

        assertThat(vaccinationRepository.count()).isEqualTo(1);
        Vaccination stored = vaccinationService.findVaccinationsForPet(pet.getId()).get(0);
        assertThat(stored.getId()).isNotNull();
    }

    @Test
    void updateNextDueDate_rejectsInvalidDate() {
        PetEntity pet = petManagementService.createPet("Piper", Status.AVAILABLE, null, null);
        vaccinationService.recordVaccination(
            pet.getId(),
            null,
            "Distemper",
            LocalDate.now().minusDays(1),
            LocalDate.now().plusMonths(6)
        );
        Vaccination stored = vaccinationService.findVaccinationsForPet(pet.getId()).get(0);

        assertThrows(IllegalArgumentException.class, () ->
            vaccinationService.updateNextDueDate(
                stored.getId(),
                LocalDate.now().minusDays(10)
            ));
    }
}
