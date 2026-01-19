package com.khchan.petstore.service;

import com.khchan.petstore.domain.Address;
import com.khchan.petstore.domain.Clinic;
import com.khchan.petstore.domain.Veterinarian;
import com.khchan.petstore.repository.ClinicRepository;
import com.khchan.petstore.repository.VeterinarianRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClinicService {

    private final ClinicRepository clinicRepository;
    private final VeterinarianRepository veterinarianRepository;

    @Autowired
    public ClinicService(ClinicRepository clinicRepository, VeterinarianRepository veterinarianRepository) {
        this.clinicRepository = clinicRepository;
        this.veterinarianRepository = veterinarianRepository;
    }

    @Transactional
    public Clinic registerClinic(String name, String phone, Address address) {
        requireNonBlank(name, "Clinic name is required");
        if (clinicRepository.findByName(name).isPresent()) {
            throw new IllegalArgumentException("Clinic already exists: " + name);
        }
        Clinic clinic = new Clinic(name, phone, address);
        return clinicRepository.save(clinic);
    }

    @Transactional(readOnly = true)
    public Clinic findClinic(Long clinicId) {
        return clinicRepository.findById(clinicId)
            .orElseThrow(() -> new IllegalArgumentException("Clinic not found: " + clinicId));
    }

    @Transactional(readOnly = true)
    public List<Clinic> findClinicsByCity(String city) {
        requireNonBlank(city, "City is required");
        return clinicRepository.findByAddress_City(city);
    }

    @Transactional
    public Clinic updateContact(Long clinicId, String phone, Address address) {
        Clinic clinic = findClinic(clinicId);
        if (phone != null) {
            clinic.setPhone(phone);
        }
        if (address != null) {
            clinic.setAddress(address);
        }
        return clinicRepository.save(clinic);
    }

    @Transactional
    public Clinic assignVeterinarian(Long clinicId, Long veterinarianId) {
        Clinic clinic = clinicRepository.findByIdWithVeterinarians(clinicId)
            .orElseThrow(() -> new IllegalArgumentException("Clinic not found: " + clinicId));
        Veterinarian veterinarian = veterinarianRepository.findById(veterinarianId)
            .orElseThrow(() -> new IllegalArgumentException("Veterinarian not found: " + veterinarianId));
        if (veterinarian.getClinic() != null && !clinicId.equals(veterinarian.getClinic().getId())) {
            throw new IllegalStateException("Veterinarian already assigned to another clinic: " + veterinarianId);
        }
        clinic.addVeterinarian(veterinarian);
        return clinicRepository.save(clinic);
    }

    @Transactional
    public Clinic removeVeterinarian(Long clinicId, Long veterinarianId) {
        Clinic clinic = clinicRepository.findByIdWithVeterinarians(clinicId)
            .orElseThrow(() -> new IllegalArgumentException("Clinic not found: " + clinicId));
        Veterinarian veterinarian = veterinarianRepository.findById(veterinarianId)
            .orElseThrow(() -> new IllegalArgumentException("Veterinarian not found: " + veterinarianId));
        if (veterinarian.getClinic() == null || !clinicId.equals(veterinarian.getClinic().getId())) {
            throw new IllegalStateException("Veterinarian is not assigned to this clinic: " + veterinarianId);
        }
        clinic.removeVeterinarian(veterinarian);
        return clinicRepository.save(clinic);
    }

    @Transactional
    public void closeClinic(Long clinicId) {
        Clinic clinic = clinicRepository.findByIdWithVeterinarians(clinicId)
            .orElseThrow(() -> new IllegalArgumentException("Clinic not found: " + clinicId));
        if (!clinic.getVeterinarians().isEmpty()) {
            throw new IllegalStateException("Clinic has veterinarians and cannot be closed: " + clinicId);
        }
        clinicRepository.delete(clinic);
    }

    private void requireNonBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }
}
