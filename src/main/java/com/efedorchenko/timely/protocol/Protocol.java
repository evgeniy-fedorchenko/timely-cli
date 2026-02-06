package com.efedorchenko.timely.protocol;

/**
 * Протокол CLI ↔ Daemon. Текстовый, одна строка = одно сообщение.
 *
 * Команды: ping, start, stop, status, shutdown
 * Ответы: timely, OK ..., ERROR ..., BYE ...
 */
public final class Protocol {

    public static final int DAEMON_PORT = 52713;
    public static final String DAEMON_HOST = "localhost";
    public static final int CONNECT_TIMEOUT_MS = 1000;
    public static final int READ_TIMEOUT_MS = 5000;

    public static final String CMD_PING = "ping";
    public static final String CMD_START = "start";
    public static final String CMD_STOP = "stop";
    public static final String CMD_STATUS = "status";
    public static final String CMD_SHUTDOWN = "shutdown";

    public static final String RESP_PONG = "timely";
    public static final String RESP_OK = "OK";
    public static final String RESP_ERROR = "ERROR";
    public static final String RESP_BYE = "BYE";

    private Protocol() {}

    /** Извлекает проект из "start learning java" → "learning java". */
//    public static String parseProject(String command) {
//        if (command == null || !command.startsWith(CMD_START)) {
//            return null;
//        }
//        var rest = command.substring(CMD_START.length()).trim();
//        return rest.isEmpty() ? null : rest;
//    }

    /** Первое слово команды. */
    public static String commandType(String command) {
        if (command == null || command.isBlank()) return "";
        int space = command.indexOf(' ');
        return space < 0 ? command : command.substring(0, space);
    }

    public static String error(String message) {
        return RESP_ERROR + " " + message;
    }

    /** Формат: "OK currentSec totalSec isRunning" */
    public static String statusResponse(long currentSec, long totalSec, boolean running/*, String project*/) {
        var sb = new StringBuilder()
                .append(RESP_OK)
                .append(" ").append(currentSec)
                .append(" ").append(totalSec)
                .append(" ").append(running);
//        if (project != null) {
//            sb.append(" ").append(project);
//        }
        return sb.toString();
    }

    public static String bye(String summary) {
        return RESP_BYE + " " + summary;
    }

    public static boolean isOk(String resp) {
        return resp != null && resp.startsWith(RESP_OK);
    }

    public static boolean isError(String resp) {
        return resp != null && resp.startsWith(RESP_ERROR);
    }

    public static boolean isBye(String resp) {
        return resp != null && resp.startsWith(RESP_BYE);
    }

    public static String errorMessage(String resp) {
        if (!isError(resp) || resp.length() <= RESP_ERROR.length() + 1) return "";
        return resp.substring(RESP_ERROR.length() + 1);
    }

    /** Парсит ответ status. */
    public static StatusResponse parseStatus(String resp) {
        if (!isOk(resp)) return null;

        var parts = resp.split(" ", 5);
        if (parts.length < 4) return null;

        try {
            return new StatusResponse(
                    Long.parseLong(parts[1]),
                    Long.parseLong(parts[2]),
                    Boolean.parseBoolean(parts[3])
//                    parts.length > 4 ? parts[4] : null
            );
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
