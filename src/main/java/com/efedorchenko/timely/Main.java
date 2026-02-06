package com.efedorchenko.timely;

import com.efedorchenko.timely.client.InteractiveCli;
import com.efedorchenko.timely.command.*;
import picocli.CommandLine;

/**
 * Точка входа. Делегирует всё picocli
 */
@CommandLine.Command(
        name = "Timely CLI",
        description = "Console time tracker",
        mixinStandardHelpOptions = true,  // Авто --help и --version
        versionProvider = VersionProvider.class,
        subcommands = {
                DaemonCommand.class,
                StartCommand.class,
                StopCommand.class,
                StatusCommand.class,
                ShutdownCommand.class
        }
)
public class Main implements Runnable {

    @CommandLine.Option(names = "--dev", description = "Interactive CLI mode")
    private boolean devMode;


    static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        if (devMode) {
            new InteractiveCli().run();
        } else {
            CommandLine.usage(this, System.out);
        }
    }
}
