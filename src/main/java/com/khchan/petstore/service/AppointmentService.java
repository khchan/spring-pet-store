package com.khchan.petstore.service;

import com.khchan.petstore.domain.Appointment;
import com.khchan.petstore.domain.AppointmentStatus;
import com.khchan.petstore.domain.PetEntity;
import com.khchan.petstore.domain.Status;
import com.khchan.petstore.domain.Veterinarian;
import com.khchan.petstore.repository.AppointmentRepository;
import com.khchan.petstore.repository.PetRepository;
import com.khchan.petstore.repository.VeterinarianRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PetRepository petRepository;
    private final VeterinarianRepository veterinarianRepository;

    @Autowired
    public AppointmentService(AppointmentRepository appointmentRepository,
                              PetRepository petRepository,
                              VeterinarianRepository veterinarianRepository) {
        this.appointmentRepository = appointmentRepository;
        this.petRepository = petRepository;
        this.veterinarianRepository = veterinarianRepository;
    }

    @Transactional
    public Appointment scheduleAppointment(Long petId, Long veterinarianId, LocalDateTime dateTime, String reason) {
        if (dateTime == null || !dateTime.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Appointment time must be in the future");
        }
        PetEntity pet = petRepository.findById(petId)
            .orElseThrow(() -> new IllegalArgumentException("Pet not found: " + petId));
        if (pet.getStatus() == Status.SOLD) {
            throw new IllegalStateException("Cannot schedule appointment for a sold pet: " + petId);
        }
        Veterinarian veterinarian = veterinarianRepository.findById(veterinarianId)
            .orElseThrow(() -> new IllegalArgumentException("Veterinarian not found: " + veterinarianId));
        Appointment appointment = new Appointment(pet, veterinarian, dateTime, reason, AppointmentStatus.SCHEDULED);
        return appointmentRepository.save(appointment);
    }

    @Transactional(readOnly = true)
    public Appointment findAppointment(Long appointmentId) {
        return appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new IllegalArgumentException("Appointment not found: " + appointmentId));
    }

    @Transactional(readOnly = true)
    public List<Appointment> findAppointmentsForPet(Long petId) {
        return appointmentRepository.findByPetId(petId);
    }

    @Transactional(readOnly = true)
    public List<Appointment> findAppointmentsForVeterinarian(Long veterinarianId) {
        return appointmentRepository.findByVeterinarianId(veterinarianId);
    }

    @Transactional
    public Appointment rescheduleAppointment(Long appointmentId, LocalDateTime newDateTime) {
        if (newDateTime == null || !newDateTime.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Appointment time must be in the future");
        }
        Appointment appointment = findAppointment(appointmentId);
        if (appointment.getStatus() == AppointmentStatus.CANCELLED
            || appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new IllegalStateException("Appointment cannot be rescheduled in status: " + appointment.getStatus());
        }
        appointment.setDateTime(newDateTime);
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        return appointmentRepository.save(appointment);
    }

    @Transactional
    public Appointment completeAppointment(Long appointmentId, String notes) {
        Appointment appointment = findAppointment(appointmentId);
        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new IllegalStateException("Cancelled appointment cannot be completed");
        }
        appointment.setStatus(AppointmentStatus.COMPLETED);
        if (notes != null) {
            appointment.setNotes(notes);
        }
        return appointmentRepository.save(appointment);
    }

    @Transactional
    public Appointment cancelAppointment(Long appointmentId, String notes) {
        Appointment appointment = findAppointment(appointmentId);
        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new IllegalStateException("Completed appointment cannot be cancelled");
        }
        appointment.setStatus(AppointmentStatus.CANCELLED);
        if (notes != null) {
            appointment.setNotes(notes);
        }
        return appointmentRepository.save(appointment);
    }
}
