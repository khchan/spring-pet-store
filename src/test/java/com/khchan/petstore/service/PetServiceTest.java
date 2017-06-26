package com.khchan.petstore.service;

import com.khchan.petstore.dto.Pet;
import com.khchan.petstore.domain.PetEntity;
import com.khchan.petstore.repository.PetRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class PetServiceTest {

    @Mock
    private PetRepository petRepository;

    @Mock
    private PetTransformer petTransformer;

    @InjectMocks
    private PetService fixture;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        doReturn(mock(PetEntity.class)).when(petTransformer).transformDTOToEntity(any(Pet.class));
        doReturn(mock(Pet.class)).when(petTransformer).transformEntityToDTO(any(PetEntity.class));
    }

    @Test
    public void findAllPets() throws Exception {
        fixture.findAllPets();

        verify(petRepository).findAll();
    }

    @Test
    public void findPet() throws Exception {
        Long petId = 1L;

        fixture.findPet(petId);

        verify(petRepository).findOne(eq(petId));
    }

}