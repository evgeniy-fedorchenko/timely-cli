package com.efedorchenko.timely.tracker;

import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Реализация тайм-трекера. Хранит сессии в памяти.
 * Не потокобезопасен — синхронизация на уровне вызывающего кода.
 */
public final class Tracker implements TimeTracker {

    private static final String DEFAULT_PROJECT = "default";

    private final Clock clock;
    private final List<TimeSession.Completed> completedSessions = new ArrayList<>();
    private TimeSession.Active currentSession;

    public Tracker() {
        this(Clock.systemDefaultZone());
    }

    /** Конструктор с контролем времени. */
    public Tracker(Clock clock) {
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    public TimeSession.Active start() {

        if (currentSession != null) {
            throw new IllegalStateException("Timer already running. Stop it first.");
        }

        currentSession = new TimeSession.Active(clock.instant());
        return currentSession;
    }

    @Override
    public TimeSession.Completed stop() {
        if (currentSession == null) {
            throw new IllegalStateException("Timer is not running.");
        }

        var completed = currentSession.complete(clock.instant());
        completedSessions.add(completed);
        currentSession = null;
        return completed;
    }

//    @Override
//    public Optional<TimeSession.Active> currentSession() {
//        return Optional.ofNullable(currentSession);
//    }
//
//    @Override
//    public List<TimeSession.Completed> completedSessions() {
//        return Collections.unmodifiableList(completedSessions);
//    }
//
//    @Override
//    public boolean isRunning() {
//        return currentSession != null;
//    }

    @Override
    public Duration totalCompletedTime() {
        return completedSessions.stream()
                .map(TimeSession.Completed::duration)
                .reduce(Duration.ZERO, Duration::plus);
    }

    @Override
    public Duration currentSessionDuration() {
        return currentSession != null
                ? currentSession.durationAt(clock.instant())
                : Duration.ZERO;
    }

    @Override
    public Duration totalTime() {
        return totalCompletedTime().plus(currentSessionDuration());
    }

    @Override
    public StatusInfo status() {
        return new StatusInfo(
                currentSession,
                currentSessionDuration(),
                totalCompletedTime(),
                totalTime()
        );
    }
}