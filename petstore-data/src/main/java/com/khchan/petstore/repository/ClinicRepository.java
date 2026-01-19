package com.khchan.petstore.repository;

import com.khchan.petstore.domain.Clinic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClinicRepository extends JpaRepository<Clinic, Long> {
    Optional<Clinic> findByName(String name);

    List<Clinic> findByAddress_City(String city);

    @Query("SELECT c FROM Clinic c LEFT JOIN FETCH c.veterinarians WHERE c.id = :id")
    Optional<Clinic> findByIdWithVeterinarians(Long id);
}
