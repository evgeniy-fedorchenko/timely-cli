package com.efedorchenko.timely.output;

import java.time.Duration;
import java.util.Objects;

/**
 * Форматирует длительность в вид "1h 2min 3sec".
 * Нулевые компоненты пропускаются. Ноль секунд → "0sec".
 */
public final class DurationFormatter {

    private DurationFormatter() {}

    public static String format(Duration duration) {
        Objects.requireNonNull(duration, "duration must not be null");
        return format(duration.toSeconds());
    }

    public static String format(long totalSeconds) {
        if (totalSeconds < 0) {
            throw new IllegalArgumentException("Seconds cannot be negative: " + totalSeconds);
        }

        if (totalSeconds == 0) {
            return "0sec";
        }

        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        var sb = new StringBuilder();

        if (hours > 0) {
            sb.append(hours).append("h");
        }
        if (minutes > 0) {
            if (!sb.isEmpty()) sb.append(" ");
            sb.append(minutes).append("min");
        }
        if (seconds > 0) {
            if (!sb.isEmpty()) sb.append(" ");
            sb.append(seconds).append("sec");
        }

        return sb.toString();
    }
}
