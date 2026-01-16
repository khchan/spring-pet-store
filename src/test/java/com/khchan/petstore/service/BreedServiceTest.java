package com.khchan.petstore.service;

import com.khchan.petstore.domain.Breed;
import com.khchan.petstore.domain.Size;
import com.khchan.petstore.repository.BreedRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class BreedServiceTest {

    @Autowired
    private BreedService breedService;

    @Autowired
    private BreedRepository breedRepository;

    @BeforeEach
    void clearData() {
        breedRepository.deleteAll();
    }

    @Test
    void createBreed_enforcesUniqueName() {
        breedService.createBreed("Husky", "Snow dog", Size.MEDIUM);

        assertThrows(IllegalArgumentException.class,
            () -> breedService.createBreed("Husky", "Duplicate", Size.MEDIUM));
        assertThat(breedRepository.count()).isEqualTo(1);
    }

    @Test
    void updateBreed_updatesFields() {
        Breed breed = breedService.createBreed("Beagle", "Hound", Size.SMALL);

        Breed updated = breedService.updateBreed(breed.getId(), "Updated", Size.MEDIUM);

        assertThat(updated.getDescription()).isEqualTo("Updated");
        assertThat(updated.getSize()).isEqualTo(Size.MEDIUM);
    }

    @Test
    void deleteBreed_removesEntity() {
        Breed breed = breedService.createBreed("Corgi", "Short legs", Size.SMALL);

        breedService.deleteBreed(breed.getId());

        assertThat(breedRepository.count()).isZero();
    }
}
