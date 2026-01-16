package com.khchan.petstore.service;

import com.khchan.petstore.domain.PetEntity;
import com.khchan.petstore.domain.Vaccination;
import com.khchan.petstore.domain.Veterinarian;
import com.khchan.petstore.repository.PetRepository;
import com.khchan.petstore.repository.VaccinationRepository;
import com.khchan.petstore.repository.VeterinarianRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class VaccinationService {

    private final VaccinationRepository vaccinationRepository;
    private final PetRepository petRepository;
    private final VeterinarianRepository veterinarianRepository;

    @Autowired
    public VaccinationService(VaccinationRepository vaccinationRepository,
                              PetRepository petRepository,
                              VeterinarianRepository veterinarianRepository) {
        this.vaccinationRepository = vaccinationRepository;
        this.petRepository = petRepository;
        this.veterinarianRepository = veterinarianRepository;
    }

    @Transactional
    public Vaccination recordVaccination(Long petId,
                                         Long veterinarianId,
                                         String vaccineName,
                                         LocalDate dateAdministered,
                                         LocalDate nextDueDate) {
        requireNonBlank(vaccineName, "Vaccine name is required");
        if (dateAdministered == null) {
            throw new IllegalArgumentException("Date administered is required");
        }
        if (dateAdministered.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Date administered cannot be in the future");
        }
        if (nextDueDate != null && nextDueDate.isBefore(dateAdministered)) {
            throw new IllegalArgumentException("Next due date cannot be before administered date");
        }
        PetEntity pet = petRepository.findById(petId)
            .orElseThrow(() -> new IllegalArgumentException("Pet not found: " + petId));
        Veterinarian veterinarian = null;
        if (veterinarianId != null) {
            veterinarian = veterinarianRepository.findById(veterinarianId)
                .orElseThrow(() -> new IllegalArgumentException("Veterinarian not found: " + veterinarianId));
        }
        Vaccination vaccination = new Vaccination(pet, vaccineName, dateAdministered, nextDueDate, veterinarian);
        pet.addVaccination(vaccination);
        petRepository.save(pet);
        return vaccination;
    }

    @Transactional(readOnly = true)
    public Vaccination findVaccination(Long vaccinationId) {
        return vaccinationRepository.findById(vaccinationId)
            .orElseThrow(() -> new IllegalArgumentException("Vaccination not found: " + vaccinationId));
    }

    @Transactional(readOnly = true)
    public List<Vaccination> findVaccinationsForPet(Long petId) {
        return vaccinationRepository.findByPetIdOrderByDateDesc(petId);
    }

    @Transactional
    public Vaccination updateNotes(Long vaccinationId, String notes) {
        Vaccination vaccination = findVaccination(vaccinationId);
        vaccination.setNotes(notes);
        return vaccinationRepository.save(vaccination);
    }

    @Transactional
    public Vaccination updateNextDueDate(Long vaccinationId, LocalDate nextDueDate) {
        Vaccination vaccination = findVaccination(vaccinationId);
        if (nextDueDate != null && vaccination.getDateAdministered() != null
            && nextDueDate.isBefore(vaccination.getDateAdministered())) {
            throw new IllegalArgumentException("Next due date cannot be before administered date");
        }
        vaccination.setNextDueDate(nextDueDate);
        return vaccinationRepository.save(vaccination);
    }

    @Transactional
    public void deleteVaccination(Long vaccinationId) {
        Vaccination vaccination = findVaccination(vaccinationId);
        vaccinationRepository.delete(vaccination);
    }

    private void requireNonBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }
}
