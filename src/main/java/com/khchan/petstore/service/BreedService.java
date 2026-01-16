package com.khchan.petstore.service;

import com.khchan.petstore.domain.Breed;
import com.khchan.petstore.domain.Size;
import com.khchan.petstore.repository.BreedRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BreedService {

    private final BreedRepository breedRepository;

    @Autowired
    public BreedService(BreedRepository breedRepository) {
        this.breedRepository = breedRepository;
    }

    @Transactional
    public Breed createBreed(String name, String description, Size size) {
        requireNonBlank(name, "Breed name is required");
        if (breedRepository.findByName(name).isPresent()) {
            throw new IllegalArgumentException("Breed already exists: " + name);
        }
        Breed breed = new Breed(name, description, size);
        return breedRepository.save(breed);
    }

    @Transactional(readOnly = true)
    public Breed findBreed(Long breedId) {
        return breedRepository.findById(breedId)
            .orElseThrow(() -> new IllegalArgumentException("Breed not found: " + breedId));
    }

    @Transactional(readOnly = true)
    public List<Breed> findBreedsBySize(Size size) {
        if (size == null) {
            throw new IllegalArgumentException("Size is required");
        }
        return breedRepository.findBySize(size);
    }

    @Transactional
    public Breed updateBreed(Long breedId, String description, Size size) {
        Breed breed = findBreed(breedId);
        if (description != null) {
            breed.setDescription(description);
        }
        if (size != null) {
            breed.setSize(size);
        }
        return breedRepository.save(breed);
    }

    @Transactional
    public void deleteBreed(Long breedId) {
        Breed breed = findBreed(breedId);
        breedRepository.delete(breed);
    }

    private void requireNonBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }
}
