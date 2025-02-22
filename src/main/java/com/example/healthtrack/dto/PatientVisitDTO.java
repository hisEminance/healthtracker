package com.example.healthtrack.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class PatientVisitDTO {
    private Long patientId;
    private Long doctorId;
    private Instant startDateTime;
    private Instant endDateTime;
    private String doctorFirstName;
    private String doctorLastName;
    private Long totalPatients;
    private String timezone;
    public PatientVisitDTO(Long patientId, Long doctorId, Instant startDateTime, Instant endDateTime,
                           String doctorFirstName, String doctorLastName, Long totalPatients, String timezone) {
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.doctorFirstName = doctorFirstName;
        this.doctorLastName = doctorLastName;
        this.totalPatients = totalPatients;
        this.timezone = timezone;
    }
}
