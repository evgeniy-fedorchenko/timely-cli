package com.efedorchenko.timely.protocol;

/**
 * Протокол CLI ↔ Daemon. Текстовый, одна строка = одно сообщение.
 * <p>
 * Команды: ping, start, stop, status, shutdown
 * Ответы: timely, OK ..., ERROR ..., BYE ...
 * <p>
 * Содержит только константы команд/ответов и разбор типа команды.
 * Построение ответов - {@link ResponseEncoder}, разбор ответов - {@link ResponseDecoder}.
 */
public final class Protocol {

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

    /** Первое слово команды. */
    public static String commandType(String command) {
        if (command == null || command.isBlank()) return "";
        int space = command.indexOf(' ');
        return space < 0 ? command : command.substring(0, space);
    }
}
