package com.khchan.petstore.service;

import com.khchan.petstore.domain.Address;
import com.khchan.petstore.domain.Clinic;
import com.khchan.petstore.domain.Veterinarian;
import com.khchan.petstore.repository.AppointmentRepository;
import com.khchan.petstore.repository.ClinicRepository;
import com.khchan.petstore.repository.MedicalRecordRepository;
import com.khchan.petstore.repository.VaccinationRepository;
import com.khchan.petstore.repository.VeterinarianRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class VeterinarianServiceTest {

    @Autowired
    private VeterinarianService veterinarianService;

    @Autowired
    private VeterinarianRepository veterinarianRepository;

    @Autowired
    private ClinicRepository clinicRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private MedicalRecordRepository medicalRecordRepository;

    @Autowired
    private VaccinationRepository vaccinationRepository;

    @BeforeEach
    void clearData() {
        appointmentRepository.deleteAll();
        medicalRecordRepository.deleteAll();
        vaccinationRepository.deleteAll();
        veterinarianRepository.deleteAll();
        clinicRepository.deleteAll();
    }

    @Test
    void registerVeterinarian_enforcesUniqueLicense() {
        veterinarianService.registerVeterinarian("Sam", "Lee", "General", "LIC-UNIQUE");

        assertThrows(IllegalArgumentException.class,
            () -> veterinarianService.registerVeterinarian("Sam", "Lee", "General", "LIC-UNIQUE"));
        assertThat(veterinarianRepository.count()).isEqualTo(1);
    }

    @Test
    void assignAndUnassignClinic_updatesVeterinarian() {
        Clinic clinic = clinicRepository.save(
            new Clinic("Vet Center", "555-3000",
                new Address("20 Oak", "Boston", "MA", "02110", "USA")));
        Veterinarian vet = veterinarianService.registerVeterinarian("Ari", "Diaz", "Surgery", "LIC-ASSIGN");

        veterinarianService.assignClinic(vet.getId(), clinic.getId());
        Veterinarian assigned = veterinarianRepository.findById(vet.getId()).orElseThrow();
        assertThat(assigned.getClinic()).isNotNull();

        veterinarianService.unassignClinic(vet.getId());
        Veterinarian unassigned = veterinarianRepository.findById(vet.getId()).orElseThrow();
        assertThat(unassigned.getClinic()).isNull();
    }
}
