package com.efedorchenko.timely.command;

import com.efedorchenko.timely.client.DaemonClient;
import com.efedorchenko.timely.client.ResponsePrinter;
import com.efedorchenko.timely.protocol.Protocol;
import picocli.CommandLine.Command;

import java.io.IOException;

import static java.lang.IO.println;


@Command(
        name = "status",
        description = "Show tracked time"
)
public class StatusCommand implements Runnable {

    @Override
    public void run() {

        try {
            var response = new DaemonClient().send(Protocol.CMD_STATUS);
            ResponsePrinter.printStatus(response);

        } catch (IOException e) {  // В том числе DaemonNotRunningException
            println("ERROR: " + e.getMessage());
        }
    }
}