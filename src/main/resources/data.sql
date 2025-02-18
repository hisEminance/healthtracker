
-- Додавання лікарів
INSERT INTO doctors (id, first_name, last_name, timezone) VALUES
                                                              (1, 'Gregory', 'House', 'UTC'),
                                                              (2, 'Lisa', 'Cuddy', 'UTC'),
                                                              (3, 'James', 'Wilson', 'UTC');

-- Додавання пацієнтів
INSERT INTO patients (id, first_name, last_name) VALUES
                                                     (1, 'John', 'Doe'),
                                                     (2, 'Jane', 'Smith'),
                                                     (3, 'Alice', 'Johnson'),
                                                     (4, 'Tom', 'Harris'),
                                                     (5, 'Emily', 'Davis');

-- Додавання візитів (зв’язки між пацієнтами та лікарями)
INSERT INTO visits (id, start_date_time, end_date_time, doctor_id, patient_id) VALUES
                                                                                   (1, '2025-02-18T10:00:00', '2025-02-18T10:30:00', 1, 1),
                                                                                   (2, '2025-02-18T11:00:00', '2025-02-18T11:30:00', 1, 2),
                                                                                   (3, '2025-02-18T12:00:00', '2025-02-18T12:30:00', 2, 3),
                                                                                   (4, '2025-02-18T13:00:00', '2025-02-18T13:30:00', 3, 1),
                                                                                   (5, '2025-02-19T10:00:00', '2025-02-19T10:30:00', 2, 4),
                                                                                   (6, '2025-02-19T11:00:00', '2025-02-19T11:30:00', 3, 5),
                                                                                   (7, '2025-02-20T10:00:00', '2025-02-20T10:30:00', 1, 3),
                                                                                   (8, '2025-02-20T11:00:00', '2025-02-20T11:30:00', 2, 1),
                                                                                   (9, '2025-02-20T12:00:00', '2025-02-20T12:30:00', 3, 4);