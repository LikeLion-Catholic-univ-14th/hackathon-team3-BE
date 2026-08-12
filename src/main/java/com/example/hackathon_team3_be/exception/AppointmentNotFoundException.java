package com.example.hackathon_team3_be.exception;

public class AppointmentNotFoundException extends RuntimeException {
    public AppointmentNotFoundException(String id){super("Appointment not found: "+id);}
}
