package com.efedorchenko.timely.tracker;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Основная бизнес-логика тайм-трекера.
 *
 * <p>Хранит текущую активную сессию (если есть) и список завершённых сессий.
 * Предоставляет операции start, stop, status.
 *
 * <p>Потокобезопасность: класс НЕ потокобезопасен. Синхронизация
 * обеспечивается на уровне {@code DaemonServer}.
 *
 * <p>Для тестируемости принимает {@link Clock} — позволяет
 * контролировать "текущее время" в тестах.
 */
public class TrackerImpl implements Tracker {

    private static final String DEFAULT_PROJECT = "default";

    private final Clock clock;
    private final List<TimeSession.Completed> completedSessions;
    private TimeSession.Active currentSession;

    /**
     * Создаёт трекер с системными часами.
     */
    public TrackerImpl() {
        this(Clock.systemDefaultZone());
    }

    /**
     * Создаёт трекер с указанными часами.
     *
     * @param clock часы для получения текущего времени
     */
    public TrackerImpl(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
        this.completedSessions = new ArrayList<>();
        this.currentSession = null;
    }

    /**
     * Запускает таймер для проекта по умолчанию.
     *
     * @return созданная активная сессия
     * @throws IllegalStateException если таймер уже запущен
     */
    @Override
    public TimeSession.Active start() {
        return start(DEFAULT_PROJECT);
    }

    /**
     * Запускает таймер для указанного проекта.
     *
     * @param project название проекта
     * @return созданная активная сессия
     * @throws IllegalStateException если таймер уже запущен
     */
    @Override
    public TimeSession.Active start(String project) {
        Objects.requireNonNull(project, "project must not be null");

        if (currentSession != null) {
            throw new IllegalStateException(
                    "Timer already running: " + currentSession.project() + ". Stop it first.");
        }

        Instant now = clock.instant();
        currentSession = new TimeSession.Active(project, now);
        return currentSession;
    }

    /**
     * Останавливает таймер.
     *
     * @return завершённая сессия с итоговой длительностью
     * @throws IllegalStateException если таймер не запущен
     */
    @Override
    public TimeSession.Completed stop() {
        if (currentSession == null) {
            throw new IllegalStateException("Timer is not running.");
        }

        Instant now = clock.instant();
        TimeSession.Completed completed = currentSession.complete(now);
        completedSessions.add(completed);
        currentSession = null;
        return completed;
    }

    /**
     * Возвращает текущую активную сессию, если есть.
     *
     * @return Optional с активной сессией или пустой Optional
     */
    @Override
    public Optional<TimeSession.Active> currentSession() {
        return Optional.ofNullable(currentSession);
    }

    /**
     * Возвращает неизменяемый список завершённых сессий.
     *
     * @return список завершённых сессий
     */
    @Override
    public List<TimeSession.Completed> completedSessions() {
        return Collections.unmodifiableList(completedSessions);
    }

    /**
     * Проверяет, запущен ли таймер.
     *
     * @return true если таймер активен
     */
    @Override
    public boolean isRunning() {
        return currentSession != null;
    }

    /**
     * Вычисляет общее затреканное время (все завершённые сессии).
     *
     * @return суммарная длительность завершённых сессий
     */
    @Override
    public Duration totalCompletedTime() {
        return completedSessions.stream()
                .map(TimeSession.Completed::duration)
                .reduce(Duration.ZERO, Duration::plus);
    }

    /**
     * Вычисляет длительность текущей сессии (если запущена).
     *
     * @return длительность текущей сессии или Duration.ZERO если не запущена
     */
    @Override
    public Duration currentSessionDuration() {
        if (currentSession == null) {
            return Duration.ZERO;
        }
        return currentSession.durationAt(clock.instant());
    }

    /**
     * Вычисляет общее время: завершённые сессии + текущая (если есть).
     *
     * @return полная суммарная длительность
     */
    @Override
    public Duration totalTime() {
        return totalCompletedTime().plus(currentSessionDuration());
    }

    /**
     * Формирует строку статуса для вывода пользователю.
     *
     * <p>Формат зависит от состояния:
     * <ul>
     *   <li>Таймер запущен: показывает текущую сессию и общее время</li>
     *   <li>Таймер остановлен: показывает только общее время</li>
     * </ul>
     *
     * @return форматированная строка статуса
     */
    @Override
    public StatusInfo status() {
        return new StatusInfo(
                currentSession,
                currentSessionDuration(),
                totalCompletedTime(),
                totalTime()
        );
    }

    /**
     * Информация о статусе трекера.
     *
     * @param currentSession       текущая сессия или null
     * @param currentSessionTime   длительность текущей сессии
     * @param totalCompletedTime   суммарное время завершённых сессий
     * @param totalTime            общее время (completed + current)
     */
    public record StatusInfo(
            TimeSession.Active currentSession,
            Duration currentSessionTime,
            Duration totalCompletedTime,
            Duration totalTime
    ) {
        /**
         * Проверяет, запущен ли таймер.
         *
         * @return true если есть активная сессия
         */
        public boolean isRunning() {
            return currentSession != null;
        }
    }
}