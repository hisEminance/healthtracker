package com.example.healthtrack.repo;


import com.example.healthtrack.entity.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;



//Насправді це було дуже важко, я ніколи не оптимізовував запити в бд і користувався звичайнийми запитами від JpaRepository, по типу save();
//за допомогою пари статєйок і гпт я зрозумів, що оптимізовувати найкраще просто пишучи кастомні запити в самому репозиторії, не знаю, наскільки це вийшло, але сподіваюсь, що данний розв'язок підійде.
@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    //пошук пацієнтів з фільтрацією по параметру search, якщо не переданий, то ігноруємо його, якщо переданий то ->
    //шукаємо ім'я по частковому збігу (like) і перевіряємо
    //чи у пацієнта є хоча б один візит до одного з лікарів у списку doctorIds
    //remarka: запит фільтрує лише тих пацієнтів, які хоча б раз були у вказаних лікарів
    @Query("""
        SELECT p FROM Patient p 
        WHERE (:search IS NULL OR LOWER(p.firstName) LIKE LOWER(CONCAT('%', :search, '%')) 
                              OR LOWER(p.lastName) LIKE LOWER(CONCAT('%', :search, '%')))
        AND (:doctorIds IS NULL OR EXISTS (
            SELECT 1 FROM Visit v WHERE v.patient.id = p.id AND v.doctor.id IN :doctorIds
        ))
    """)
    Page<Patient> searchPatients(
            @Param("search") String search,
            @Param("doctorIds") List<Long> doctorIds,
            Pageable pageable
    );

    //для кожного пацієнта і лікаря вибираємо останній візит, шукаємо для певного пацієнту, групуємо лікарів і пацієнтів.
    @Query("""
        SELECT v.patient.id, v.doctor.id, MAX(v.startDateTime) 
        FROM Visit v 
        WHERE v.patient.id IN :patientIds
        GROUP BY v.patient.id, v.doctor.id
    """)
    List<Object[]> findLatestVisitsByPatients(@Param("patientIds") List<Long> patientIds);


    //Замість того, щоб вираховувати кількість пацієнтів у коді (повільно)
    //ми відразу отримуємо результат з БД шляхом підрахунку для кожного лікаря унікальних пацієнтів
    // потім шукаємо для певного лікаря і групуємо по лікарях
    @Query("""
        SELECT v.doctor.id, COUNT(DISTINCT v.patient.id) 
        FROM Visit v 
        WHERE v.doctor.id IN :doctorIds
        GROUP BY v.doctor.id
    """)
    List<Object[]> countUniquePatientsPerDoctor(@Param("doctorIds") List<Long> doctorIds);
}