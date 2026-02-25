package com.efedorchenko.timely.command;

import com.efedorchenko.timely.client.TrackerService;
import picocli.CommandLine.Command;


@Command(
        name = "start",
        description = StartCommand.DESCRIPTION
)
public class StartCommand implements Runnable {

    public static final String DESCRIPTION = "Start tracking time. Use 'stop' to finish the session";

    private final TrackerService service;

    public StartCommand() {
        this(new TrackerService());
    }

    /** Для переопределения, например в тестах */
    StartCommand(TrackerService service) {
        this.service = service;
    }

    @Override
    public void run() {
        service.start();
    }

}
