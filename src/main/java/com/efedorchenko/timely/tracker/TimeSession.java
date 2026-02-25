package com.efedorchenko.timely.tracker;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * Сессия трекинга. Active — таймер идёт, Completed — остановлен.
 */
public sealed interface TimeSession permits TimeSession.Active, TimeSession.Completed {

    Instant startTime();

    record Active(Instant startTime) implements TimeSession {

        public Active {
            Objects.requireNonNull(startTime);
        }

        public Duration durationAt(Instant now) {
            return Duration.between(startTime, now);
        }

        public Completed complete(Instant endTime) {
            return new Completed(startTime, endTime);
        }
    }

    record Completed(Instant startTime, Instant endTime) implements TimeSession {

        public Completed {
            Objects.requireNonNull(startTime);
            Objects.requireNonNull(endTime);
            if (endTime.isBefore(startTime)) {
                throw new IllegalArgumentException("endTime cannot be before startTime");
            }
        }

        public Duration duration() {
            return Duration.between(startTime, endTime);
        }
    }
}