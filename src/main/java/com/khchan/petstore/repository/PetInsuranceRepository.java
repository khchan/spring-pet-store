package com.khchan.petstore.repository;

import com.khchan.petstore.domain.InsuranceStatus;
import com.khchan.petstore.domain.PetInsurance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PetInsuranceRepository extends JpaRepository<PetInsurance, Long> {
    Optional<PetInsurance> findByPolicyNumber(String policyNumber);

    List<PetInsurance> findByProvider(String provider);

    List<PetInsurance> findByStatus(InsuranceStatus status);

    @Query("SELECT pi FROM PetInsurance pi WHERE pi.pet.id = :petId")
    Optional<PetInsurance> findByPetId(Long petId);
}
