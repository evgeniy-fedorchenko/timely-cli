package com.efedorchenko.timely.command;

import com.efedorchenko.timely.client.DaemonClient;
import com.efedorchenko.timely.client.ResponsePrinter;
import com.efedorchenko.timely.protocol.Protocol;
import picocli.CommandLine.Command;

import java.io.IOException;

import static java.lang.IO.println;


@Command(
        name = "start",
        description = StartCommand.DESCRIPTION
)
public class StartCommand implements Runnable {

    public static final String DESCRIPTION = "Start tracking time. Use 'stop' to finish the session";

    private final DaemonClient client;

    /** Конструктор без аргументов, например для picocly или просто для удобства */
    public StartCommand() {
        this(new DaemonClient());
    }

    /** Для переопределения, например в тестах */
    StartCommand(DaemonClient client) {
        this.client = client;
    }

    @Override
    public void run() {

        try {
            var response = client.send(Protocol.CMD_START);
            ResponsePrinter.printStartResult(response);
        } catch (IOException e) {  // В том числе DaemonNotRunningException
            println("ERROR: " + e.getMessage());
        }
    }

}
