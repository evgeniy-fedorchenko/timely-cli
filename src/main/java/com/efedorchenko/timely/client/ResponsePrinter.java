package com.efedorchenko.timely.client;


import com.efedorchenko.timely.AppProperties;
import com.efedorchenko.timely.command.StartCommand;
import com.efedorchenko.timely.command.StatusCommand;
import com.efedorchenko.timely.command.StopCommand;
import com.efedorchenko.timely.format.DurationFormatter;
import com.efedorchenko.timely.protocol.Protocol;

import static java.lang.IO.println;

public final class ResponsePrinter {

    private ResponsePrinter() { }

    public static void printStartResult(String response) {
        if (Protocol.isOk(response)) {
            println("Timer started");
        } else {
            println("ERROR: " + Protocol.errorMessage(response));
        }
    }

    public static void printStopResult(String response) {
        if (Protocol.isOk(response)) {
            var seconds = Long.parseLong(response.substring(Protocol.RESP_OK.length()).trim());
            println("Timer stopped: " + DurationFormatter.format(seconds));
        } else {
            println("ERROR: " + Protocol.errorMessage(response));
        }
    }

    public static void printStatus(String response) {
        var status = Protocol.parseStatus(response);

        if (status == null) {
            println("ERROR: Invalid response: " + response);
            return;
        }
        if (status.running()) {
            println(DurationFormatter.format(status.currentSec()) + " (running)");
        }

        println("Total: " + DurationFormatter.format(status.totalSec()));
    }

    public static void printVersion() {
        println(AppProperties.version());
    }

    /**
     * Обычно help-сообщение выводит picocli, но в интерактивном режиме она чет не справляется сама
     */
    // TODO 05.02.2026 20:38: проверить что не справляется
    // TODO 05.02.2026 20:39: проверить, как справляется с другими командами
    public static void printHelp() {
        println("""
            start   - %s
            stop    - %s
            status  - %s
            ping    - Check daemon running
            """.formatted(
                StartCommand.DESCRIPTION,
                StopCommand.DESCRIPTION,
                StatusCommand.DESCRIPTION
        ));
    }

}
