package com.khchan.petstore.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

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
    @ToString.Exclude
    private Clinic clinic;

    /**
     * No cascade here - appointments are managed separately.
     */
    @OneToMany(mappedBy = "veterinarian")
    @ToString.Exclude
    private List<Appointment> appointments = new ArrayList<>();

    @OneToMany(mappedBy = "veterinarian")
    @ToString.Exclude
    private List<MedicalRecord> medicalRecords = new ArrayList<>();

    @OneToMany(mappedBy = "administeredBy")
    @ToString.Exclude
    private List<Vaccination> vaccinationsAdministered = new ArrayList<>();

    public Veterinarian(String firstName, String lastName, String specialty, String licenseNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.specialty = specialty;
        this.licenseNumber = licenseNumber;
    }
}
