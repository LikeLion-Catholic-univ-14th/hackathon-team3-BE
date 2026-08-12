package com.example.hackathon_team3_be.config;

import com.example.hackathon_team3_be.entity.ProductEntity;
import com.example.hackathon_team3_be.repository.ProductRepository;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.math.BigDecimal;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McmProductDataConfig {
    @Bean
    CommandLineRunner mcmProducts(ProductRepository repository) {
        return args -> {
            if (repository.count() > 0) return;
            ObjectMapper objectMapper = new ObjectMapper();
            try (InputStream input = new ClassPathResource("data/mcm-bags.normalized.json").getInputStream()) {
                JsonNode products = objectMapper.readTree(input).path("products");
                for (JsonNode p : products) {
                    JsonNode attributes = p.path("officialMatchingAttributes");
                    repository.save(new ProductEntity(
                            text(p, "productId"), textOr(p, "name", text(p, "productId")),
                            textOr(p, "imageUrl", ""), text(attributes, "category"),
                            text(attributes, "size"),
                            text(attributes, "color"), text(attributes, "material"),
                            decimalOrZero(p, "price")));
                }
            }
        };
    }

    private static String text(JsonNode node, String field) {
        return node.path(field).asText();
    }

    private static String textOr(JsonNode node, String field, String fallback) {
        String value = text(node, field);
        return value == null || value.isBlank() ? fallback : value;
    }

    private static BigDecimal decimalOrZero(JsonNode node, String field) {
        return node.path(field).isNumber() ? node.path(field).decimalValue() : BigDecimal.ZERO;
    }

}
