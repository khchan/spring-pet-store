package com.khchan.petstore.repository;

import com.khchan.petstore.domain.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {
    List<MedicalRecord> findByPetId(Long petId);

    List<MedicalRecord> findByVeterinarianId(Long veterinarianId);

    List<MedicalRecord> findByPetIdOrderByVisitDateDesc(Long petId);

    @Query("SELECT mr FROM MedicalRecord mr WHERE mr.pet.id = :petId AND mr.visitDate BETWEEN :startDate AND :endDate")
    List<MedicalRecord> findByPetIdAndDateRange(Long petId, LocalDate startDate, LocalDate endDate);
}
