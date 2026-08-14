package com.example.hackathon_team3_be.repository;

import com.example.hackathon_team3_be.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExperienceProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByAvailableTrueOrderByIdAsc();
}
