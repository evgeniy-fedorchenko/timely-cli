package com.efedorchenko.timely.daemon;

import com.efedorchenko.timely.AppProperties;
import com.efedorchenko.timely.protocol.Protocol;
import com.efedorchenko.timely.protocol.ResponseEncoder;
import com.efedorchenko.timely.tracker.TimeTracker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;

/**
 * TCP-сервер daemon-процесса.
 *
 * Слушает порт, принимает команды от CLI, выполняет через {@link TimeTracker}.
 * Обрабатывает соединения последовательно (одно за раз).
 * В dev-режиме выводит логи в консоль.
 */
public final class DaemonServer {

    private final TimeTracker tracker;
    private final int port;

    private volatile boolean running;
    private volatile boolean shutdownAfterCurrentRequest = false;

    private ServerSocket serverSocket;

    public DaemonServer(TimeTracker tracker) {
        this(tracker, AppProperties.daemonPort());
    }

    public DaemonServer(TimeTracker tracker, int port) {
        this.tracker = Objects.requireNonNull(tracker);
        this.port = port;
    }

    /**
     * Запускает сервер. Блокирует поток до вызова {@link #stop()}.
     */
    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        running = true;
        log("Daemon started on port " + port);

        while (running) {
            try {
                var clientSocket = serverSocket.accept();
                handleClient(clientSocket);

                if (shutdownAfterCurrentRequest) {
                    stop();
                }
            } catch (IOException e) {
                // Ошибка чтения с сокета, так что не получится отправить
                // клиенту ERROR, но он и так узнает, что сокет отвалился
                log("Error: " + e.getMessage());
            }
        }
        log("Daemon stopped");
    }

    /** Останавливает сервер. Можно вызывать из любого потока. */
    public void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            log("Error closing socket: " + e.getMessage());
        }
    }

    private void handleClient(Socket socket) {
        try (socket;
             var reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             var writer = new PrintWriter(socket.getOutputStream(), true)) {

            var command = reader.readLine();
            if (command == null) return;

            log("<- " + command);

            var response = processCommand(command);
            writer.println(response);

            log("-> " + response);

        } catch (IOException e) {
            log("Error handling client: " + e.getMessage());
        }
    }

    /** Синхронизирован для потокобезопасности Tracker. */
    private synchronized String processCommand(String command) {
        var type = Protocol.commandType(command);

        return switch (type) {
            case Protocol.CMD_PING -> ResponseEncoder.pong();
            case Protocol.CMD_START -> handleStart(/*command*/);
            case Protocol.CMD_STOP -> handleStop();
            case Protocol.CMD_STATUS -> handleStatus();
            case Protocol.CMD_SHUTDOWN -> handleShutdown();
            default -> ResponseEncoder.error("Unknown command: " + type);
        };
    }

    private String handleStart() {
        try {
            tracker.start();
            return ResponseEncoder.ok();
        } catch (IllegalStateException e) {
            return ResponseEncoder.error(e.getMessage());
        }
    }

    private String handleStop() {
        try {
            var session = tracker.stop();
            return ResponseEncoder.okWithSeconds(session.duration().toSeconds());
        } catch (IllegalStateException e) {
            return ResponseEncoder.error(e.getMessage());
        }
    }

    private String handleStatus() {
        var status = tracker.status();
        return ResponseEncoder.statusResponse(
                status.currentSessionTime().toSeconds(),
                status.totalTime().toSeconds(),
                status.isRunning()
        );
    }

    private String handleShutdown() {
        var totalSec = tracker.totalTime().toSeconds();
        shutdownAfterCurrentRequest = true;
        return ResponseEncoder.bye(totalSec);
    }

    private void log(String message) {
//     TODO: пишем логи в файл. Внешние хранилища нельзя по заданию, поэтому пока pass
//      println("[daemon " + time + "] " + message);

    }
}