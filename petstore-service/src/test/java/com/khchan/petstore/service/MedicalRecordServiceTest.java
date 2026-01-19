package com.khchan.petstore.service;

import com.khchan.petstore.domain.MedicalRecord;
import com.khchan.petstore.domain.PetEntity;
import com.khchan.petstore.domain.Status;
import com.khchan.petstore.domain.Veterinarian;
import com.khchan.petstore.repository.AppointmentRepository;
import com.khchan.petstore.repository.MedicalRecordRepository;
import com.khchan.petstore.repository.PetRepository;
import com.khchan.petstore.repository.VeterinarianRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class MedicalRecordServiceTest {

    @Autowired
    private MedicalRecordService medicalRecordService;

    @Autowired
    private PetManagementService petManagementService;

    @Autowired
    private MedicalRecordRepository medicalRecordRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private VeterinarianRepository veterinarianRepository;

    @BeforeEach
    void clearData() {
        medicalRecordRepository.deleteAll();
        appointmentRepository.deleteAll();
        petRepository.deleteAll();
        veterinarianRepository.deleteAll();
    }

    @Test
    void createRecord_persistsRecord() {
        PetEntity pet = petManagementService.createPet("Rex", Status.AVAILABLE, null, null);
        Veterinarian vet = veterinarianRepository.save(new Veterinarian("Logan", "Reed", "General", "LIC-REC-1"));

        medicalRecordService.createRecord(
            pet.getId(),
            vet.getId(),
            LocalDate.now().minusDays(1),
            "Exam",
            "Healthy"
        );

        assertThat(medicalRecordRepository.count()).isEqualTo(1);
        MedicalRecord stored = medicalRecordService.findRecordsForPet(pet.getId()).get(0);
        assertThat(stored.getId()).isNotNull();
    }

    @Test
    void createRecord_rejectsFutureVisitDate() {
        PetEntity pet = petManagementService.createPet("Sky", Status.AVAILABLE, null, null);

        assertThrows(IllegalArgumentException.class, () ->
            medicalRecordService.createRecord(
                pet.getId(),
                null,
                LocalDate.now().plusDays(1),
                "Exam",
                "Notes"
            ));
    }
}
