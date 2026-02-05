package com.efedorchenko.timely.protocol;

/**
 * Протокол общения между CLI-клиентом и Daemon-сервером.
 *
 * <p>Формат сообщений — простой текстовый, одна команда/ответ на строку.
 *
 * <h2>Команды (CLI → Daemon):</h2>
 * <ul>
 *   <li>{@code ping} — проверка, что это наш daemon</li>
 *   <li>{@code start} или {@code start <project>} — запустить таймер</li>
 *   <li>{@code stop} — остановить таймер</li>
 *   <li>{@code status} — получить статус</li>
 *   <li>{@code shutdown} — остановить daemon</li>
 * </ul>
 *
 * <h2>Ответы (Daemon → CLI):</h2>
 * <ul>
 *   <li>{@code timely} — ответ на ping</li>
 *   <li>{@code OK} — успешное выполнение start/stop</li>
 *   <li>{@code OK <current_seconds> <total_seconds> <is_running> [project]} — ответ на status</li>
 *   <li>{@code ERROR <message>} — ошибка</li>
 *   <li>{@code BYE <summary>} — ответ на shutdown перед завершением</li>
 * </ul>
 */
public final class Protocol {

    // ========== Сетевые настройки ==========

    /**
     * Порт по умолчанию для daemon.
     * Выбран достаточно большой, чтобы минимизировать конфликты.
     */
    public static final int DEFAULT_PORT = 52713;

    /**
     * Хост для подключения (только localhost).
     */
    public static final String HOST = "localhost";

    /**
     * Таймаут подключения в миллисекундах.
     */
    public static final int CONNECT_TIMEOUT_MS = 1000;

    /**
     * Таймаут чтения в миллисекундах.
     */
    public static final int READ_TIMEOUT_MS = 5000;

    // ========== Команды ==========

    public static final String CMD_PING = "ping";
    public static final String CMD_START = "start";
    public static final String CMD_STOP = "stop";
    public static final String CMD_STATUS = "status";
    public static final String CMD_SHUTDOWN = "shutdown";

    // ========== Ответы ==========

    public static final String RESP_PONG = "timely";
    public static final String RESP_OK = "OK";
    public static final String RESP_ERROR = "ERROR";
    public static final String RESP_BYE = "BYE";

    private Protocol() {
        // Утилитный класс
    }

    // ========== Форматирование ответов ==========

    /**
     * Формирует ответ об ошибке.
     *
     * @param message сообщение об ошибке
     * @return строка вида "ERROR message"
     */
    public static String error(String message) {
        return RESP_ERROR + " " + message;
    }

    /**
     * Формирует ответ на status.
     *
     * @param currentSeconds секунды текущей сессии (0 если не запущена)
     * @param totalSeconds   общее количество секунд
     * @param isRunning      запущен ли таймер
     * @param project        название проекта (может быть null)
     * @return строка вида "OK 123 456 true work"
     */
    public static String statusResponse(long currentSeconds, long totalSeconds,
                                        boolean isRunning, String project) {
        StringBuilder sb = new StringBuilder();
        sb.append(RESP_OK)
                .append(" ").append(currentSeconds)
                .append(" ").append(totalSeconds)
                .append(" ").append(isRunning);
        if (project != null && !project.isEmpty()) {
            sb.append(" ").append(project);
        }
        return sb.toString();
    }

    /**
     * Формирует ответ на shutdown.
     *
     * @param summary итоговая информация перед завершением
     * @return строка вида "BYE Total tracked: 1h 23min"
     */
    public static String bye(String summary) {
        return RESP_BYE + " " + summary;
    }

    // ========== Парсинг ответов ==========

    /**
     * Проверяет, является ли ответ успешным.
     *
     * @param response ответ от daemon
     * @return true если начинается с "OK"
     */
    public static boolean isOk(String response) {
        return response != null && response.startsWith(RESP_OK);
    }

    /**
     * Проверяет, является ли ответ ошибкой.
     *
     * @param response ответ от daemon
     * @return true если начинается с "ERROR"
     */
    public static boolean isError(String response) {
        return response != null && response.startsWith(RESP_ERROR);
    }

    /**
     * Извлекает сообщение об ошибке из ответа.
     *
     * @param response ответ вида "ERROR message"
     * @return сообщение об ошибке или пустая строка
     */
    public static String extractErrorMessage(String response) {
        if (!isError(response)) {
            return "";
        }
        return response.length() > RESP_ERROR.length() + 1
                ? response.substring(RESP_ERROR.length() + 1)
                : "";
    }
}