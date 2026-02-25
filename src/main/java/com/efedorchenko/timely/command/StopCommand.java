package com.efedorchenko.timely.command;

import com.efedorchenko.timely.client.TrackerService;
import picocli.CommandLine.Command;


@Command(
        name = StopCommand.NAME,
        description = StopCommand.DESCRIPTION
)
public class StopCommand implements Runnable {

    public static final String NAME = "stop";
    public static final String DESCRIPTION = "Stop tracking and save the session. Shows elapsed time";

    private final TrackerService service;

    public StopCommand(TrackerService service) {
        this.service = service;
    }

    @Override
    public void run() {
        service.stop();
    }
}
