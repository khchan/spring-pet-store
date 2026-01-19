package com.khchan.petstore.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a pet owner.
 * Demonstrates CASCADE.ALL and orphanRemoval with OneToMany relationship to Pet.
 * When an owner is deleted, all their pets are deleted (cascade).
 * When a pet is removed from the owner's collection, it's deleted (orphanRemoval).
 */
@Entity
@Table(name = "owners")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Owner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;
    private String email;
    private String phone;

    @Embedded
    private Address address;

    /**
     * CASCADE.ALL: All operations (PERSIST, MERGE, REMOVE, REFRESH, DETACH) cascade to pets.
     * orphanRemoval = true: If a pet is removed from this collection, it will be deleted.
     */
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<PetEntity> pets = new ArrayList<>();

    public Owner(String firstName, String lastName, String email, String phone, Address address) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.address = address;
    }

    // Helper methods to maintain bidirectional relationship
    public void addPet(PetEntity pet) {
        pets.add(pet);
        pet.setOwner(this);
    }

    public void removePet(PetEntity pet) {
        pets.remove(pet);
        pet.setOwner(null);
    }
}
