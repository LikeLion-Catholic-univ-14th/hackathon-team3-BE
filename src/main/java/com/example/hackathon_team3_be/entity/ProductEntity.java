package com.example.hackathon_team3_be.entity;

import com.example.hackathon_team3_be.dto.BagAttributes;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
public class ProductEntity {
    @Id private String productId;
    private String name;
    private String imageUrl;
    private String category;
    private String size;
    private String color;
    private String material;
    private BigDecimal price;

    protected ProductEntity() { }
    public ProductEntity(String id, String name, String category, String size, String color,
                         String material, BigDecimal price) {
        this(id, name, "/products/" + id + "/front.jpg", category, size, color, material, price);
    }

    public ProductEntity(String id, String name, String imageUrl, String category,
                         String size, String color, String material, BigDecimal price) {
        this.productId=id; this.name=name; this.category=category; this.size=size;
        this.color=color; this.material=material;
        this.price=price; this.imageUrl=imageUrl;
    }
    public String getProductId(){return productId;} public String getName(){return name;}
    public String getImageUrl(){return imageUrl;} public BigDecimal getPrice(){return price;}
    public BagAttributes attributes(){return new BagAttributes(
            category,"unknown",size,color,material,"unknown","unknown","unknown");}
}
