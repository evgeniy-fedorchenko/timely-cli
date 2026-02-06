package com.efedorchenko.timely.command;


import com.efedorchenko.timely.client.DaemonClient;
import com.efedorchenko.timely.format.DurationFormatter;
import com.efedorchenko.timely.protocol.Protocol;
import picocli.CommandLine.Command;

import java.io.IOException;

import static java.lang.IO.println;


@Command(
        name = "stop",
        description = "Stop running timer"
)
public class StopCommand implements Runnable {

    @Override
    public void run() {
        var client = new DaemonClient();

        try {
            var response = client.send(Protocol.CMD_STOP);

            if (Protocol.isOk(response)) {
                var seconds = Long.parseLong(response.substring(Protocol.RESP_OK.length()).trim());
                println("Stopped: " + DurationFormatter.format(seconds));
            } else {
                println("ERROR: " + Protocol.errorMessage(response));
            }
        } catch (IOException e) {  // В том числе DaemonNotRunningException
            println("ERROR: " + e.getMessage());
        }
    }
}
