package com.example.hackathon_team3_be.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String sku;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 50)
    private String silhouette;

    @Column(nullable = false, length = 50)
    private String structureType;

    @Column(nullable = false, length = 50)
    private String color;

    @Column(nullable = false, length = 300)
    private String imageUrl;

    @Column(nullable = false)
    private boolean available = true;

    public Product(String sku, String name, String silhouette, String structureType, String color, String imageUrl) {
        this.sku = sku;
        this.name = name;
        this.silhouette = silhouette;
        this.structureType = structureType;
        this.color = color;
        this.imageUrl = imageUrl;
    }
}
