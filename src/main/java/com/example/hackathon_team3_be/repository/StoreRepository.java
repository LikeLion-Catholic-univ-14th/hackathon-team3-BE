package com.example.hackathon_team3_be.repository;

import com.example.hackathon_team3_be.domain.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoreRepository extends JpaRepository<Store, Long> {
    List<Store> findByActiveTrueOrderByNameAsc();
    List<Store> findByActiveTrueAndCityIgnoreCaseOrderByNameAsc(String city);
}
