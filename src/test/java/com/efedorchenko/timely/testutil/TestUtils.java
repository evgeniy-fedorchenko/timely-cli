package com.efedorchenko.timely.testutil;

import com.efedorchenko.timely.client.DaemonClient;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Утилиты для тестов
 */
public final class TestUtils {

    private TestUtils() {}

    /** Находит свободный порт для тестового сервера */
    public static int findFreePort() {
        try (var socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new RuntimeException("Failed to find free port", e);
        }
    }

    /** Ожидает запуска сервера на указанном порту */
    public static void waitForServer(int port) {
        waitForServer("localhost", port, 50, 10);
    }

    /**
     * Ожидает запуска сервера с настраиваемыми параметрами
     *
     * @param host        хост сервера
     * @param port        порт сервера
     * @param maxAttempts максимальное количество попыток
     * @param delayMs     задержка между попытками в миллисекундах
     */
    public static void waitForServer(String host, int port, int maxAttempts, int delayMs) {
        for (int i = 0; i < maxAttempts; i++) {
            try {
                var client = new DaemonClient(host, port);
                if (client.isDaemonRunning()) {
                    return;
                }
            } catch (Exception _) {
                // Ещё не готов
            }
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for server", e);
            }
        }
        throw new IllegalStateException("Server failed to start on port " + port);
    }
}