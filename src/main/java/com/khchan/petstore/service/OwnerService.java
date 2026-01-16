package com.khchan.petstore.service;

import com.khchan.petstore.domain.Address;
import com.khchan.petstore.domain.Owner;
import com.khchan.petstore.domain.PetEntity;
import com.khchan.petstore.repository.OwnerRepository;
import com.khchan.petstore.repository.PetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OwnerService {

    private final OwnerRepository ownerRepository;
    private final PetRepository petRepository;

    @Autowired
    public OwnerService(OwnerRepository ownerRepository, PetRepository petRepository) {
        this.ownerRepository = ownerRepository;
        this.petRepository = petRepository;
    }

    @Transactional
    public Owner registerOwner(String firstName, String lastName, String email, String phone, Address address) {
        requireNonBlank(email, "Owner email is required");
        if (ownerRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Owner email already exists: " + email);
        }
        Owner owner = new Owner(firstName, lastName, email, phone, address);
        return ownerRepository.save(owner);
    }

    @Transactional(readOnly = true)
    public Owner findOwner(Long ownerId) {
        return ownerRepository.findById(ownerId)
            .orElseThrow(() -> new IllegalArgumentException("Owner not found: " + ownerId));
    }

    @Transactional(readOnly = true)
    public List<Owner> findOwnersByLastName(String lastName) {
        requireNonBlank(lastName, "Last name is required");
        return ownerRepository.findByLastName(lastName);
    }

    @Transactional
    public Owner updateContact(Long ownerId, String email, String phone) {
        Owner owner = findOwner(ownerId);
        if (email != null && !email.equals(owner.getEmail())) {
            if (ownerRepository.findByEmail(email).isPresent()) {
                throw new IllegalArgumentException("Owner email already exists: " + email);
            }
            owner.setEmail(email);
        }
        if (phone != null) {
            owner.setPhone(phone);
        }
        return ownerRepository.save(owner);
    }

    @Transactional
    public PetEntity addPetToOwner(Long ownerId, Long petId) {
        Owner owner = findOwner(ownerId);
        PetEntity pet = petRepository.findById(petId)
            .orElseThrow(() -> new IllegalArgumentException("Pet not found: " + petId));
        if (pet.getOwner() != null && !pet.getOwner().getId().equals(ownerId)) {
            throw new IllegalStateException("Pet already assigned to another owner: " + petId);
        }
        owner.addPet(pet);
        ownerRepository.save(owner);
        return pet;
    }

    @Transactional
    public void removePetFromOwner(Long ownerId, Long petId) {
        Owner owner = ownerRepository.findByIdWithPets(ownerId)
            .orElseThrow(() -> new IllegalArgumentException("Owner not found: " + ownerId));
        PetEntity pet = petRepository.findById(petId)
            .orElseThrow(() -> new IllegalArgumentException("Pet not found: " + petId));
        if (pet.getOwner() == null || !ownerId.equals(pet.getOwner().getId())) {
            throw new IllegalStateException("Pet is not owned by this owner: " + petId);
        }
        owner.removePet(pet);
        ownerRepository.save(owner);
    }

    @Transactional
    public void deleteOwner(Long ownerId) {
        Owner owner = ownerRepository.findByIdWithPets(ownerId)
            .orElseThrow(() -> new IllegalArgumentException("Owner not found: " + ownerId));
        if (!owner.getPets().isEmpty()) {
            throw new IllegalStateException("Owner has pets and cannot be deleted: " + ownerId);
        }
        ownerRepository.delete(owner);
    }

    private void requireNonBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }
}
