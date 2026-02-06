package com.efedorchenko.timely.command;

import com.efedorchenko.timely.client.DaemonClient;
import com.efedorchenko.timely.client.ResponsePrinter;
import com.efedorchenko.timely.protocol.Protocol;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.IOException;
import java.util.List;

import static java.lang.IO.println;


@Command(
        name = "start",
        description = "Start timer for a project"
)
public class StartCommand implements Runnable {

//    @Parameters(description = "Project name (can have multiple words)", arity = "0..*")
//    private List<String> projectWords;

    @Override
    public void run() {
        var client = new DaemonClient();

        try {
            var response = client.send(Protocol.CMD_START);
            ResponsePrinter.printStartResult(response);
        } catch (IOException e) {  // В том числе DaemonNotRunningException
            println("ERROR: " + e.getMessage());
        }
    }

//    private String createDaemonCommand() {
//        var project = projectWords != null ? String.join(" ", projectWords) : null;
//        return project != null ? Protocol.CMD_START + " " + project : Protocol.CMD_START;
//    }
}
