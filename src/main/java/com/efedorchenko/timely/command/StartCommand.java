package com.efedorchenko.timely.command;

import com.efedorchenko.timely.client.TrackerService;
import picocli.CommandLine.Command;


@Command(
        name = StartCommand.NAME,
        description = StartCommand.DESCRIPTION
)
public class StartCommand implements Runnable {

    public static final String NAME = "start";
    public static final String DESCRIPTION = "Start tracking time. Use 'stop' to finish the session";

    private final TrackerService service;

    public StartCommand(TrackerService service) {
        this.service = service;
    }

    @Override
    public void run() {
        service.start();
    }

}
