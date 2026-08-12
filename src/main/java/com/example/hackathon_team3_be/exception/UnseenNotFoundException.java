package com.example.hackathon_team3_be.exception;
public class UnseenNotFoundException extends RuntimeException {
    public UnseenNotFoundException(String id){super("UNSEEN bag not found: " + id);}
}
