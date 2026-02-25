package com.efedorchenko.timely.client;

/**
 * Выводит результаты трекерных команд пользователю.
 * Отвечает за форматирование и способ вывода - реализация решает, куда и как.
 */
public interface Printer {

    /** Сессия успешно запущена. */
    void printStarted();

    /** Сессия остановлена. {@code seconds} - длительность сессии в секундах. */
    void printStopped(long seconds);

    /**
     * Текущий статус трекера.
     *
     * @param running    активна ли сессия
     * @param currentSec длительность текущей сессии в секундах (0 если не запущена)
     * @param totalSec   суммарное время включая текущую сессию
     */
    void printStatus(boolean running, long currentSec, long totalSec);

    /** Daemon остановлен. {@code totalSeconds} - суммарное отслеженное время в секундах. */
    void printShutdown(long totalSeconds);

    /** Вывод сообщения об ошибке. */
    void printError(String message);
}
