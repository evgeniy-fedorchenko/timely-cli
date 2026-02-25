package com.efedorchenko.timely.daemon;

import com.efedorchenko.timely.tracker.Tracker;

import java.io.IOException;

import static java.lang.IO.println;

/** Запускает daemon в виртуальном потоке и паркует вызывающий поток. Только для IDEA. */
public final class DevDaemon {

    public static void start() {
        var daemon = new DaemonServer(new Tracker());
        Runtime.getRuntime().addShutdownHook(new Thread(daemon::stop));

        Thread.startVirtualThread(() -> {
            try {
                daemon.start();
            } catch (IOException e) {
                // Печать напрямую в stderr - намеренно: dev-утилита, логгер/принтер здесь избыточен
                System.err.println("ERROR: " + e.getMessage());
            }
        });

        println("Dev daemon started. Use run-start / run-stop / run-status configs.");
        println("\n!! You don't need to send commands here !!\n");
        println("it's done through client launches, see doc in README.md");
        try {
            Thread.currentThread().join();
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
        }
    }
}
