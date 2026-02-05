package com.efedorchenko.timely.tracker;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * Сессия отслеживания времени.
 *
 * <p>Sealed interface с двумя реализациями:
 * <ul>
 *   <li>{@link Active} — активная сессия (таймер запущен)</li>
 *   <li>{@link Completed} — завершённая сессия (таймер остановлен)</li>
 * </ul>
 *
 * <p>Такой подход обеспечивает type-safety: невозможно случайно обратиться
 * к endTime у активной сессии.
 */
public sealed interface TimeSession permits TimeSession.Active, TimeSession.Completed {

    /**
     * Название проекта. На текущем этапе не используется в логике,
     * но сохраняется для будущих версий.
     *
     * @return название проекта, не null
     */
    String project();

    /**
     * Время начала сессии.
     *
     * @return момент старта, не null
     */
    Instant startTime();

    /**
     * Активная сессия — таймер запущен, но ещё не остановлен.
     *
     * @param project   название проекта
     * @param startTime время начала
     */
    record Active(String project, Instant startTime) implements TimeSession {

        public Active {
            Objects.requireNonNull(project, "project must not be null");
            Objects.requireNonNull(startTime, "startTime must not be null");
        }

        /**
         * Вычисляет длительность сессии на указанный момент времени.
         *
         * @param now текущий момент времени
         * @return длительность от startTime до now
         */
        public Duration durationAt(Instant now) {
            Objects.requireNonNull(now, "now must not be null");
            return Duration.between(startTime, now);
        }

        /**
         * Завершает сессию, создавая {@link Completed}.
         *
         * @param endTime время завершения
         * @return завершённая сессия
         */
        public Completed complete(Instant endTime) {
            Objects.requireNonNull(endTime, "endTime must not be null");
            return new Completed(project, startTime, endTime);
        }
    }

    /**
     * Завершённая сессия — таймер остановлен.
     *
     * @param project   название проекта
     * @param startTime время начала
     * @param endTime   время завершения
     */
    record Completed(String project, Instant startTime, Instant endTime) implements TimeSession {

        public Completed {
            Objects.requireNonNull(project, "project must not be null");
            Objects.requireNonNull(startTime, "startTime must not be null");
            Objects.requireNonNull(endTime, "endTime must not be null");
            if (endTime.isBefore(startTime)) {
                throw new IllegalArgumentException("endTime cannot be before startTime");
            }
        }

        /**
         * Вычисляет длительность сессии.
         *
         * @return длительность от startTime до endTime
         */
        public Duration duration() {
            return Duration.between(startTime, endTime);
        }
    }
}