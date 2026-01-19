package com.khchan.petstore.service;

import com.khchan.petstore.domain.Clinic;
import com.khchan.petstore.domain.Veterinarian;
import com.khchan.petstore.repository.ClinicRepository;
import com.khchan.petstore.repository.VeterinarianRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VeterinarianService {

    private final VeterinarianRepository veterinarianRepository;
    private final ClinicRepository clinicRepository;

    @Autowired
    public VeterinarianService(VeterinarianRepository veterinarianRepository, ClinicRepository clinicRepository) {
        this.veterinarianRepository = veterinarianRepository;
        this.clinicRepository = clinicRepository;
    }

    @Transactional
    public Veterinarian registerVeterinarian(String firstName, String lastName, String specialty, String licenseNumber) {
        requireNonBlank(licenseNumber, "License number is required");
        if (veterinarianRepository.findByLicenseNumber(licenseNumber).isPresent()) {
            throw new IllegalArgumentException("Veterinarian license already exists: " + licenseNumber);
        }
        Veterinarian vet = new Veterinarian(firstName, lastName, specialty, licenseNumber);
        return veterinarianRepository.save(vet);
    }

    @Transactional(readOnly = true)
    public Veterinarian findVeterinarian(Long veterinarianId) {
        return veterinarianRepository.findById(veterinarianId)
            .orElseThrow(() -> new IllegalArgumentException("Veterinarian not found: " + veterinarianId));
    }

    @Transactional(readOnly = true)
    public List<Veterinarian> findVeterinariansBySpecialty(String specialty) {
        requireNonBlank(specialty, "Specialty is required");
        return veterinarianRepository.findBySpecialty(specialty);
    }

    @Transactional
    public Veterinarian updateSpecialty(Long veterinarianId, String specialty) {
        requireNonBlank(specialty, "Specialty is required");
        Veterinarian vet = findVeterinarian(veterinarianId);
        vet.setSpecialty(specialty);
        return veterinarianRepository.save(vet);
    }

    @Transactional
    public Veterinarian assignClinic(Long veterinarianId, Long clinicId) {
        Veterinarian vet = findVeterinarian(veterinarianId);
        Clinic clinic = clinicRepository.findById(clinicId)
            .orElseThrow(() -> new IllegalArgumentException("Clinic not found: " + clinicId));
        if (vet.getClinic() != null && !clinicId.equals(vet.getClinic().getId())) {
            throw new IllegalStateException("Veterinarian already assigned to another clinic: " + veterinarianId);
        }
        vet.setClinic(clinic);
        return veterinarianRepository.save(vet);
    }

    @Transactional
    public Veterinarian unassignClinic(Long veterinarianId) {
        Veterinarian vet = findVeterinarian(veterinarianId);
        vet.setClinic(null);
        return veterinarianRepository.save(vet);
    }

    @Transactional
    public void removeVeterinarian(Long veterinarianId) {
        Veterinarian vet = findVeterinarian(veterinarianId);
        veterinarianRepository.delete(vet);
    }

    private void requireNonBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }
}
