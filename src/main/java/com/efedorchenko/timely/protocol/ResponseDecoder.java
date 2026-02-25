package com.efedorchenko.timely.protocol;

/**
 * Разбирает строки ответов daemon → CLI.
 * Парный класс к {@link ResponseEncoder}, который строит эти ответы.
 */
public final class ResponseDecoder {

    private ResponseDecoder() {}

    public static boolean isOk(String resp) {
        return resp != null && resp.startsWith(Protocol.RESP_OK);
    }

    public static boolean isError(String resp) {
        return resp != null && resp.startsWith(Protocol.RESP_ERROR);
    }

    public static boolean isBye(String resp) {
        return resp != null && resp.startsWith(Protocol.RESP_BYE);
    }

    public static String errorMessage(String resp) {
        if (!isError(resp) || resp.length() <= Protocol.RESP_ERROR.length() + 1) return "";
        return resp.substring(Protocol.RESP_ERROR.length() + 1);
    }

    /** Парсит ответ status. Формат: "OK currentSec totalSec isRunning" */
    public static StatusResponse parseStatus(String resp) {
        if (!isOk(resp)) return null;

        var parts = resp.split(" ", 5);
        if (parts.length < 4) return null;

        try {
            return new StatusResponse(
                    Long.parseLong(parts[1]),
                    Long.parseLong(parts[2]),
                    Boolean.parseBoolean(parts[3])
            );
        } catch (NumberFormatException _) {
            return null;
        }
    }

    /**
     * Извлекает секунды из ответа вида "OK 123" или "BYE 45".
     * Берёт всё после первого пробела и парсит как long.
     *
     * @throws NumberFormatException если ответ не содержит числа
     */
    public static long parseSeconds(String resp) {
        int space = resp.indexOf(' ');
        if (space < 0 || space == resp.length() - 1) {
            throw new NumberFormatException("No seconds in response: " + resp);
        }
        return Long.parseLong(resp.substring(space + 1).trim());
    }
}
