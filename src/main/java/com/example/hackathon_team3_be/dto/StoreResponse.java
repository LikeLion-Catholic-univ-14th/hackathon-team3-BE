package com.example.hackathon_team3_be.dto;

public record StoreResponse(String storeId,String name,String address,String phone,
                            Double latitude,Double longitude,boolean resenseAvailable,
                            int appointmentDurationMinutes,String dataStatus) { }
