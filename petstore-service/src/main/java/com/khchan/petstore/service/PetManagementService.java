package com.khchan.petstore.service;

import com.khchan.petstore.domain.Breed;
import com.khchan.petstore.domain.Owner;
import com.khchan.petstore.domain.PetEntity;
import com.khchan.petstore.domain.Status;
import com.khchan.petstore.repository.BreedRepository;
import com.khchan.petstore.repository.OwnerRepository;
import com.khchan.petstore.repository.PetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PetManagementService {

    private final PetRepository petRepository;
    private final OwnerRepository ownerRepository;
    private final BreedRepository breedRepository;

    @Autowired
    public PetManagementService(PetRepository petRepository,
                                OwnerRepository ownerRepository,
                                BreedRepository breedRepository) {
        this.petRepository = petRepository;
        this.ownerRepository = ownerRepository;
        this.breedRepository = breedRepository;
    }

    @Transactional
    public PetEntity createPet(String name, Status status, Long breedId, Long ownerId) {
        requireNonBlank(name, "Pet name is required");
        PetEntity pet = new PetEntity();
        pet.setName(name);
        pet.setStatus(status == null ? Status.AVAILABLE : status);
        if (breedId != null) {
            Breed breed = breedRepository.findById(breedId)
                .orElseThrow(() -> new IllegalArgumentException("Breed not found: " + breedId));
            pet.setBreed(breed);
        }
        if (ownerId != null) {
            Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Owner not found: " + ownerId));
            owner.addPet(pet);
        }
        return petRepository.save(pet);
    }

    @Transactional
    public PetEntity updateStatus(Long petId, Status status) {
        if (status == null) {
            throw new IllegalArgumentException("Status is required");
        }
        PetEntity pet = petRepository.findById(petId)
            .orElseThrow(() -> new IllegalArgumentException("Pet not found: " + petId));
        pet.setStatus(status);
        return petRepository.save(pet);
    }

    @Transactional
    public PetEntity assignOwner(Long petId, Long ownerId) {
        PetEntity pet = petRepository.findById(petId)
            .orElseThrow(() -> new IllegalArgumentException("Pet not found: " + petId));
        Owner owner = ownerRepository.findById(ownerId)
            .orElseThrow(() -> new IllegalArgumentException("Owner not found: " + ownerId));
        if (pet.getOwner() != null && !ownerId.equals(pet.getOwner().getId())) {
            throw new IllegalStateException("Pet already assigned to another owner: " + petId);
        }
        owner.addPet(pet);
        ownerRepository.save(owner);
        return pet;
    }

    @Transactional
    public void deletePet(Long petId) {
        PetEntity pet = petRepository.findById(petId)
            .orElseThrow(() -> new IllegalArgumentException("Pet not found: " + petId));
        if (pet.getStatus() == Status.PENDING) {
            throw new IllegalStateException("Pending pets cannot be deleted: " + petId);
        }
        petRepository.delete(pet);
    }

    private void requireNonBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }
}
