package com.khchan.petstore.service;

import com.khchan.petstore.domain.*;
import com.khchan.petstore.dto.Pet;
import com.khchan.petstore.dto.Tag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class PetTransformerTest {

    private final Category dogCategory = Category.builder().id(1L).name("Dogs").build();

    private PetTransformer fixture;

    @BeforeEach
    public void setUp() {
        fixture = new PetTransformer();
    }

    @Test
    public void transformEntityToDTO() {
        PetEntity petEntity = PetEntity.builder()
            .id(1L)
            .name("Fluffy")
            .status(Status.AVAILABLE)
            .category(dogCategory)
            .media(Arrays.asList(
                Media.builder().url("url").build()
            ))
            .tags(Arrays.asList(
                TagEntity.builder().name("cute").build()
            ))
            .build();

        Pet actual = fixture.transformEntityToDTO(petEntity);

        assertEquals(Long.valueOf(1), actual.getId());
        assertEquals("Fluffy", actual.getName());
        assertEquals(Status.AVAILABLE, actual.getStatus());
        assertEquals("Dogs", actual.getCategory().getName());
        assertEquals("url", actual.getPhotoUrls().get(0));
        assertEquals("cute", actual.getTags().get(0).getName());
    }

    @Test
    public void transformDTOToEntity() {
        Pet petDTO = Pet.builder()
            .id(1L)
            .name("Fluffy")
            .status(Status.AVAILABLE)
            .category(dogCategory)
            .photoUrls(Arrays.asList("url"))
            .tags(Arrays.asList(
                Tag.builder().name("cute").build()
            ))
            .build();

        PetEntity actual = fixture.transformDTOToEntity(petDTO);

        assertEquals(Long.valueOf(1), actual.getId());
        assertEquals("Fluffy", actual.getName());
        assertEquals(Status.AVAILABLE, actual.getStatus());
        assertEquals("Dogs", actual.getCategory().getName());
        assertNull(actual.getMedia());
        assertEquals("cute", actual.getTags().get(0).getName());
    }

}
