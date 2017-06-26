package com.khchan.petstore.service;

import com.khchan.petstore.domain.PetEntity;
import com.khchan.petstore.dto.Pet;
import com.khchan.petstore.repository.PetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PetService {

    private final PetRepository petRepository;
    private final PetTransformer petTransformer;

    @Autowired
    public PetService(PetRepository petRepository, PetTransformer petTransformer) {
        this.petRepository = petRepository;
        this.petTransformer = petTransformer;
    }

    public List<Pet> findAllPets() {
        return petRepository.findAll().stream()
            .map(petTransformer::transformEntityToDTO)
            .collect(Collectors.toList());
    }

    public Pet findPet(Long id) {
        PetEntity petEntity = petRepository.findOne(id);
        return petTransformer.transformEntityToDTO(petEntity);
    }
}
