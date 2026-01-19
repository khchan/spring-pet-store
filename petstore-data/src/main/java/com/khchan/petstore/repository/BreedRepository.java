package com.khchan.petstore.repository;

import com.khchan.petstore.domain.Breed;
import com.khchan.petstore.domain.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BreedRepository extends JpaRepository<Breed, Long> {
    Optional<Breed> findByName(String name);

    List<Breed> findBySize(Size size);
}
