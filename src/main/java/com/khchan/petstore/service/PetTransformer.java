package com.khchan.petstore.service;

import com.khchan.petstore.dto.Pet;
import com.khchan.petstore.domain.PetEntity;
import org.springframework.stereotype.Service;

@Service
public class PetTransformer {
    Pet transformEntityToDTO(PetEntity petEntity) {
        return Pet.builder()
            .id(petEntity.getId())
            .name(petEntity.getName())
            .status(petEntity.getStatus())
            .category(petEntity.getCategory())
            .build();
    }

    PetEntity transformDTOToEntity(Pet petDTO) {
        return PetEntity.builder()
            .id(petDTO.getId())
            .name(petDTO.getName())
            .status(petDTO.getStatus())
            .category(petDTO.getCategory())
            .build();
    }
}
