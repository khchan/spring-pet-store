package com.khchan.petstore.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Tag {
    private Long id;
    private String name;
}
