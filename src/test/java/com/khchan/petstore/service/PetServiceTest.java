package com.khchan.petstore.service;

import com.khchan.petstore.domain.Status;
import com.khchan.petstore.dto.Pet;
import com.khchan.petstore.domain.PetEntity;
import com.khchan.petstore.repository.PetRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

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

    @Test
    public void findAllPets() {
        fixture.findAllPets();

        verify(petRepository).findAll();
    }

    @Test
    public void findPet() {
        Long petId = 1L;
        PetEntity mockEntity = mock(PetEntity.class);
        doReturn(Optional.of(mockEntity)).when(petRepository).findById(eq(petId));
        doReturn(mock(Pet.class)).when(petTransformer).transformEntityToDTO(eq(mockEntity));

        fixture.findPet(petId);

        verify(petRepository).findById(eq(petId));
        verify(petTransformer).transformEntityToDTO(eq(mockEntity));
    }

    @Test
    public void savePet() {
        Pet newPetDTO = createPetDTO();
        PetEntity mockEntity = mock(PetEntity.class);
        PetEntity savedEntity = mock(PetEntity.class);
        doReturn(mockEntity).when(petTransformer).transformDTOToEntity(eq(newPetDTO));
        doReturn(savedEntity).when(petRepository).save(eq(mockEntity));
        doReturn(mock(Pet.class)).when(petTransformer).transformEntityToDTO(eq(savedEntity));

        fixture.savePet(newPetDTO);

        verify(petRepository).save(eq(mockEntity));
        verify(petTransformer).transformEntityToDTO(eq(savedEntity));
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