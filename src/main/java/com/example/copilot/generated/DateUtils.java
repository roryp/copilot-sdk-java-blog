package com.example.copilot.generated;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public final class DateUtils {

    private DateUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static long daysUntil(LocalDate target) {
        Objects.requireNonNull(target, "target must not be null");
        return ChronoUnit.DAYS.between(LocalDate.now(), target);
    }

    public static boolean isWeekend(LocalDate date) {
        Objects.requireNonNull(date, "date must not be null");
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }

    public static String formatRelative(LocalDate date) {
        Objects.requireNonNull(date, "date must not be null");
        long daysDifference = ChronoUnit.DAYS.between(date, LocalDate.now());
        
        if (daysDifference == 0L) {
            return "today";
        } else if (daysDifference == 1L) {
            return "yesterday";
        } else if (daysDifference == -1L) {
            return "tomorrow";
        } else if (daysDifference > 1L) {
            return daysDifference + " days ago";
        } else {
            return Math.abs(daysDifference) + " days from now";
        }
    }
}