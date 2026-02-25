package com.efedorchenko.timely.protocol;

/**
 * Строит строки ответов daemon → CLI.
 * Парный класс к {@link ResponseDecoder}, который разбирает эти ответы.
 * Используется только в {@code DaemonServer}.
 */
public final class ResponseEncoder {

    private ResponseEncoder() {}

    public static String ok() {
        return Protocol.RESP_OK;
    }

    public static String okWithSeconds(long seconds) {
        return Protocol.RESP_OK + " " + seconds;
    }

    public static String error(String message) {
        return Protocol.RESP_ERROR + " " + message;
    }

    /** Формат: "OK currentSec totalSec isRunning" */
    public static String statusResponse(long currentSec, long totalSec, boolean running) {
        return new StringBuilder()
                .append(Protocol.RESP_OK)
                .append(" ").append(currentSec)
                .append(" ").append(totalSec)
                .append(" ").append(running)
                .toString();
    }

    public static String bye(long totalSeconds) {
        return Protocol.RESP_BYE + " " + totalSeconds;
    }

    public static String pong() {
        return Protocol.RESP_PONG;
    }
}
