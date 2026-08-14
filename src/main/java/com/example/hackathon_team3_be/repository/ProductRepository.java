package com.example.hackathon_team3_be.repository;

import com.example.hackathon_team3_be.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<ProductEntity, String> { }
