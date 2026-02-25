package com.efedorchenko.timely.command;

import com.efedorchenko.timely.client.DaemonClient;
import com.efedorchenko.timely.daemon.DaemonServer;
import com.efedorchenko.timely.tracker.Tracker;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;

import static java.lang.IO.println;


/**
 * Запуск daemon в prod-режиме.
 *
 * Как это работает:
 * {@code timely demon} запускает {@link #runBackground()}, который запускает {@code timely demon --foreground},
 * ждем пока в фоновом процессе запустится настоящий демон и завершается. Демон продолжает работать в процессе,
 * отвязанном от терминала до срабатывания {@code ShutdownHook}
 * <blockquote><pre>
 *     Терминал пользователя                     Фоновый процесс
 *     ─────────────────────                     ───────────────
 *     >$ timely daemon
 *          └── runBackground()
 *                ├── запускает процесс  ──>  timely daemon --foreground
 *                ├── ждёт 300ms                 └── runForeground()
 *                ├── "Daemon started"                 ├── server.start()
 *                └── завершается                      ├── while(running)...
 *                                                     └── (работает пока не shutdown)
 *     >$ _  <- терминал свободен
 * </pre></blockquote>
 *
 * Флаг --foreground скрыт от пользователя, используется только
 * для запуска сервера в фоновом процессе.
 *
 * Не покрыт unit-тестами - зависит от ProcessBuilder и fork процесса.
 * Компоненты (DaemonServer, DaemonClient, Tracker) протестированы отдельно.
 * Да и просто ProcessBuilder, ShutdownHook и тд тестить - быстрее с ума сойти
 */
@Command(name = "daemon", description = DaemonCommand.DESCRIPTION)
public class DaemonCommand implements Runnable {

    public static final String DESCRIPTION = "Start background daemon process for time tracking";

    @Option(names = "--foreground", hidden = true)
    private boolean foreground;

    @Override
    public void run() {
        if (foreground) {
            runForeground();
        } else {
            runBackground();
        }
    }

    /** Запускает сервер. Блокирует, но это фоновый процесс — пользователь не ждёт */
    private void runForeground() {
        var tracker = new Tracker();
        var server = new DaemonServer(tracker);

        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));

        try {
            server.start();
        } catch (IOException _) {
        }
    }

    /**
     * Форкает процесс с --foreground и отдаёт терминал пользователю.
     * <p>
     * Печать напрямую в stderr - намеренно: это инфраструктурный код запуска процесса,
     * не трекерная операция, так что принтер не подходит. Когда появится нормальный логгер - использовать его.
     */
    private void runBackground() {
        try {
            var executable = ProcessHandle.current().info().command().orElse("timely");

            new ProcessBuilder(executable, "daemon", "--foreground")
                    .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                    .redirectError(ProcessBuilder.Redirect.DISCARD)
                    .start();

            Thread.sleep(300);

            var client = new DaemonClient();
            if (client.isDaemonRunning()) {
                println("Daemon started");
            } else {
                System.err.println("ERROR: Daemon failed to start");
            }
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
        }
    }
}