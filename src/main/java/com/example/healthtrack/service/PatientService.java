package com.example.healthtrack.service;

import org.springframework.stereotype.Service;
import com.example.healthtrack.entity.Doctor;
import com.example.healthtrack.entity.Patient;
import com.example.healthtrack.entity.Visit;
import com.example.healthtrack.repo.DoctorRepository;
import com.example.healthtrack.repo.PatientRepository;
import com.example.healthtrack.repo.VisitRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
@Service
public class PatientService {
    private final PatientRepository patientRepository;
    private final VisitRepository visitRepository;
    private final DoctorRepository doctorRepository;

    public PatientService(PatientRepository patientRepository, VisitRepository visitRepository, DoctorRepository doctorRepository) {
        this.patientRepository = patientRepository;
        this.visitRepository = visitRepository;
        this.doctorRepository = doctorRepository;
    }

    public Map<String, Object> getPatientsWithVisits(int page, int size, String search, List<Long> doctorIds) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<Patient> patientPage = patientRepository.searchPatients(search, doctorIds, pageable);
        List<Patient> patients = patientPage.getContent();
        List<Long> patientIds = patients.stream().map(Patient::getId).collect(Collectors.toList());


        List<Object[]> aggregated = patientRepository.findLatestVisitsByPatients(patientIds);
        Map<String, Instant> latestMap = new HashMap<>();
        for (Object[] row : aggregated) {
            Long pId = (Long) row[0];
            Long dId = (Long) row[1];
            Instant maxStart = (Instant) row[2];
            String key = pId + "_" + dId;
            latestMap.put(key, maxStart);
        }

        Map<Long, List<Visit>> patientVisitsMap = new HashMap<>();
        for (Map.Entry<String, Instant> entry : latestMap.entrySet()) {
            String key = entry.getKey();
            String[] parts = key.split("_");
            Long pId = Long.parseLong(parts[0]);
            Long dId = Long.parseLong(parts[1]);
            Optional<Visit> optVisit = visitRepository.findLastVisitByPatientAndDoctor(pId, dId);
            optVisit.ifPresent(visit -> patientVisitsMap.computeIfAbsent(pId, k -> new ArrayList<>()).add(visit));
        }


        List<Long> doctorIdsForCount;
        if (doctorIds == null || doctorIds.isEmpty()) {
            doctorIdsForCount = aggregated.stream()
                    .map(row -> (Long) row[1])
                    .distinct()
                    .collect(Collectors.toList());
        } else {
            doctorIdsForCount = doctorIds;
        }
        List<Object[]> countData = patientRepository.countUniquePatientsPerDoctor(doctorIdsForCount);
        Map<Long, Long> doctorPatientCountMap = new HashMap<>();
        for (Object[] row : countData) {
            Long dId = (Long) row[0];
            Long count = (Long) row[1];
            doctorPatientCountMap.put(dId, count);
        }

        List<Map<String, Object>> patientDataList = new ArrayList<>();
        for (Patient patient : patients) {
            Map<String, Object> patientMap = new HashMap<>();
            patientMap.put("firstName", patient.getFirstName());
            patientMap.put("lastName", patient.getLastName());

            List<Map<String, Object>> lastVisitsList = new ArrayList<>();
            List<Visit> visitsForPatient = patientVisitsMap.get(patient.getId());
            if (visitsForPatient != null) {
                for (Visit visit : visitsForPatient) {
                    Map<String, Object> visitMap = new HashMap<>();
                    Doctor doctor = visit.getDoctor();

                    ZoneId doctorZone = ZoneId.of(doctor.getTimezone());
                    LocalDateTime localStart = LocalDateTime.ofInstant(visit.getStartDateTime(), doctorZone);
                    LocalDateTime localEnd = LocalDateTime.ofInstant(visit.getEndDateTime(), doctorZone);
                    visitMap.put("start", localStart.toString());
                    visitMap.put("end", localEnd.toString());

                    Map<String, Object> doctorMap = new HashMap<>();
                    doctorMap.put("firstName", doctor.getFirstName());
                    doctorMap.put("lastName", doctor.getLastName());
                    doctorMap.put("totalPatients", doctorPatientCountMap.getOrDefault(doctor.getId(), 0L));
                    visitMap.put("doctor", doctorMap);

                    lastVisitsList.add(visitMap);
                }
            }
            patientMap.put("lastVisits", lastVisitsList);
            patientDataList.add(patientMap);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("data", patientDataList);
        response.put("count", patientPage.getTotalElements());
        return response;
    }
}
