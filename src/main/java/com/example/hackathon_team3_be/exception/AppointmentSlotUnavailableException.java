package com.example.hackathon_team3_be.exception;

public class AppointmentSlotUnavailableException extends RuntimeException {
    public AppointmentSlotUnavailableException(){super("The selected appointment slot is unavailable");}
}
