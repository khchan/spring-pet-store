package com.khchan.petstore.repository;

import com.khchan.petstore.domain.Veterinarian;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VeterinarianRepository extends JpaRepository<Veterinarian, Long> {
    Optional<Veterinarian> findByLicenseNumber(String licenseNumber);

    List<Veterinarian> findBySpecialty(String specialty);

    @Query("SELECT v FROM Veterinarian v WHERE v.clinic.id = :clinicId")
    List<Veterinarian> findByClinicId(Long clinicId);
}
