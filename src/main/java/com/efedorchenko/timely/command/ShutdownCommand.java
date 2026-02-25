package com.efedorchenko.timely.command;

import com.efedorchenko.timely.client.TrackerService;
import picocli.CommandLine.Command;


@Command(
        name = ShutdownCommand.NAME,
        description = ShutdownCommand.DESCRIPTION
)
public class ShutdownCommand implements Runnable {

    public static final String NAME = "shutdown";
    public static final String DESCRIPTION = "Stop the daemon and save session data (not impl, only show)";

    private final TrackerService service;

    public ShutdownCommand(TrackerService service) {
        this.service = service;
    }

    @Override
    public void run() {
        service.shutdown();
    }
}