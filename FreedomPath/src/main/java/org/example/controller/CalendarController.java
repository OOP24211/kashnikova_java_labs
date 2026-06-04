package org.example.controller;

import org.example.repository.HabitDayRepository;
import org.example.service.TrackingService;
import org.example.repository.UserRepository;
import org.example.model.User;
import org.example.model.Achievement;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class CalendarController {

    private final TrackingService trackingService;
    private final HabitDayRepository habitRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public CalendarController(TrackingService trackingService, HabitDayRepository habitRepository,
                              UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.trackingService = trackingService;
        this.habitRepository = habitRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/")
    public String index(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();

        long streak = trackingService.calculateCurrentStreak(user);

        model.addAttribute("username", user.getUsername());
        model.addAttribute("streak", streak);
        model.addAttribute("motivation", trackingService.getMotivationPhrase(streak));
        model.addAttribute("today", LocalDate.now());

        var allAchievements = trackingService.getAchievementsForUser(user);

        List<String> unlockedAchievements = allAchievements.stream()
                .filter(Achievement::isUnlocked)
                .map(Achievement::getTitle)
                .collect(Collectors.toList());

        System.out.println("Открытые ачивки юзера: " + unlockedAchievements);

        model.addAttribute("unlockedAchievements", unlockedAchievements);
        model.addAttribute("calendarDays", trackingService.getCalendarDays(user));

        return "calendar";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/log")
    public String logDay(@RequestParam("clean") boolean clean,
                         @AuthenticationPrincipal UserDetails userDetails,
                         RedirectAttributes redirectAttributes) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();

        long streakBefore = trackingService.calculateCurrentStreak(user);
        trackingService.logDay(clean, user);
        long streakAfter = trackingService.calculateCurrentStreak(user);

        if (clean && streakAfter > streakBefore) {
            var achievements = trackingService.getAchievementsForUser(user);
            for (var ach : achievements) {
                if (ach.getRequiredDays() == streakAfter) {
                    redirectAttributes.addFlashAttribute("newAchievement", ach.getTitle());
                    break;
                }
            }
        }

        return "redirect:/";
    }

    @GetMapping("/achievements")
    public String achievementsPage(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();

        var allAchievements = trackingService.getAchievementsForUser(user);

        model.addAttribute("username", user.getUsername());
        model.addAttribute("achievements", allAchievements);

        return "achievements";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam String username, @RequestParam String password) {
        if (userRepository.findByUsername(username).isPresent()) {
            return "redirect:/login?error=register";
        }

        String encodedPassword = passwordEncoder.encode(password);

        User newUser = new User(username, encodedPassword, "ROLE_USER");
        userRepository.save(newUser);

        return "redirect:/login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "login";
    }
}