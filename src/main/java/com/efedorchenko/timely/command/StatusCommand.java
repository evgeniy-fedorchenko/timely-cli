package com.efedorchenko.timely.command;

import com.efedorchenko.timely.client.TrackerService;
import picocli.CommandLine.Command;


@Command(
        name = "status",
        description = StatusCommand.DESCRIPTION
)
public class StatusCommand implements Runnable {

    public static final String DESCRIPTION = "Show current session and total tracked time";

    private final TrackerService service;

    public StatusCommand() {
        this(new TrackerService());
    }

    /** Для переопределения, например в тестах */
    StatusCommand(TrackerService service) {
        this.service = service;
    }

    @Override
    public void run() {
        service.status();
    }
}