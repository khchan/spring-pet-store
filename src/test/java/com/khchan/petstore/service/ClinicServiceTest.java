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
public class ClinicServiceTest {

    @Autowired
    private ClinicService clinicService;

    @Autowired
    private ClinicRepository clinicRepository;

    @Autowired
    private VeterinarianRepository veterinarianRepository;

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
    void registerClinic_enforcesUniqueName() {
        Address address = new Address("10 Elm", "Boston", "MA", "02110", "USA");
        clinicService.registerClinic("City Clinic", "555-2000", address);

        assertThrows(IllegalArgumentException.class,
            () -> clinicService.registerClinic("City Clinic", "555-2000", address));
        assertThat(clinicRepository.count()).isEqualTo(1);
    }

    @Test
    void assignAndRemoveVeterinarian_updatesClinic() {
        Address address = new Address("11 Elm", "Boston", "MA", "02110", "USA");
        Clinic clinic = clinicService.registerClinic("Central Clinic", "555-2001", address);
        Veterinarian vet = veterinarianRepository.save(
            new Veterinarian("Riley", "Chen", "General", "LIC-CLINIC-1"));

        clinicService.assignVeterinarian(clinic.getId(), vet.getId());
        Veterinarian assigned = veterinarianRepository.findById(vet.getId()).orElseThrow();
        assertThat(assigned.getClinic()).isNotNull();

        clinicService.removeVeterinarian(clinic.getId(), vet.getId());
        Veterinarian removed = veterinarianRepository.findById(vet.getId()).orElseThrow();
        assertThat(removed.getClinic()).isNull();
    }
}
