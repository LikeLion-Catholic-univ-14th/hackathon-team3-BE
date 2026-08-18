package com.example.hackathon_team3_be.config;

import com.example.hackathon_team3_be.domain.Product;
import com.example.hackathon_team3_be.domain.Store;
import com.example.hackathon_team3_be.repository.ExperienceProductRepository;
import com.example.hackathon_team3_be.repository.StoreRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DemoDataInitializer {

    @Bean
    CommandLineRunner seedDemoData(StoreRepository storeRepository, ExperienceProductRepository productRepository) {
        return args -> {
            if (storeRepository.count() == 0) {
                storeRepository.saveAll(List.of(
                        new Store("MCM HAUS SEOUL", "Seoul", "7 Dosan-daero 99-gil, Gangnam-gu", 37.5274, 127.0438),
                        new Store("MCM Lotte Main", "Seoul", "81 Namdaemun-ro, Jung-gu", 37.5649, 126.9810),
                        new Store("MCM Shinsegae Gangnam", "Seoul", "176 Sinbanpo-ro, Seocho-gu", 37.5049, 127.0049)
                ));
            }
            if (productRepository.count() == 0) {
                productRepository.saveAll(List.of(
                        new Product("MCM-DEMO-001", "Soft Diamond Crossbody", "Crossbody", "Soft", "Cognac", "/api/v1/assets/products/MCM-DEMO-001.svg"),
                        new Product("MCM-DEMO-002", "Nomad Carry Bag", "Tote", "Semi-structured", "Neutral", "/api/v1/assets/products/MCM-DEMO-002.svg"),
                        new Product("MCM-DEMO-003", "Quiet Mode Hobo", "Hobo", "Relaxed", "Black", "/api/v1/assets/products/MCM-DEMO-003.svg")
                ));
            }
        };
    }
}
