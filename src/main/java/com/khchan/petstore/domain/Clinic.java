package com.khchan.petstore.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a veterinary clinic.
 * Demonstrates OneToMany relationship with Veterinarian.
 */
@Entity
@Table(name = "clinics")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Clinic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String phone;

    @Embedded
    private Address address;

    /**
     * CASCADE.PERSIST and MERGE only - removing a clinic doesn't delete veterinarians.
     * They should be reassigned to another clinic instead.
     */
    @OneToMany(mappedBy = "clinic", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Veterinarian> veterinarians = new ArrayList<>();

    public Clinic(String name, String phone, Address address) {
        this.name = name;
        this.phone = phone;
        this.address = address;
    }

    // Helper methods
    public void addVeterinarian(Veterinarian vet) {
        veterinarians.add(vet);
        vet.setClinic(this);
    }

    public void removeVeterinarian(Veterinarian vet) {
        veterinarians.remove(vet);
        vet.setClinic(null);
    }
}
