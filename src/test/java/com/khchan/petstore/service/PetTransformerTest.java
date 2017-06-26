package com.khchan.petstore.service;

import com.khchan.petstore.dto.Pet;
import com.khchan.petstore.domain.PetEntity;
import com.khchan.petstore.domain.Status;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;

public class PetTransformerTest {

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
            .build();

        Pet actual = fixture.transformEntityToDTO(petEntity);

        assertEquals(Long.valueOf(1), actual.getId());
        assertEquals("Fluffy", actual.getName());
        assertEquals(Status.AVAILABLE, actual.getStatus());
    }

    @Test
    public void transformDTOToEntity() throws Exception {
        Pet petDTO = Pet.builder()
            .id(1L)
            .name("Fluffy")
            .status(Status.AVAILABLE)
            .build();

        PetEntity actual = fixture.transformDTOToEntity(petDTO);

        assertEquals(Long.valueOf(1), actual.getId());
        assertEquals("Fluffy", actual.getName());
        assertEquals(Status.AVAILABLE, actual.getStatus());
    }

}