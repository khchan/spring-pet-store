package com.khchan.petstore.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a specific breed of pet.
 * More granular than Category (e.g., "Golden Retriever" vs "Dogs").
 */
@Entity
@Table(name = "breeds")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Breed {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    private Size size;

    public Breed(String name, String description, Size size) {
        this.name = name;
        this.description = description;
        this.size = size;
    }
}
