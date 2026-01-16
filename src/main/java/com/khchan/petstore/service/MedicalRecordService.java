package com.khchan.petstore.service;

import com.khchan.petstore.domain.MedicalRecord;
import com.khchan.petstore.domain.PetEntity;
import com.khchan.petstore.domain.Veterinarian;
import com.khchan.petstore.repository.MedicalRecordRepository;
import com.khchan.petstore.repository.PetRepository;
import com.khchan.petstore.repository.VeterinarianRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final PetRepository petRepository;
    private final VeterinarianRepository veterinarianRepository;

    @Autowired
    public MedicalRecordService(MedicalRecordRepository medicalRecordRepository,
                                PetRepository petRepository,
                                VeterinarianRepository veterinarianRepository) {
        this.medicalRecordRepository = medicalRecordRepository;
        this.petRepository = petRepository;
        this.veterinarianRepository = veterinarianRepository;
    }

    @Transactional
    public MedicalRecord createRecord(Long petId, Long veterinarianId, LocalDate visitDate,
                                      String diagnosis, String treatment) {
        if (visitDate == null) {
            throw new IllegalArgumentException("Visit date is required");
        }
        if (visitDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Visit date cannot be in the future");
        }
        PetEntity pet = petRepository.findById(petId)
            .orElseThrow(() -> new IllegalArgumentException("Pet not found: " + petId));
        Veterinarian veterinarian = null;
        if (veterinarianId != null) {
            veterinarian = veterinarianRepository.findById(veterinarianId)
                .orElseThrow(() -> new IllegalArgumentException("Veterinarian not found: " + veterinarianId));
        }
        MedicalRecord record = new MedicalRecord(pet, veterinarian, visitDate, diagnosis, treatment);
        pet.addMedicalRecord(record);
        petRepository.save(pet);
        return record;
    }

    @Transactional(readOnly = true)
    public MedicalRecord findRecord(Long recordId) {
        return medicalRecordRepository.findById(recordId)
            .orElseThrow(() -> new IllegalArgumentException("Medical record not found: " + recordId));
    }

    @Transactional(readOnly = true)
    public List<MedicalRecord> findRecordsForPet(Long petId) {
        return medicalRecordRepository.findByPetIdOrderByVisitDateDesc(petId);
    }

    @Transactional
    public MedicalRecord updateNotes(Long recordId, String notes) {
        MedicalRecord record = findRecord(recordId);
        record.setNotes(notes);
        return medicalRecordRepository.save(record);
    }

    @Transactional
    public MedicalRecord updateTreatment(Long recordId, String treatment) {
        MedicalRecord record = findRecord(recordId);
        record.setTreatment(treatment);
        return medicalRecordRepository.save(record);
    }

    @Transactional
    public void deleteRecord(Long recordId) {
        MedicalRecord record = findRecord(recordId);
        medicalRecordRepository.delete(record);
    }
}
