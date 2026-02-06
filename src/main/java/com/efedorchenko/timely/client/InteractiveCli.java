package com.efedorchenko.timely.client;

import com.efedorchenko.timely.AppProperties;
import com.efedorchenko.timely.daemon.DaemonServer;
import com.efedorchenko.timely.format.DurationFormatter;
import com.efedorchenko.timely.protocol.Protocol;
import com.efedorchenko.timely.tracker.Tracker;

import java.io.IOException;
import java.util.Scanner;
import java.util.Set;

import static java.lang.IO.print;
import static java.lang.IO.println;

/**
 * Интерактивный CLI для dev-режима
 * REPL: читает команды из stdin, отправляет в daemon, выводит результат
 */
public final class InteractiveCli {

    private static final Set<String> KNOWN_COMMANDS = Set.of(
            Protocol.CMD_START,
            Protocol.CMD_STOP,
            Protocol.CMD_STATUS,
            Protocol.CMD_PING
//            Еще exit, но это команда cli-режима, а не общения с демоном, как собственно help/version
    );

    private static final String WELCOME_MESS = """
        
        Welcome to Timely CLI v%s — time tracker
        Type 'help' for commands, 'exit' to quit
        
        """.formatted(AppProperties.version());

    private final DaemonClient client;

    public InteractiveCli() {
        this(new DaemonClient());
    }

    public InteractiveCli(DaemonClient client) {
        this.client = client;
    }

    /** Стартует демона и запускает цикл. Выход по 'exit' или EOF */
    public void run() {

        println("dev mode detected");
        if (!startDaemon()) {
            println("ERROR: Failed to start daemon");
            return;
        }
        println(WELCOME_MESS);

        var scanner = new Scanner(System.in);

        while (true) {
            print("> ");

            if (!scanner.hasNextLine()) break;
            var input = scanner.nextLine().trim();
            if (input.isEmpty()) continue;

            switch (input.toLowerCase()) {
                case "exit", "quit" -> {
                    exit();
                    return;
                }
                case "help" -> ResponsePrinter.printHelp();
                case "version" -> ResponsePrinter.printVersion();
                default -> executeCommand(input);
            }
        }
    }

    private boolean startDaemon() {
        if (client.isDaemonRunning()) {
            println("Daemon already running");
            return true;
        }

        println("Starting daemon...");

        var tracker = new Tracker();
        var daemon = new DaemonServer(tracker);

        /* Так как в cli-режиме (типа dev), то стартуем daemon автоматически и немного ждем,
           пока поднимется. В prod-режиме это отдельный процесс на запуск, см. Main.run() */
        Thread.startVirtualThread(() -> {
            try {
                daemon.start();
            } catch (IOException e) {
                println("ERROR: Daemon starting failed: " + e.getMessage());
            }
        });

        for (int i = 0; i < 20; i++) {
            if (client.isDaemonRunning()) {
                println("Daemon started");
                return true;
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException _) {
                // Прерывание при ожидании — выходим из цикла
                break;
            }
        }

        return false;
    }
    private void executeCommand(String command) {
        var type = Protocol.commandType(command);

        if (!KNOWN_COMMANDS.contains(type)) {
            println("ERROR: Unknown command: " + type + ". Type 'help' for available commands.");
            return;
        }

        try {
            var response = client.send(command);
            printResponse(command, response);
        } catch (DaemonNotRunningException e) {
            println(e.getMessage());
        } catch (IOException e) {
            println("Error: " + e.getMessage());
        }
    }

    private void printResponse(String command, String response) {
        var type = Protocol.commandType(command);

        switch (type) {
            case Protocol.CMD_START -> ResponsePrinter.printStartResult(response);
            case Protocol.CMD_STOP -> ResponsePrinter.printStopResult(response);
            case Protocol.CMD_STATUS -> ResponsePrinter.printStatus(response);
            case Protocol.CMD_PING -> println("pong");
            default -> println(response);
        }
    }


    /** Сначала получаем статус, чтоб показать, что получилось по итогу, потом останавливаемся */
    private void exit() {
        try {
            var response = client.send(Protocol.CMD_STATUS);
            var status = Protocol.parseStatus(response);
            if (status == null) return;

            if (status.running()) {
                println("Timer was running: " + status.currentSec());
            }
            if (status.totalSec() > 0) {
                println("Total: " + DurationFormatter.format(status.totalSec()));
            }
        } catch (IOException _) {
            // Daemon недоступен — выходим без статистики
        }

        try {
            client.send(Protocol.CMD_SHUTDOWN);
            println("Bye!");
        } catch (IOException _) {
            // Daemon уже мёртв или недоступен — ок, всё равно выходим
        }
    }
}
