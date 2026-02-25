package com.efedorchenko.timely.format;

import com.efedorchenko.timely.output.DurationFormatter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Тесты для {@link DurationFormatter}.
 *
 * <p>Формат: "Xh Ymin Zsec". Нулевые компоненты пропускаются,
 * кроме случая когда всё время = 0 → "0sec".
 *
 * <p>Негативные сценарии (null, negative) покрыты
 */
class DurationFormatterTest {

    @Nested
    @DisplayName("format(long seconds)")
    class FormatSeconds {

        @Test
        @DisplayName("zero seconds -> '0sec'")
        void zero() {
            assertThat(DurationFormatter.format(0)).isEqualTo("0sec");
        }

        @ParameterizedTest
        @CsvSource({
                "1, 1sec",
                "45, 45sec",
                "59, 59sec"
        })
        @DisplayName("seconds only")
        void secondsOnly(long seconds, String expected) {
            assertThat(DurationFormatter.format(seconds)).isEqualTo(expected);
        }

        @ParameterizedTest
        @CsvSource({
                "60, 1min",
                "120, 2min",
                "300, 5min"
        })
        @DisplayName("full minutes without seconds")
        void minutesOnly(long seconds, String expected) {
            assertThat(DurationFormatter.format(seconds)).isEqualTo(expected);
        }

        @ParameterizedTest
        @CsvSource({
                "61, 1min 1sec",
                "90, 1min 30sec",
                "754, 12min 34sec"
        })
        @DisplayName("minutes with seconds")
        void minutesAndSeconds(long seconds, String expected) {
            assertThat(DurationFormatter.format(seconds)).isEqualTo(expected);
        }

        @ParameterizedTest
        @CsvSource({
                "3600, 1h",
                "7200, 2h",
                "36000, 10h"
        })
        @DisplayName("full hours without minutes and seconds")
        void hoursOnly(long seconds, String expected) {
            assertThat(DurationFormatter.format(seconds)).isEqualTo(expected);
        }

        @Test
        @DisplayName("hours with minutes, no seconds")
        void hoursAndMinutes() {
            assertThat(DurationFormatter.format(3660)).isEqualTo("1h 1min");
            assertThat(DurationFormatter.format(9000)).isEqualTo("2h 30min");
        }

        @Test
        @DisplayName("hours with seconds, no minutes")
        void hoursAndSeconds() {
            assertThat(DurationFormatter.format(3601)).isEqualTo("1h 1sec");
            assertThat(DurationFormatter.format(3645)).isEqualTo("1h 45sec");
        }

        @Test
        @DisplayName("all components: hours, minutes, seconds")
        void allComponents() {
            assertThat(DurationFormatter.format(3661)).isEqualTo("1h 1min 1sec");
            assertThat(DurationFormatter.format(45296)).isEqualTo("12h 34min 56sec");
        }

        @Test
        @DisplayName("negative seconds throws IllegalArgumentException")
        void negativeThrows() {
            assertThatThrownBy(() -> DurationFormatter.format(-1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("negative");
        }
    }

    @Nested
    @DisplayName("format(Duration)")
    class FormatDuration {

        @Test
        @DisplayName("formats duration with all components")
        void allComponents() {
            var duration = Duration.ofHours(2).plusMinutes(30).plusSeconds(15);

            assertThat(DurationFormatter.format(duration)).isEqualTo("2h 30min 15sec");
        }

        @Test
        @DisplayName("zero duration → '0sec'")
        void zeroDuration() {
            assertThat(DurationFormatter.format(Duration.ZERO)).isEqualTo("0sec");
        }

        @Test
        @DisplayName("null duration throws NullPointerException")
        void nullThrows() {
            assertThatThrownBy(() -> DurationFormatter.format(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("negative duration throws IllegalArgumentException")
        void negativeDurationThrows() {
            var negative = Duration.ofSeconds(-10);

            assertThatThrownBy(() -> DurationFormatter.format(negative))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("negative");
        }
    }
}