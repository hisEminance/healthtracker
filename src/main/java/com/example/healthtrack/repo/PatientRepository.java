package com.example.healthtrack.repo;


import com.example.healthtrack.entity.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


//UPD: здаю цю тестову таску другий раз, і сподіваюсь це буде прочитано.
// Хочу зауважити, що я дуже переоцінив свої знання бд та sql загалом. Під час виконання мені доводилось неодноразово питати в нейромережі, що і як, неодноразово переробляти запити, тому, що я не знаю JPQL.
// І навіть зараз, коли роботи та правки виконані, я знаю, що я швидше за все зробив недостатньо і можна оптимізувати ще.
// Я не додав індекси, я міг би детальніше та глибше вникнути в суть Executor Servicе та нормалізувати бд. Також ймовірніше за все можна якось ще краще оптимізувати VisitRepository.
// В будь-якому разі, цей процес допоміг мені розширити знання, і я задоволений результатом. Дякую за фідбек!

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    // переписав на джойн, краще підходить, якщо ми говоримо про велику к-сть данних
    // адже в попередньому варіанті для кожного запису проводилась перевірка, що займало більше часу
    // пояснення: звичайний джойн, приєднюємо таблицю візитів до пацієнтів, щоб знайти пацієнтів з візитами
    // select distinct дозволяє уникнути дублювання, у випадках коли один пацієнт має кілька візитів
    // по умові where знаходимо пацієнтів по прізвищу за допомогю like
    // і перевіряється чи параметр doctorIDs нуль чи співпадає ідентифікатор лікаря з переданими значеннями.
    // повертаємо результат у вигляді сторінки (підказав гпт, я чесно не знав, що взагалі таке існує xD)
    @Query("""
    SELECT DISTINCT p FROM Patient p 
    LEFT JOIN p.visits v 
    WHERE (:search IS NULL OR LOWER(p.firstName) LIKE LOWER(CONCAT('%', :search, '%')) 
          OR LOWER(p.lastName) LIKE LOWER(CONCAT('%', :search, '%')))
      AND (:doctorIds IS NULL OR v.doctor.id IN :doctorIds)
""")
    Page<Patient> searchPatients(
            @Param("search") String search,
            @Param("doctorIds") List<Long> doctorIds,
            Pageable pageable
    );

}