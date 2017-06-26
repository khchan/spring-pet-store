package com.khchan.petstore.service;

import com.khchan.petstore.domain.Category;
import com.khchan.petstore.dto.Pet;
import com.khchan.petstore.domain.PetEntity;
import com.khchan.petstore.domain.Status;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;

public class PetTransformerTest {

    private final Category dogCategory = Category.builder().id(1L).name("Dogs").build();

    @InjectMocks
    private PetTransformer fixture;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void transformEntityToDTO() throws Exception {
        PetEntity petEntity = PetEntity.builder()
            .id(1L)
            .name("Fluffy")
            .status(Status.AVAILABLE)
            .category(dogCategory)
            .build();

        Pet actual = fixture.transformEntityToDTO(petEntity);

        assertEquals(Long.valueOf(1), actual.getId());
        assertEquals("Fluffy", actual.getName());
        assertEquals(Status.AVAILABLE, actual.getStatus());
        assertEquals("Dogs", actual.getCategory().getName());
    }

    @Test
    public void transformDTOToEntity() throws Exception {
        Pet petDTO = Pet.builder()
            .id(1L)
            .name("Fluffy")
            .status(Status.AVAILABLE)
            .category(dogCategory)
            .build();

        PetEntity actual = fixture.transformDTOToEntity(petDTO);

        assertEquals(Long.valueOf(1), actual.getId());
        assertEquals("Fluffy", actual.getName());
        assertEquals(Status.AVAILABLE, actual.getStatus());
        assertEquals("Dogs", actual.getCategory().getName());
    }

}