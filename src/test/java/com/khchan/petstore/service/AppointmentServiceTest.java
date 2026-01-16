package com.khchan.petstore.service;

import com.khchan.petstore.domain.Appointment;
import com.khchan.petstore.domain.AppointmentStatus;
import com.khchan.petstore.domain.PetEntity;
import com.khchan.petstore.domain.Status;
import com.khchan.petstore.domain.Veterinarian;
import com.khchan.petstore.repository.AppointmentRepository;
import com.khchan.petstore.repository.PetRepository;
import com.khchan.petstore.repository.VeterinarianRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class AppointmentServiceTest {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private PetManagementService petManagementService;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private VeterinarianRepository veterinarianRepository;

    @BeforeEach
    void clearData() {
        appointmentRepository.deleteAll();
        petRepository.deleteAll();
        veterinarianRepository.deleteAll();
    }

    @Test
    void scheduleAppointment_persistsScheduledAppointment() {
        PetEntity pet = petManagementService.createPet("Clover", Status.AVAILABLE, null, null);
        Veterinarian vet = veterinarianRepository.save(new Veterinarian("Ivy", "Ng", "General", "LIC-APPT-1"));

        Appointment appointment = appointmentService.scheduleAppointment(
            pet.getId(),
            vet.getId(),
            LocalDateTime.now().plusDays(1),
            "Checkup"
        );

        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
        assertThat(appointmentRepository.count()).isEqualTo(1);
    }

    @Test
    void rescheduleAppointment_rejectsPastDates() {
        PetEntity pet = petManagementService.createPet("Hazel", Status.AVAILABLE, null, null);
        Veterinarian vet = veterinarianRepository.save(new Veterinarian("Ivy", "Ng", "General", "LIC-APPT-2"));
        Appointment appointment = appointmentService.scheduleAppointment(
            pet.getId(),
            vet.getId(),
            LocalDateTime.now().plusDays(2),
            "Follow-up"
        );

        assertThrows(IllegalArgumentException.class, () ->
            appointmentService.rescheduleAppointment(appointment.getId(), LocalDateTime.now().minusDays(1)));
    }
}
