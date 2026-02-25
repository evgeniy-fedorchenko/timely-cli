package com.efedorchenko.timely.client;

import com.efedorchenko.timely.output.ConsolePrinter;
import com.efedorchenko.timely.output.Printer;
import com.efedorchenko.timely.protocol.Protocol;
import com.efedorchenko.timely.protocol.ResponseDecoder;

import java.io.IOException;

/**
 * Клиентский фасад для трекерных команд.
 *
 * Владеет {@link DaemonClient}, выполняет команды.
 * Парсинг ответов — через {@link ResponseDecoder}. Форматирование вывода — в {@link Printer}.
 */
public class TrackerService {

    private final DaemonClient client;
    private final Printer printer;

    public TrackerService() {
        this(new DaemonClient(), new ConsolePrinter());
    }

    TrackerService(DaemonClient client, Printer printer) {
        this.client = client;
        this.printer = printer;
    }

    public void start() {
        try {
            var response = client.send(Protocol.CMD_START);
            if (ResponseDecoder.isOk(response)) {
                printer.printStarted();
            } else {
                printer.printError(ResponseDecoder.errorMessage(response));
            }
        } catch (IOException e) {
            printer.printError(e.getMessage());
        }
    }

    public void stop() {
        try {
            var response = client.send(Protocol.CMD_STOP);
            if (ResponseDecoder.isOk(response)) {
                printer.printStopped(ResponseDecoder.parseSeconds(response));
            } else {
                printer.printError(ResponseDecoder.errorMessage(response));
            }
        } catch (IOException e) {
            printer.printError(e.getMessage());
        }
    }

    public void status() {
        try {
            var response = client.send(Protocol.CMD_STATUS);
            var status = ResponseDecoder.parseStatus(response);
            if (status == null) {
                printer.printError("Invalid response: " + response);
                return;
            }
            printer.printStatus(status.running(), status.currentSec(), status.totalSec());
        } catch (IOException e) {
            printer.printError(e.getMessage());
        }
    }

    public void shutdown() {
        try {
            var response = client.send(Protocol.CMD_SHUTDOWN);
            if (ResponseDecoder.isBye(response)) {
                printer.printShutdown(ResponseDecoder.parseSeconds(response));
            } else {
                printer.printError(ResponseDecoder.errorMessage(response));
            }
        } catch (IOException e) {
            printer.printError(e.getMessage());
        }
    }
}
