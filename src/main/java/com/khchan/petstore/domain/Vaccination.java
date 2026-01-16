package com.khchan.petstore.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;

/**
 * Represents a vaccination given to a pet.
 * Demonstrates cascade from Pet side with orphan removal.
 */
@Entity
@Table(name = "vaccinations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vaccination {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private PetEntity pet;

    private String vaccineName;
    private LocalDate dateAdministered;
    private LocalDate nextDueDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "administered_by")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Veterinarian administeredBy;

    @Column(length = 500)
    private String notes;

    public Vaccination(PetEntity pet, String vaccineName, LocalDate dateAdministered, LocalDate nextDueDate, Veterinarian administeredBy) {
        this.pet = pet;
        this.vaccineName = vaccineName;
        this.dateAdministered = dateAdministered;
        this.nextDueDate = nextDueDate;
        this.administeredBy = administeredBy;
    }
}
