package com.fsms.analytics;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// ============================================================
// SportProfileRegistry looks up the SportProfile for a sport name.
// Spring injects every SportProfile bean; the registry indexes them
// by sport. Callers ask for a profile and receive the abstract type
// (Polymorphism) without knowing which subclass they get.
// ============================================================
@Component
public class SportProfileRegistry {

    // Display order used across the UI
    public static final List<String> SPORT_ORDER = List.of("Football", "Cricket", "Basketball", "Tennis");

    private final Map<String, SportProfile> profiles = new LinkedHashMap<>();

    public SportProfileRegistry(List<SportProfile> allProfiles) {
        for (String sport : SPORT_ORDER) {
            for (SportProfile p : allProfiles) {
                if (p.getSport().equals(sport)) profiles.put(sport, p);
            }
        }
        for (SportProfile p : allProfiles) {
            profiles.putIfAbsent(p.getSport(), p);
        }
    }

    // Returns the profile for a sport, or null if the sport is unknown
    public SportProfile get(String sport) {
        if (sport == null) return null;
        for (Map.Entry<String, SportProfile> e : profiles.entrySet()) {
            if (e.getKey().equalsIgnoreCase(sport.trim())) return e.getValue();
        }
        return null;
    }

    public boolean supports(String sport) {
        return get(sport) != null;
    }

    public List<String> getSports() {
        return Collections.unmodifiableList(new ArrayList<>(profiles.keySet()));
    }
}
