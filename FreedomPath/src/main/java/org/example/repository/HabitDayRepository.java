package org.example.repository;

import org.example.model.HabitDay;
import org.example.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface HabitDayRepository extends JpaRepository<HabitDay, Long> {
    Optional<HabitDay> findByDateAndUser(LocalDate date, User user);
    List<HabitDay> findAllByUserOrderByDateDesc(User user);
}