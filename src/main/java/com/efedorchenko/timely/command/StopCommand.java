package com.efedorchenko.timely.command;

import com.efedorchenko.timely.client.TrackerService;
import picocli.CommandLine.Command;


@Command(
        name = "stop",
        description = StopCommand.DESCRIPTION
)
public class StopCommand implements Runnable {

    public static final String DESCRIPTION = "Stop tracking and save the session. Shows elapsed time";

    private final TrackerService service;

    public StopCommand() {
        this(new TrackerService());
    }

    /** Для переопределения, например в тестах */
    StopCommand(TrackerService service) {
        this.service = service;
    }

    @Override
    public void run() {
        service.stop();
    }
}
