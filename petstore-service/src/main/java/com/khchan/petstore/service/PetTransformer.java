package com.khchan.petstore.service;

import com.khchan.petstore.domain.Media;
import com.khchan.petstore.domain.TagEntity;
import com.khchan.petstore.dto.Pet;
import com.khchan.petstore.domain.PetEntity;
import com.khchan.petstore.dto.Tag;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class PetTransformer {
    Pet transformEntityToDTO(PetEntity petEntity) {
        Pet.PetBuilder petBuilder = Pet.builder()
            .id(petEntity.getId())
            .name(petEntity.getName())
            .status(petEntity.getStatus())
            .category(petEntity.getCategory());

        if (petEntity.getMedia() != null) {
            petBuilder = petBuilder
                .photoUrls(petEntity.getMedia().stream()
                    .map(Media::getUrl)
                    .collect(Collectors.toList()));
        }

        if (petEntity.getTags() != null) {
            petBuilder = petBuilder
                .tags(petEntity.getTags().stream()
                    .map(tag -> Tag.builder()
                        .id(tag.getId())
                        .name(tag.getName())
                        .build())
                    .collect(Collectors.toList()));
        }

        return petBuilder.build();
    }

    PetEntity transformDTOToEntity(Pet petDTO) {
        PetEntity.PetEntityBuilder petEntityBuilder = PetEntity.builder()
            .id(petDTO.getId())
            .name(petDTO.getName())
            .status(petDTO.getStatus())
            .category(petDTO.getCategory());

        if (petDTO.getTags() != null) {
            petEntityBuilder = petEntityBuilder
                .tags(petDTO.getTags().stream()
                    .map(tag -> TagEntity.builder()
                        .id(tag.getId())
                        .name(tag.getName())
                        .build())
                    .collect(Collectors.toList()));
        }

        return petEntityBuilder.build();
    }
}
