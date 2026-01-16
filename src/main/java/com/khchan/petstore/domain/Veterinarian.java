package com.khchan.petstore.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a veterinarian who works at a clinic and treats pets.
 */
@Entity
@Table(name = "veterinarians")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Veterinarian {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;
    private String specialty;
    private String licenseNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clinic_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Clinic clinic;

    /**
     * No cascade here - appointments are managed separately.
     */
    @OneToMany(mappedBy = "veterinarian", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Appointment> appointments = new ArrayList<>();

    @OneToMany(mappedBy = "veterinarian", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<MedicalRecord> medicalRecords = new ArrayList<>();

    @OneToMany(mappedBy = "administeredBy", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Vaccination> vaccinationsAdministered = new ArrayList<>();

    public Veterinarian(String firstName, String lastName, String specialty, String licenseNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.specialty = specialty;
        this.licenseNumber = licenseNumber;
    }
}
