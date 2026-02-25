package com.efedorchenko.timely.client;

import com.efedorchenko.timely.format.DurationFormatter;

import static java.lang.IO.println;

/** Реализация {@link Printer}, печатающая в стандартный вывод. */
public class ConsolePrinter implements Printer {

    @Override
    public void printStarted() {
        println("Timer started");
    }

    @Override
    public void printStopped(long seconds) {
        println("Stopped: " + DurationFormatter.format(seconds));
    }

    @Override
    public void printStatus(boolean running, long currentSec, long totalSec) {
        println("State: " + (running ? "running" : "not running"));
        if (running) {
            println("Running session: " + DurationFormatter.format(currentSec));
        }
        var sb = new StringBuilder("Total ");
        sb.append(running ? "(include running): " : ": ");
        sb.append(DurationFormatter.format(totalSec));
        println(sb.toString());
    }

    @Override
    public void printShutdown(long totalSeconds) {
        println("Daemon stopped. Total: " + DurationFormatter.format(totalSeconds));
    }

    @Override
    public void printError(String message) {
        System.err.println("ERROR: " + message);
    }
}
