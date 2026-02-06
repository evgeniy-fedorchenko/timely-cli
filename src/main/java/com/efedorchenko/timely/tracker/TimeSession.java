package com.efedorchenko.timely.tracker;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * Сессия трекинга. Active — таймер идёт, Completed — остановлен.
 */
public sealed interface TimeSession permits TimeSession.Active, TimeSession.Completed {

//    String project();
    Instant startTime();

    record Active(/*String project,*/ Instant startTime) implements TimeSession {

        public Active {
//            Objects.requireNonNull(project);
            Objects.requireNonNull(startTime);
        }

        public Duration durationAt(Instant now) {
            return Duration.between(startTime, now);
        }

        public Completed complete(Instant endTime) {
            return new Completed(/*project, */startTime, endTime);
        }
    }

    record Completed(/*String project, */Instant startTime, Instant endTime) implements TimeSession {

        public Completed {
//            Objects.requireNonNull(project);
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