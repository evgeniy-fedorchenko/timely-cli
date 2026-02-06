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
 *
 * Не покрыт тестами, тк это dev-инструмент, а не production код.
 * Для тестирования надо было быинжектить вывод (статический println),
 * что избыточно для dev-режима. Тем более, что омпоненты (DaemonClient,
 * DaemonServer, Protocol) протестированы отдельно
 */
public final class InteractiveCli {

    private static final Set<String> KNOWN_COMMANDS = Set.of(
            Protocol.CMD_START, Protocol.CMD_STOP, Protocol.CMD_STATUS, Protocol.CMD_PING
//            Еще exit, но это команда cli-режима, а не общения с демоном, как собственно help/version
    );

    private static final String WELCOME_MESS = """
        \nWelcome to Timely CLI v%s — time tracker
        Type 'help' for commands, 'exit' to quit
        """.formatted(AppProperties.version());

    private final DaemonClient client;

    /* Сохраняем ссылку на демона, которого запустили сами. Если найден
       уже работающий демон - не сохраняем. Это нужно, чтобы при exit
       вырубить своего демона, только если мы сами его запустили */
    private DaemonServer ownDaemon;

    /** Флаг для предотвращения повторного вызова exit() из shutdown hook */
    private volatile boolean exited = false;

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

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            println();
            exit();
        }));

        println(WELCOME_MESS);
        var scanner = new Scanner(System.in);
        this.run(scanner);
    }

    private void run(Scanner scanner) {
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

        // Присваиваем в поле тут. Иначе теоретически exit() может вызваться до присвоения ownDaemon
        ownDaemon = daemon;

        for (int i = 0; i < 20; i++) {
            if (client.isDaemonRunning()) {
                println("Daemon started");
                return true;
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException _) {
                break;  // Прерывание при ожидании — выходим из цикла
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
            ResponsePrinter.printResponse(command, response);

        } catch (IOException e) {  // В том числе DaemonNotRunningException
            println("Error: " + e.getMessage());
        }
    }

    /** Сначала получаем статус, чтоб показать, что получилось по итогу, потом останавливаемся */
    private void exit() {
        if (exited) return;
        exited = true;

        try {
            var stopResponse = client.send(Protocol.CMD_STOP);
            if (Protocol.isOk(stopResponse)) {
                ResponsePrinter.printStopResult(stopResponse);
            }
        } catch (IOException _) { }  // Daemon недоступен — выходим так

        try {
            var response = client.send(Protocol.CMD_STATUS);
            var status = Protocol.parseStatus(response);
            if (status == null) return;

            if (status.running()) {
                println("Timer was running: " + DurationFormatter.format(status.currentSec()));
            }
            if (status.totalSec() > 0) {
                println("Total: " + DurationFormatter.format(status.totalSec()));
            }

        } catch (IOException _) { }  // Daemon недоступен — выходим без статистики

        if (ownDaemon != null) {
            try {
                client.send(Protocol.CMD_SHUTDOWN);
            } catch (IOException _) { }  // Daemon уже мёртв или недоступен — ок, всё равно выходим
        }

        println("Bye!");
    }
}
