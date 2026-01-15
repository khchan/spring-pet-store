package com.khchan.petstore.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.khchan.petstore.domain.Status;
import com.khchan.petstore.dto.Pet;
import com.khchan.petstore.service.PetService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PetController.class)
public class PetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PetService petService;

    @Test
    public void findAllPets() throws Exception {
        List<Pet> pets = Arrays.asList(
            createPet(1L, "Fluffy"),
            createPet(2L, "Spot")
        );
        doReturn(pets).when(petService).findAllPets();

        mockMvc.perform(get("/pets"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].name").value("Fluffy"))
            .andExpect(jsonPath("$[1].id").value(2))
            .andExpect(jsonPath("$[1].name").value("Spot"));

        verify(petService).findAllPets();
    }

    @Test
    public void findPet() throws Exception {
        Long petId = 1L;
        Pet pet = createPet(petId, "Fluffy");
        doReturn(pet).when(petService).findPet(eq(petId));

        mockMvc.perform(get("/pet/{id}", petId))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Fluffy"));

        verify(petService).findPet(eq(petId));
    }

    @Test
    public void createPet() throws Exception {
        Pet newPet = createPet(null, "NewPet");
        Pet savedPet = createPet(1L, "NewPet");
        doReturn(savedPet).when(petService).savePet(any(Pet.class));

        mockMvc.perform(post("/pet")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newPet)))
            .andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("NewPet"));

        verify(petService).savePet(any(Pet.class));
    }

    @Test
    public void updatePet() throws Exception {
        Pet pet = createPet(1L, "UpdatedPet");
        doReturn(pet).when(petService).savePet(any(Pet.class));

        mockMvc.perform(put("/pet")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pet)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("UpdatedPet"));

        verify(petService).savePet(any(Pet.class));
    }

    @Test
    public void removePet() throws Exception {
        Long petId = 1L;

        mockMvc.perform(delete("/pet/{id}", petId))
            .andExpect(status().isOk());

        verify(petService).removePet(eq(petId));
    }

    private Pet createPet(Long id, String name) {
        return Pet.builder()
            .id(id)
            .name(name)
            .status(Status.AVAILABLE)
            .build();
    }
}
