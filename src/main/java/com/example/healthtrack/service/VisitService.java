package com.example.healthtrack.service;

import com.example.healthtrack.dto.VisitRequest;
import com.example.healthtrack.entity.Doctor;
import com.example.healthtrack.entity.Patient;
import com.example.healthtrack.entity.Visit;
import com.example.healthtrack.repo.DoctorRepository;
import com.example.healthtrack.repo.PatientRepository;
import com.example.healthtrack.repo.VisitRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class VisitService {
    private final VisitRepository visitRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    public VisitService(VisitRepository visitRepository, PatientRepository patientRepository, DoctorRepository doctorRepository) {
        this.visitRepository = visitRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
    }

    public Visit createVisit(VisitRequest request) {

        Patient patient = patientRepository.findById((long) request.getPatientId())
                .orElseThrow(() -> new RuntimeException("Пацієнт не знайдений"));
        Doctor doctor = doctorRepository.findById((long) request.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Лікаря не знайдено"));


        ZoneId doctorZone = ZoneId.of(doctor.getTimezone());
        LocalDateTime localStart = LocalDateTime.parse(request.getStart());
        LocalDateTime localEnd = LocalDateTime.parse(request.getEnd());

        Instant startInstant = localStart.atZone(doctorZone).toInstant();
        Instant endInstant = localEnd.atZone(doctorZone).toInstant();

        if (!startInstant.isBefore(endInstant)) {
            throw new RuntimeException("Час початку повинен бути перед часом закінчення");
        }

        boolean existsOverlap = visitRepository.existsOverlappingVisit(doctor.getId(), startInstant, endInstant);
        if (existsOverlap) {
            throw new RuntimeException("В цього лікаря є візит у вказаний час");
        }

        Visit visit = new Visit();
        visit.setPatient(patient);
        visit.setDoctor(doctor);
        visit.setStartDateTime(startInstant);
        visit.setEndDateTime(endInstant);

        return visitRepository.save(visit);
    }
}
