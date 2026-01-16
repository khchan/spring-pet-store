package com.khchan.petstore.repository;

import com.khchan.petstore.domain.Vaccination;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface VaccinationRepository extends JpaRepository<Vaccination, Long> {
    List<Vaccination> findByPetId(Long petId);

    List<Vaccination> findByVaccineName(String vaccineName);

    @Query("SELECT v FROM Vaccination v WHERE v.nextDueDate < :date")
    List<Vaccination> findOverdueVaccinations(LocalDate date);

    @Query("SELECT v FROM Vaccination v WHERE v.pet.id = :petId ORDER BY v.dateAdministered DESC")
    List<Vaccination> findByPetIdOrderByDateDesc(Long petId);
}
