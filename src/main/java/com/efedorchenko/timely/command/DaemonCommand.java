package com.efedorchenko.timely.command;

import com.efedorchenko.timely.daemon.DaemonServer;
import com.efedorchenko.timely.tracker.Tracker;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;

import static java.lang.IO.println;


@Command(
        name = "daemon",
        description = "Start background daemon process"
)
public class DaemonCommand implements Runnable {

    @Override
    public void run() {
        var tracker = new Tracker();
        var server = new DaemonServer(tracker);

        try {
            server.start();
        } catch (IOException e) {
            println("ERROR: Failed to start daemon: " + e.getMessage());
        }
    }
}