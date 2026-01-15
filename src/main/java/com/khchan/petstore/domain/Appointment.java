package com.khchan.petstore.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Represents an appointment between a pet and a veterinarian.
 * This acts as a join entity for the many-to-many relationship between Pet and Veterinarian,
 * with additional attributes (dateTime, reason, notes).
 */
@Entity
@Table(name = "appointments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id", nullable = false)
    @ToString.Exclude
    private PetEntity pet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "veterinarian_id", nullable = false)
    @ToString.Exclude
    private Veterinarian veterinarian;

    private LocalDateTime dateTime;
    private String reason;

    @Column(length = 1000)
    private String notes;

    @Enumerated(EnumType.STRING)
    private AppointmentStatus status;

    public Appointment(PetEntity pet, Veterinarian veterinarian, LocalDateTime dateTime, String reason, AppointmentStatus status) {
        this.pet = pet;
        this.veterinarian = veterinarian;
        this.dateTime = dateTime;
        this.reason = reason;
        this.status = status;
    }
}
