package com.efedorchenko.timely.command;

import com.efedorchenko.timely.daemon.DaemonServer;
import com.efedorchenko.timely.tracker.Tracker;
import picocli.CommandLine.Command;

import java.io.IOException;

import static java.lang.IO.println;


@Command(
        name = "daemon",
        description = DaemonCommand.DESCRIPTION
)
public class DaemonCommand implements Runnable {

    public static final String DESCRIPTION = "Start background daemon process for time tracking";

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