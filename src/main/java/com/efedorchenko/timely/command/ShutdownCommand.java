package com.efedorchenko.timely.command;

import com.efedorchenko.timely.client.DaemonClient;
import com.efedorchenko.timely.format.DurationFormatter;
import com.efedorchenko.timely.protocol.Protocol;
import picocli.CommandLine.Command;

import java.io.IOException;

import static java.lang.IO.println;


@Command(
        name = "shutdown",
        description = ShutdownCommand.DESCRIPTION
)
public class ShutdownCommand implements Runnable {

    public static final String DESCRIPTION = "Stop the daemon and save session data (not impl, only show)";

    @Override
    public void run() {

        try {
            var response = new DaemonClient().send(Protocol.CMD_SHUTDOWN);

            if (Protocol.isBye(response)) {
                var seconds = Long.parseLong(response.substring(Protocol.RESP_BYE.length()).trim());
                println("Daemon stopped. Total: " + DurationFormatter.format(seconds));
            } else {
                println(response);
            }
        } catch (IOException e) {  // В том числе DaemonNotRunningException
            println("ERROR: " + e.getMessage());
        }    }
}