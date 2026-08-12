package com.example.hackathon_team3_be.exception;
public class NoNegotiationMatchException extends RuntimeException {
    public NoNegotiationMatchException(){super("No product satisfies every locked feature");}
}
