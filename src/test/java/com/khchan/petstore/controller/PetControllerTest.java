package com.khchan.petstore.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.khchan.PetstoreApplication;
import com.khchan.petstore.domain.PetEntity;
import com.khchan.petstore.domain.Status;
import com.khchan.petstore.dto.Pet;
import com.khchan.petstore.repository.PetRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.Charset;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = PetstoreApplication.class)
@WebAppConfiguration
public class PetControllerTest {

    private Long PET_ID = 1L;

    private MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
        MediaType.APPLICATION_JSON.getSubtype(),
        Charset.forName("utf8"));

    private ObjectMapper jsonMapper = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private PetRepository petRepository;

    @Before
    public void setUp() throws Exception {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();

        petRepository.save(PetEntity.builder()
            .id(PET_ID)
            .name("mittens")
            .status(Status.AVAILABLE)
            .build());
    }

    @Test
    public void findAllPets() throws Exception {
        mockMvc.perform(get("/pets"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(contentType));
    }

    @Test
    public void findPet() throws Exception {
        mockMvc.perform(get("/pet/" + PET_ID.toString()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(contentType));
    }

    @Test
    public void createPet() throws Exception {
        Pet pet = Pet.builder().name("Timmy").status(Status.SOLD).build();

        mockMvc.perform(post("/pet")
            .header("Content-Type", "application/json")
            .content(jsonMapper.writeValueAsString(pet)))
            .andExpect(status().isCreated())
            .andExpect(content().contentType(contentType));
    }

    @Test
    public void updatePet() throws Exception {
        Pet pet = Pet.builder().id(1L).name("Timmy").status(Status.SOLD).build();

        mockMvc.perform(put("/pet")
            .header("Content-Type", "application/json")
            .content(jsonMapper.writeValueAsString(pet)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(contentType));
    }

    @Test
    public void removePet() throws Exception {
        mockMvc.perform(delete("/pet/" + PET_ID.toString())
            .header("Content-Type", "application/json"))
            .andExpect(status().isOk());
    }

}