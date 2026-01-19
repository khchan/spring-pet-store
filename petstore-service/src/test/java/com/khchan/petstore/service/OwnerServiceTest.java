package com.khchan.petstore.service;

import com.khchan.petstore.domain.Address;
import com.khchan.petstore.domain.Owner;
import com.khchan.petstore.domain.PetEntity;
import com.khchan.petstore.domain.Status;
import com.khchan.petstore.repository.OwnerRepository;
import com.khchan.petstore.repository.PetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class OwnerServiceTest {

    @Autowired
    private OwnerService ownerService;

    @Autowired
    private PetManagementService petManagementService;

    @Autowired
    private OwnerRepository ownerRepository;

    @Autowired
    private PetRepository petRepository;

    @BeforeEach
    void clearData() {
        petRepository.deleteAll();
        ownerRepository.deleteAll();
    }

    @Test
    void registerOwner_enforcesUniqueEmail() {
        Address address = new Address("1 Main", "Boston", "MA", "02110", "USA");
        ownerService.registerOwner("Ava", "Stone", "ava@example.com", "555-1010", address);

        assertThrows(IllegalArgumentException.class, () ->
            ownerService.registerOwner("Ava", "Stone", "ava@example.com", "555-1010", address));
    }

    @Test
    void addAndRemovePet_updatesOwnership() {
        Address address = new Address("2 Main", "Boston", "MA", "02110", "USA");
        Owner owner = ownerService.registerOwner("Eli", "Park", "eli@example.com", "555-1011", address);
        PetEntity pet = petManagementService.createPet("Milo", Status.AVAILABLE, null, null);

        ownerService.addPetToOwner(owner.getId(), pet.getId());
        PetEntity ownedPet = petRepository.findById(pet.getId()).orElseThrow();
        assertThat(ownedPet.getOwner()).isNotNull();

        ownerService.removePetFromOwner(owner.getId(), pet.getId());
        assertThat(petRepository.count()).isZero();
    }

    @Test
    void deleteOwner_failsWhenPetsExist() {
        Address address = new Address("3 Main", "Boston", "MA", "02110", "USA");
        Owner owner = ownerService.registerOwner("Noah", "Gray", "noah@example.com", "555-1012", address);
        petManagementService.createPet("Bella", Status.AVAILABLE, null, owner.getId());

        assertThrows(IllegalStateException.class, () -> ownerService.deleteOwner(owner.getId()));
        assertThat(ownerRepository.count()).isEqualTo(1);
    }
}
