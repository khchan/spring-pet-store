package com.khchan.petstore.dto;

import com.khchan.petstore.domain.Category;
import com.khchan.petstore.domain.Status;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Pet {
    private Long id;
    private String name;
    private Status status;
    private Category category;
}