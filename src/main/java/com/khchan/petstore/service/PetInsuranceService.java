package com.khchan.petstore.service;

import com.khchan.petstore.domain.InsuranceStatus;
import com.khchan.petstore.domain.PetEntity;
import com.khchan.petstore.domain.PetInsurance;
import com.khchan.petstore.repository.PetInsuranceRepository;
import com.khchan.petstore.repository.PetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class PetInsuranceService {

    private final PetInsuranceRepository petInsuranceRepository;
    private final PetRepository petRepository;

    @Autowired
    public PetInsuranceService(PetInsuranceRepository petInsuranceRepository, PetRepository petRepository) {
        this.petInsuranceRepository = petInsuranceRepository;
        this.petRepository = petRepository;
    }

    @Transactional
    public PetInsurance enrollPet(Long petId,
                                  String policyNumber,
                                  String provider,
                                  BigDecimal coverageAmount,
                                  BigDecimal monthlyPremium,
                                  LocalDate startDate,
                                  LocalDate endDate) {
        requireNonBlank(policyNumber, "Policy number is required");
        requireNonBlank(provider, "Provider is required");
        if (petInsuranceRepository.findByPolicyNumber(policyNumber).isPresent()) {
            throw new IllegalArgumentException("Policy number already exists: " + policyNumber);
        }
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Policy start and end dates are invalid");
        }
        if (coverageAmount != null && coverageAmount.signum() <= 0) {
            throw new IllegalArgumentException("Coverage amount must be positive");
        }
        if (monthlyPremium != null && monthlyPremium.signum() < 0) {
            throw new IllegalArgumentException("Monthly premium cannot be negative");
        }
        PetEntity pet = petRepository.findById(petId)
            .orElseThrow(() -> new IllegalArgumentException("Pet not found: " + petId));
        if (pet.getInsurance() != null) {
            throw new IllegalStateException("Pet already has insurance: " + petId);
        }
        PetInsurance insurance = new PetInsurance(
            policyNumber,
            provider,
            coverageAmount,
            monthlyPremium,
            startDate,
            endDate,
            InsuranceStatus.ACTIVE
        );
        pet.setInsurance(insurance);
        petRepository.save(pet);
        return insurance;
    }

    @Transactional(readOnly = true)
    public PetInsurance findInsurance(Long insuranceId) {
        return petInsuranceRepository.findById(insuranceId)
            .orElseThrow(() -> new IllegalArgumentException("Insurance not found: " + insuranceId));
    }

    @Transactional(readOnly = true)
    public PetInsurance findInsuranceForPet(Long petId) {
        return petInsuranceRepository.findByPetId(petId)
            .orElseThrow(() -> new IllegalArgumentException("Insurance not found for pet: " + petId));
    }

    @Transactional
    public PetInsurance updateStatus(Long insuranceId, InsuranceStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Status is required");
        }
        PetInsurance insurance = findInsurance(insuranceId);
        insurance.setStatus(status);
        return petInsuranceRepository.save(insurance);
    }

    @Transactional
    public PetInsurance cancelPolicy(Long insuranceId) {
        PetInsurance insurance = findInsurance(insuranceId);
        insurance.setStatus(InsuranceStatus.CANCELLED);
        insurance.setEndDate(LocalDate.now());
        return petInsuranceRepository.save(insurance);
    }

    private void requireNonBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }
}
