package org.example.service;

import java.util.ArrayList;
import java.time.YearMonth;

import org.example.model.Achievement;
import org.example.model.CalendarDay;
import org.example.model.HabitDay;
import org.example.model.User;
import org.example.repository.HabitDayRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
public class TrackingService {

    private final HabitDayRepository repository;

    public TrackingService(HabitDayRepository repository) {
        this.repository = repository;
    }

    public void logDay(boolean isClean, User user) {
        LocalDate today = LocalDate.now();
        HabitDay day = repository.findByDateAndUser(today, user)
                .orElse(new HabitDay(today, isClean));
        day.setClean(isClean);
        day.setUser(user);
        repository.save(day);
    }

    public long calculateCurrentStreak(User user) {
        List<HabitDay> allDays = repository.findAllByUserOrderByDateDesc(user);
        long streak = 0;
        for (HabitDay day : allDays) {
            if (day.isClean()) { streak++; } else { break; }
        }
        return streak;
    }

    public String getMotivationPhrase(long streak) {
        if (streak == 0) return "Ошибки случаются. Главное — попробовать снова прямо сейчас!";
        if (streak < 3) return "Первые дни самые тяжелые. Держись, ты сильнее этой привычки!";
        return "Отличный результат! Шаг за шагом ты побеждаешь.";
    }

    public List<CalendarDay> getCalendarDays(User user) {
        List<CalendarDay> calendarDays = new ArrayList<>();
        LocalDate today = LocalDate.now();
        YearMonth currentMonth = YearMonth.from(today);

        LocalDate firstOfMonth = currentMonth.atDay(1);
        int dayOfWeekValue = firstOfMonth.getDayOfWeek().getValue();

        for (int i = 1; i < dayOfWeekValue; i++) {
            calendarDays.add(new CalendarDay(null, 0, "empty"));
        }

        int lengthOfMonth = currentMonth.lengthOfMonth();
        for (int day = 1; day <= lengthOfMonth; day++) {
            LocalDate date = currentMonth.atDay(day);

            if (date.isAfter(today)) {
                calendarDays.add(new CalendarDay(date, day, "future"));
            } else {
                var habitDayOpt = repository.findByDateAndUser(date, user);
                if (habitDayOpt.isPresent()) {
                    String status = habitDayOpt.get().isClean() ? "clean" : "fail";
                    calendarDays.add(new CalendarDay(date, day, status));
                } else {
                    calendarDays.add(new CalendarDay(date, day, "none"));
                }
            }
        }

        return calendarDays;
    }

    public List<Achievement> getAchievementsForUser(User user) {
        long currentStreak = calculateCurrentStreak(user);
        List<Achievement> achievements = new ArrayList<>();

        achievements.add(new Achievement("День первый", "Самый сложный шаг сделан!", 1, currentStreak >= 1));
        achievements.add(new Achievement("Очищение", "Легкие начинают дышать.", 3, currentStreak >= 3));
        achievements.add(new Achievement("Стойкость", "Почти неделя свободы!", 5, currentStreak >= 5));
        achievements.add(new Achievement("Неделя без дыма", "Отличный результат!", 7, currentStreak >= 7));
        achievements.add(new Achievement("Новый человек", "Две недели чистоты! Тяга отступает.", 14, currentStreak >= 14));
        achievements.add(new Achievement("Железная воля", "Месяц свободы! Вы полностью контролируете себя.", 30, currentStreak >= 30));

        return achievements;
    }
}