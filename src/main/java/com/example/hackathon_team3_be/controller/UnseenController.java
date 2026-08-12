package com.example.hackathon_team3_be.controller;

import com.example.hackathon_team3_be.dto.*;
import com.example.hackathon_team3_be.service.UnseenService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/unseen")
public class UnseenController {
    private final UnseenService service;
    public UnseenController(UnseenService service){this.service=service;}
    @PostMapping("/generate") public GenerateUnseenResponse generate(@Valid @RequestBody GenerateUnseenRequest r){return service.generate(r);}
    @GetMapping("/{id}/match") public MatchResponse match(@PathVariable String id){return service.match(id);}
    @PostMapping("/{id}/lock") public LockFeaturesResponse lock(@PathVariable String id,@Valid @RequestBody LockFeaturesRequest r){return service.lock(id,r);}
    @GetMapping("/{id}/negotiate") public NegotiationResponse negotiate(@PathVariable String id){return service.negotiate(id);}
}
