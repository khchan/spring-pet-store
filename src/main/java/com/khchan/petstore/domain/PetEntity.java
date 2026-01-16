package com.khchan.petstore.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Main Pet entity demonstrating various cascade types and relationship patterns.
 *
 * Cascade Configurations:
 * - Owner -> Pet: CASCADE.ALL + orphanRemoval (from Owner side)
 * - Pet -> MedicalRecords: CASCADE.ALL + orphanRemoval
 * - Pet -> Vaccinations: CASCADE.ALL + orphanRemoval
 * - Pet -> Insurance: CASCADE.ALL + orphanRemoval
 * - Pet -> Appointments: CASCADE.PERSIST, MERGE
 * - Pet -> Tags: CASCADE.MERGE (existing)
 * - Pet -> Breed: No cascade (Breed is a reference entity)
 */
@Entity
@Table(name = "pets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PetEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Category category;

    /**
     * ManyToOne to Owner - no cascade from this side.
     * Owner controls the cascade behavior (CASCADE.ALL + orphanRemoval).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Owner owner;

    /**
     * ManyToOne to Breed - no cascade (Breed is a shared reference entity).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "breed_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Breed breed;

    /**
     * OneToOne with Insurance - CASCADE.ALL and orphanRemoval.
     * If pet is deleted, insurance is deleted.
     * If insurance is removed from pet, it's deleted.
     */
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "insurance_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private PetInsurance insurance;

    @OneToMany(fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Media> media = new ArrayList<>();

    @ManyToMany(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    @JoinTable(name = "pet_tags",
        joinColumns = @JoinColumn(name = "pet_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id"))
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<TagEntity> tags = new ArrayList<>();

    /**
     * OneToMany to Appointments - CASCADE.PERSIST and MERGE only.
     * Appointments involve veterinarians, so we don't want to delete them automatically.
     */
    @OneToMany(mappedBy = "pet", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Appointment> appointments = new ArrayList<>();

    /**
     * OneToMany to MedicalRecords - CASCADE.ALL and orphanRemoval.
     * Medical records are owned by the pet and should be deleted with it.
     */
    @OneToMany(mappedBy = "pet", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<MedicalRecord> medicalRecords = new ArrayList<>();

    /**
     * OneToMany to Vaccinations - CASCADE.ALL and orphanRemoval.
     * Vaccination records are owned by the pet and should be deleted with it.
     */
    @OneToMany(mappedBy = "pet", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Vaccination> vaccinations = new ArrayList<>();

    // Helper methods to maintain bidirectional relationships
    public void addMedicalRecord(MedicalRecord record) {
        medicalRecords.add(record);
        record.setPet(this);
    }

    public void removeMedicalRecord(MedicalRecord record) {
        medicalRecords.remove(record);
        record.setPet(null);
    }

    public void addVaccination(Vaccination vaccination) {
        vaccinations.add(vaccination);
        vaccination.setPet(this);
    }

    public void removeVaccination(Vaccination vaccination) {
        vaccinations.remove(vaccination);
        vaccination.setPet(null);
    }

    public void addAppointment(Appointment appointment) {
        appointments.add(appointment);
        appointment.setPet(this);
    }

    public void removeAppointment(Appointment appointment) {
        appointments.remove(appointment);
        appointment.setPet(null);
    }

    public void setInsurance(PetInsurance insurance) {
        this.insurance = insurance;
        if (insurance != null && insurance.getPet() != this) {
            insurance.setPet(this);
        }
    }
}
