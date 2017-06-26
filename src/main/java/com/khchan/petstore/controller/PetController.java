package com.khchan.petstore.controller;

import com.khchan.petstore.dto.Pet;
import com.khchan.petstore.service.PetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class PetController {

    private final PetService petService;

    @Autowired
    public PetController(PetService petService) {
        this.petService = petService;
    }

    @GetMapping(value = "/pets")
    public List<Pet> findAllPets() {
        return petService.findAllPets();
    }
}
