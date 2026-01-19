package com.khchan.petstore.service;

import com.khchan.petstore.domain.Address;
import com.khchan.petstore.domain.Breed;
import com.khchan.petstore.domain.Owner;
import com.khchan.petstore.domain.PetEntity;
import com.khchan.petstore.domain.Size;
import com.khchan.petstore.domain.Status;
import com.khchan.petstore.repository.BreedRepository;
import com.khchan.petstore.repository.OwnerRepository;
import com.khchan.petstore.repository.PetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class PetManagementServiceTest {

    @Autowired
    private PetManagementService petManagementService;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private OwnerRepository ownerRepository;

    @Autowired
    private BreedRepository breedRepository;

    @BeforeEach
    void clearData() {
        petRepository.deleteAll();
        ownerRepository.deleteAll();
        breedRepository.deleteAll();
    }

    @Test
    void createPet_assignsOwnerAndBreed() {
        Owner owner = ownerRepository.save(new Owner(
            "Mia", "Reed", "mia@example.com", "555-2000",
            new Address("1 Park", "Boston", "MA", "02110", "USA")
        ));
        Breed breed = breedRepository.save(new Breed("Retriever", "Friendly", Size.LARGE));

        PetEntity pet = petManagementService.createPet("Scout", Status.AVAILABLE, breed.getId(), owner.getId());

        PetEntity stored = petRepository.findById(pet.getId()).orElseThrow();
        assertThat(stored.getOwner()).isNotNull();
        assertThat(stored.getBreed()).isNotNull();
    }

    @Test
    void updateStatus_changesPetStatus() {
        PetEntity pet = petManagementService.createPet("Buddy", Status.AVAILABLE, null, null);

        PetEntity updated = petManagementService.updateStatus(pet.getId(), Status.SOLD);

        assertThat(updated.getStatus()).isEqualTo(Status.SOLD);
    }

    @Test
    void deletePet_rejectsPendingPet() {
        PetEntity pet = petManagementService.createPet("Ruby", Status.PENDING, null, null);

        assertThrows(IllegalStateException.class, () -> petManagementService.deletePet(pet.getId()));
        assertThat(petRepository.count()).isEqualTo(1);
    }
}
