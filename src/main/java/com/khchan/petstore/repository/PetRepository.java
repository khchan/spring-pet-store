package com.khchan.petstore.repository;

import com.khchan.petstore.domain.PetEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PetRepository extends JpaRepository<PetEntity, Long> {
}
