package com.efedorchenko.timely.format;

/**
 * Форматирует длительность в человекочитаемый вид.
 *
 * <p>Формат: {@code 1h 2min 3sec}
 *
 * <p>Правила:
 * <ul>
 *   <li>Нулевые компоненты пропускаются</li>
 *   <li>Если всё нули — выводится {@code 0sec}</li>
 *   <li>Примеры: 0 → "0sec", 65 → "1min 5sec", 3665 → "1h 1min 5sec"</li>
 * </ul>
 */
public final class DurationFormatter {

    private DurationFormatter() {
        // Утилитный класс
    }

    /**
     * Форматирует количество секунд в строку вида "1h 2min 3sec".
     *
     * @param totalSeconds общее количество секунд (неотрицательное)
     * @return отформатированная строка
     * @throws IllegalArgumentException если totalSeconds отрицательное
     */
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

        StringBuilder sb = new StringBuilder();

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