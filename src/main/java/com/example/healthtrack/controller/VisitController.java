package com.example.healthtrack.controller;


import com.example.healthtrack.dto.VisitRequest;
import com.example.healthtrack.entity.Visit;
import com.example.healthtrack.service.VisitService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/visits")
public class VisitController {
    private final VisitService visitService;

    public VisitController(VisitService visitService) {
        this.visitService = visitService;
    }

    @PostMapping
    public ResponseEntity<?> createVisit(@RequestBody VisitRequest request) {
        try {
            Visit visit = visitService.createVisit(request);
            return ResponseEntity.ok(visit);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}
