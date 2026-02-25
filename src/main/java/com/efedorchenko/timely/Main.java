package com.efedorchenko.timely;

import com.efedorchenko.timely.command.*;
import com.efedorchenko.timely.daemon.DevDaemon;
import picocli.CommandLine;

/**
 * Точка входа. Делегирует всё picocli
 */
@CommandLine.Command(
        name = "timely",
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

    @CommandLine.Option(names = "--dev", description = "Start daemon in-process (for IDE use only)", hidden = true)
    private boolean devMode;

    static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        if (devMode) {
            DevDaemon.start();
        } else {
            CommandLine.usage(this, System.out);
        }
    }
}
