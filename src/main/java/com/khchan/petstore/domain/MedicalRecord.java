package com.khchan.petstore.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;

/**
 * Represents a medical record for a pet.
 * Demonstrates cascade from Pet side - when pet is deleted, medical records are deleted.
 */
@Entity
@Table(name = "medical_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicalRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id", nullable = false)
    @ToString.Exclude
    private PetEntity pet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "veterinarian_id")
    @ToString.Exclude
    private Veterinarian veterinarian;

    private LocalDate visitDate;

    @Column(length = 500)
    private String diagnosis;

    @Column(length = 1000)
    private String treatment;

    @Column(length = 1000)
    private String notes;

    private Double weight; // in kg

    public MedicalRecord(PetEntity pet, Veterinarian veterinarian, LocalDate visitDate, String diagnosis, String treatment) {
        this.pet = pet;
        this.veterinarian = veterinarian;
        this.visitDate = visitDate;
        this.diagnosis = diagnosis;
        this.treatment = treatment;
    }
}
