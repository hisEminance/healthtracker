package com.example.healthtrack.service;

import com.example.healthtrack.dto.PatientVisitDTO;
import org.springframework.stereotype.Service;
import com.example.healthtrack.entity.Patient;

import com.example.healthtrack.repo.PatientRepository;
import com.example.healthtrack.repo.VisitRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
@Service
public class PatientService {
    private final PatientRepository patientRepository;
    private final VisitRepository visitRepository;

    public PatientService(PatientRepository patientRepository, VisitRepository visitRepository) {
        this.patientRepository = patientRepository;
        this.visitRepository = visitRepository;
    }
    public Map<String, Object> getPatientsWithVisits(int page, int size, String search, List<Long> doctorIds) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<Patient> patientPage = patientRepository.searchPatients(search, doctorIds, pageable);
        List<Patient> patients = patientPage.getContent();
        List<Long> patientIds = patients.stream().map(Patient::getId).collect(Collectors.toList());

        List<PatientVisitDTO> lastVisits = visitRepository.findLastVisitsByPatients(patientIds);
        Map<Long, List<PatientVisitDTO>> patientVisitsMap = lastVisits.stream()
                .collect(Collectors.groupingBy(PatientVisitDTO::getPatientId));

        List<Map<String, Object>> patientDataList = new ArrayList<>();
        for (Patient patient : patients) {
            Map<String, Object> patientMap = new HashMap<>();
            patientMap.put("firstName", patient.getFirstName());
            patientMap.put("lastName", patient.getLastName());

            List<Map<String, Object>> lastVisitsList = new ArrayList<>();
            List<PatientVisitDTO> visitsForPatient = patientVisitsMap.get(patient.getId());
            if (visitsForPatient != null) {
                for (PatientVisitDTO dto : visitsForPatient) {
                    Map<String, Object> visitMap = new HashMap<>();
                    ZoneId doctorZone = ZoneId.of(dto.getTimezone());
                    LocalDateTime localStart = LocalDateTime.ofInstant(dto.getStartDateTime(), doctorZone);
                    LocalDateTime localEnd = LocalDateTime.ofInstant(dto.getEndDateTime(), doctorZone);
                    visitMap.put("start", localStart.toString());
                    visitMap.put("end", localEnd.toString());

                    Map<String, Object> doctorMap = new HashMap<>();
                    doctorMap.put("firstName", dto.getDoctorFirstName());
                    doctorMap.put("lastName", dto.getDoctorLastName());
                    doctorMap.put("totalPatients", dto.getTotalPatients());
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
