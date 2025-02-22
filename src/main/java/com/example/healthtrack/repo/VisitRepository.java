package com.example.healthtrack.repo;

import com.example.healthtrack.dto.PatientVisitDTO;
import com.example.healthtrack.entity.Visit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface VisitRepository extends JpaRepository<Visit, Long> {
    //найважчий запит, писав гпт, я редачив, але всеодно туго написано, суть в тому, що ми підраховуємо к-сть записів в таблиці visit які задовільняють умови філтрації
    //якщо є хоча б один візит, який перетинається з новим, то COUNT(v) буде більше за 0, і запит поверне true. Якщо ж перетину немає — false
    //він перевіряє всі візити для лікаря з doctorId
    //для кожного візиту лікаря перевіряється, чи його час перекривається з часом нового візиту за допомогою трьох умов, самі умови писав гпт, я не здужав
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


    //Запит вибирає останній візит кожного пацієнта до конкретного лікаря та повертає розширену інформацію про цей візит
    //Юзаєм `SELECT new com.example.healthtrack.dto.PatientVisitDTO(...)` для створення DTO, який містить необхідні дані
    //JOIN v.doctor d` дозволяє отримати інформацію про лікаря, який приймав пацієнта
    //фільтруємо лише пацієнтів, переданих у параметрі `patientIds`.
    //використовуємо сабквері `MAX(v3.startDateTime)`, щоб знайти останній візит кожного пацієнта до кожного лікаря
    //Підзапит `(SELECT COUNT(DISTINCT v2.patient.id) FROM Visit v2 WHERE v2.doctor.id = d.id)` рахує унікальних пацієнтів у лікаря
    //данний запит, не є найоптимальніший, але працює та простий у сприйнятті, я не придумав, як зробити краще, а гпт видав:
    //що, альтернативою могло б бути використання `GROUP BY` або переписування на більш ефективний JOIN замість підзапиту, як я це робив у методі searchPatients.
    @Query("""
       SELECT new com.example.healthtrack.dto.PatientVisitDTO(
          v.patient.id,
          v.doctor.id,
          v.startDateTime,
          v.endDateTime,
          d.firstName,
          d.lastName,
          (SELECT CAST(COUNT(DISTINCT v2.patient.id) AS long) FROM Visit v2 WHERE v2.doctor.id = d.id),
          d.timezone
       )
       FROM Visit v JOIN v.doctor d
       WHERE v.patient.id IN :patientIds
         AND v.startDateTime = (
             SELECT MAX(v3.startDateTime)
             FROM Visit v3
             WHERE v3.patient.id = v.patient.id AND v3.doctor.id = v.doctor.id
         )
    """)
    List<PatientVisitDTO> findLastVisitsByPatients(@Param("patientIds") List<Long> patientIds);

}