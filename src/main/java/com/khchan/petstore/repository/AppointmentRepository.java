package com.khchan.petstore.repository;

import com.khchan.petstore.domain.Appointment;
import com.khchan.petstore.domain.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByPetId(Long petId);

    List<Appointment> findByVeterinarianId(Long veterinarianId);

    List<Appointment> findByStatus(AppointmentStatus status);

    @Query("SELECT a FROM Appointment a WHERE a.veterinarian.id = :vetId AND a.dateTime BETWEEN :start AND :end")
    List<Appointment> findByVeterinarianIdAndDateTimeBetween(Long vetId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT a FROM Appointment a JOIN FETCH a.pet JOIN FETCH a.veterinarian WHERE a.id = :id")
    Appointment findByIdWithDetails(Long id);
}
