package com.efedorchenko.timely.command;

import com.efedorchenko.timely.client.DaemonClient;
import com.efedorchenko.timely.client.ResponsePrinter;
import com.efedorchenko.timely.protocol.Protocol;
import picocli.CommandLine.Command;

import java.io.IOException;

import static java.lang.IO.println;


@Command(
        name = "status",
        description = StatusCommand.DESCRIPTION
)
public class StatusCommand implements Runnable {

    public static final String DESCRIPTION = "Show current session and total tracked time";

    private final DaemonClient client;

    /** Конструктор без аргументов, например для picocly или просто для удобства */
    public StatusCommand() {
        this(new DaemonClient());
    }

    /** Для переопределения, например в тестах */
    StatusCommand(DaemonClient client) {
        this.client = client;
    }

    @Override
    public void run() {

        try {
            var response = client.send(Protocol.CMD_STATUS);
            ResponsePrinter.printStatus(response);

        } catch (IOException e) {  // В том числе DaemonNotRunningException
            println("ERROR: " + e.getMessage());
        }
    }
}