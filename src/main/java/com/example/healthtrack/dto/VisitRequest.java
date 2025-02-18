package com.example.healthtrack.dto;

import lombok.Data;

@Data
public class VisitRequest {
    private String start;
    private String end;
    private int patientId;
    private int doctorId;
}
