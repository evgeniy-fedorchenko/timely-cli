package com.efedorchenko.timely.tracker;

import com.efedorchenko.timely.testutil.MutableClock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TrackerTest {

    private MutableClock clock;  //  Наша имплементация Clock, чтоб можно было увеличивать время руками
    private Tracker tracker;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(Instant.parse("2024-01-01T10:00:00Z"));
        tracker = new Tracker(clock);
    }

    @Nested
    @DisplayName("start()")
    class Start {

        @Test
        @DisplayName("creates active session with current time")
        void createsActiveSession() {
            var session = tracker.start();

            assertThat(session).isNotNull();
            assertThat(session.startTime()).isEqualTo(clock.instant());
        }

        @Test
        @DisplayName("throws when timer already running")
        void throwsWhenAlreadyRunning() {
            tracker.start();

            assertThatThrownBy(() -> tracker.start())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("already running");
        }
    }

    @Nested
    @DisplayName("stop()")
    class Stop {

        @Test
        @DisplayName("returns completed session with correct duration")
        void returnsCompletedSession() {
            tracker.start();
            clock.advance(Duration.ofMinutes(5));

            var completed = tracker.stop();

            assertThat(completed).isNotNull();
            assertThat(completed.duration()).isEqualTo(Duration.ofMinutes(5));
        }

        @Test
        @DisplayName("throws when timer not running")
        void throwsWhenNotRunning() {
            assertThatThrownBy(() -> tracker.stop())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not running");
        }

        @Test
        @DisplayName("allows starting new session after stop")
        void allowsRestartAfterStop() {
            tracker.start();
            clock.advance(Duration.ofMinutes(1));
            tracker.stop();

            var newSession = tracker.start();

            assertThat(newSession).isNotNull();
        }
    }

    @Nested
    @DisplayName("time calculations")
    class TimeCalculations {

        @Test
        @DisplayName("totalCompletedTime returns zero initially")
        void totalCompletedTimeInitiallyZero() {
            assertThat(tracker.totalCompletedTime()).isEqualTo(Duration.ZERO);
        }

        @Test
        @DisplayName("totalCompletedTime sums all completed sessions")
        void totalCompletedTimeSumsAll() {
            // First session: 5 minutes
            tracker.start();
            clock.advance(Duration.ofMinutes(5));
            tracker.stop();

            // Second session: 10 minutes
            tracker.start();
            clock.advance(Duration.ofMinutes(10));
            tracker.stop();

            assertThat(tracker.totalCompletedTime()).isEqualTo(Duration.ofMinutes(15));
        }

        @Test
        @DisplayName("currentSessionDuration returns zero when not running")
        void currentSessionDurationWhenNotRunning() {
            assertThat(tracker.currentSessionDuration()).isEqualTo(Duration.ZERO);
        }

        @Test
        @DisplayName("currentSessionDuration returns elapsed time when running")
        void currentSessionDurationWhenRunning() {
            tracker.start();
            clock.advance(Duration.ofSeconds(30));

            assertThat(tracker.currentSessionDuration()).isEqualTo(Duration.ofSeconds(30));
        }

        @Test
        @DisplayName("totalTime includes both completed and current session")
        void totalTimeIncludesBoth() {
            // Completed: 5 minutes
            tracker.start();
            clock.advance(Duration.ofMinutes(5));
            tracker.stop();

            // Current running: 3 minutes
            tracker.start();
            clock.advance(Duration.ofMinutes(3));

            assertThat(tracker.totalTime()).isEqualTo(Duration.ofMinutes(8));
        }
    }

    @Nested
    @DisplayName("status()")
    class Status {

        @Test
        @DisplayName("returns not running status when idle")
        void statusWhenIdle() {
            var status = tracker.status();

            assertThat(status.isRunning()).isFalse();
            assertThat(status.currentSession()).isNull();
            assertThat(status.currentSessionTime()).isEqualTo(Duration.ZERO);
            assertThat(status.totalTime()).isEqualTo(Duration.ZERO);
        }

        @Test
        @DisplayName("returns running status with correct times")
        void statusWhenRunning() {
            tracker.start();
            clock.advance(Duration.ofSeconds(45));

            var status = tracker.status();

            assertThat(status.isRunning()).isTrue();
            assertThat(status.currentSession()).isNotNull();
            assertThat(status.currentSessionTime()).isEqualTo(Duration.ofSeconds(45));
            assertThat(status.totalTime()).isEqualTo(Duration.ofSeconds(45));
        }

        @Test
        @DisplayName("totalCompletedTime excludes current running session")
        void totalCompletedTimeExcludesCurrent() {
            // Completed: 10 minutes
            tracker.start();
            clock.advance(Duration.ofMinutes(10));
            tracker.stop();

            // Running: 5 minutes
            tracker.start();
            clock.advance(Duration.ofMinutes(5));

            var status = tracker.status();

            assertThat(status.totalCompletedTime()).isEqualTo(Duration.ofMinutes(10));
            assertThat(status.totalTime()).isEqualTo(Duration.ofMinutes(15));
        }
    }
}