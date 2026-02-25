package com.efedorchenko.timely;

import com.efedorchenko.timely.client.DaemonClient;
import com.efedorchenko.timely.client.TrackerService;
import com.efedorchenko.timely.command.*;
import com.efedorchenko.timely.output.ConsolePrinter;
import com.efedorchenko.timely.output.Printer;
import picocli.CommandLine;
import picocli.CommandLine.IFactory;

/**
 * Фабрика для picocli: собирает граф зависимостей в одном месте.
 * <p>
 * picocli вызывает {@link #create(Class)} для создания субкоманд.
 * Команды получают готовый {@link TrackerService}, а не создают зависимости сами.
 * Всё остальное (help, version provider и т.д.) делегируется стандартной фабрике.
 */
public final class AppFactory implements IFactory {

    /**
     * IFactory - интерфейс picocli, для внедрения объектов в команды. Без него фрейворк не знает,
     * что внедрять в классы команд и приходилось использовать создание зависимостей в конструкторах
     */
    private final IFactory factory;
    private final TrackerService trackerService;
    private final Printer printer;
    private final DaemonClient client;

    public AppFactory() {
        this.factory = CommandLine.defaultFactory();
        this.client = new DaemonClient();
        this.printer = new ConsolePrinter();
        this.trackerService = new TrackerService(client, printer);
    }

    @Override
    public <K> K create(Class<K> cls) throws Exception {
        if (cls == StartCommand.class)    return cls.cast(new StartCommand(trackerService));
        if (cls == StopCommand.class)     return cls.cast(new StopCommand(trackerService));
        if (cls == StatusCommand.class)   return cls.cast(new StatusCommand(trackerService));
        if (cls == ShutdownCommand.class) return cls.cast(new ShutdownCommand(trackerService));

        return factory.create(cls);
    }
}
