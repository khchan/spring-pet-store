package com.khchan.petstore.service;

import com.khchan.petstore.domain.Status;
import com.khchan.petstore.dto.Pet;
import com.khchan.petstore.domain.PetEntity;
import com.khchan.petstore.repository.PetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class PetServiceTest {

    @Mock
    private PetRepository petRepository;

    @Mock
    private PetTransformer petTransformer;

    @InjectMocks
    private PetService fixture;

    @BeforeEach
    public void setUp() throws Exception {
        doReturn(mock(PetEntity.class)).when(petTransformer).transformDTOToEntity(any(Pet.class));
        doReturn(mock(Pet.class)).when(petTransformer).transformEntityToDTO(any(PetEntity.class));
    }

    @Test
    public void findAllPets() {
        fixture.findAllPets();

        verify(petRepository).findAll();
    }

    @Test
    public void findPet() {
        Long petId = 1L;

        fixture.findPet(petId);

        verify(petRepository).findById(eq(petId));
    }

    @Test
    public void savePet() {
        Pet newPetDTO = createPetDTO();

        fixture.savePet(newPetDTO);

        verify(petRepository).save(any(PetEntity.class));
    }

    @Test
    public void removePet() {
        Long petId = 1L;

        fixture.removePet(petId);

        verify(petRepository).deleteById(eq(petId));
    }

    private Pet createPetDTO() {
        return Pet.builder()
            .id(1L)
            .name("Fluffy")
            .status(Status.AVAILABLE).build();
    }

}