package com.example.healthtrack.repo;

import com.example.healthtrack.entity.Visit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface VisitRepository extends JpaRepository<Visit, Long> {
    //найважчий запит, писав гпт, я редачив, але всеодно туго написано, суть в тому, що ми підраховуємо к-сть записів в таблиці visit які задовільняють умови філтрації
    //якщо є хоча б один візит, який перетинається з новим, то COUNT(v) буде більше за 0, і запит поверне true. Якщо ж перетину немає — false
    //він перевіряє всі візити для лікаря з doctorId
    //для кожного візиту лікаря перевіряється, чи його час перекривається з часом нового візиту за допомогою трьох умов, самі умови писав гпт, я не здужав, хоч і розумів, які мають бути.
    @Query("""
        SELECT COUNT(v) > 0 FROM Visit v 
        WHERE v.doctor.id = :doctorId 
        AND (:start BETWEEN v.startDateTime AND v.endDateTime 
             OR :end BETWEEN v.startDateTime AND v.endDateTime
             OR v.startDateTime BETWEEN :start AND :end)
    """)
    boolean existsOverlappingVisit(
            @Param("doctorId") Long doctorId,
            @Param("start") Instant start,
            @Param("end") Instant end
    );


    //найпростіший запит, просто дістаємо останній Visit та
    //знаходимо візити конкретного пацієнта до конкретного лікаря, сортуємо від найновіших до найстаріших і ставимо ліміт 1, щоб бачити тільки найновіший
    @Query("""
        SELECT v FROM Visit v 
        WHERE v.patient.id = :patientId AND v.doctor.id = :doctorId
        ORDER BY v.startDateTime DESC 
        LIMIT 1
    """)
    Optional<Visit> findLastVisitByPatientAndDoctor(
            @Param("patientId") Long patientId,
            @Param("doctorId") Long doctorId
    );
}