package com.efedorchenko.timely;

import com.efedorchenko.timely.command.*;
import com.efedorchenko.timely.daemon.DevDaemon;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * Точка входа. Делегирует всё picocli.
 *
 * <h2>Рекомендуемый способ запуска</h2>
 * Запуск native-сборки проще, чем из IDE, и передаёт весь смысл использования.
 * Готовые бинарники в каталоге {@code artifact/}:
 * <ol>
 *     <li>{@code artifact/v2.0/timely daemon} - старт демона</li>
 *     <li>{@code artifact/v2.0/timely start/stop/status} - работа с приложением</li>
 *     <li>{@code artifact/v2.0/timely shutdown} - завершение демона</li>
 * </ol>
 *
 * <h2>Запуск в продакшене</h2>
 * Рассчитан на native-image: {@code timely daemon} форкает себя через {@link ProcessBuilder},
 * не имея зависимости от среды
 * <pre>
 *   timely daemon     # запускает фоновый процесс
 *   timely start      # отправляет TCP-команду daemon'у и завершается
 *   timely stop
 *   timely status
 *   timely shutdown   # останавливает daemon
 * </pre>
 *
 * <h2>Запуск из IDE</h2>
 * Используйте run-конфигурации из {@code .run/}:
 * <ol>
 *   <li>Запустите <b>dev-daemon</b>, это - поднимает daemon в виртуальном потоке, в блокирующем режиме</li>
 *   <li>Запускайте run-конфиги <b>run-start/run-stop/run-status</b> по очереди</li>
 *   <li>Для остановки - <b>run-shutdown</b> или Stop на <b>dev-daemon</b></li>
 * </ol>
 * Но сразу скажу, что это неудобно, лучше используйте рекомендуемый способ
 *
 * @apiNote Флаг {@code --dev} не является частью продового API.
 */
@Command(
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

    @Option(names = "--dev", description = "Start daemon in-process (for IDE use only)", hidden = true)
    private boolean devMode;

    static void main(String[] args) {
        int exitCode = new CommandLine(new Main(), new AppFactory()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        if (devMode) {
//            dev-режим просто запускает deamon-сервер. В этом была основная сложность поддержки dev-режима. Сейчас
//            ответственность dev-режима минимальна. Команды он принимает от приложения, работающего в обычном режиме
            DevDaemon.start();
        } else {
            CommandLine.usage(this, System.out);
        }
    }
}
