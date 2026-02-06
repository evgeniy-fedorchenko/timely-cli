package com.efedorchenko.timely.protocol;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тесты для {@link Protocol} — внутренний протокол общения CLI ↔ Daemon.
 *
 * <p>Edge cases вроде множественных пробелов, пробелов в начале/конце строки
 * или некорректного ввода от внешних источников намеренно не покрыты.
 * Это внутренний протокол, где:
 * <ul>
 *   <li>Команды приходят из нашего CLI (парсит picocli)</li>
 *   <li>Ответы формируются методами {@link Protocol#statusResponse} и подобными</li>
 *   <li>Обе стороны под нашим контролем — нет недоверенного ввода</li>
 * </ul>
 *
 * <p>Roundtrip тест ({@code format → parse}) гарантирует консистентность формата.
 */
class ProtocolTest {

    @Nested
    @DisplayName("commandType()")
    class CommandType {

        @Test
        @DisplayName("extracts command without arguments")
        void commandWithoutArguments() {
            assertThat(Protocol.commandType("start")).isEqualTo("start");
        }

        @Test
        @DisplayName("extracts first word when command has arguments")
        void commandWithArguments() {
            assertThat(Protocol.commandType("start project")).isEqualTo("start");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        @DisplayName("returns empty string for null, empty or blank")
        void nullEmptyOrBlank(String input) {
            assertThat(Protocol.commandType(input)).isEmpty();
        }
    }

    @Nested
    @DisplayName("statusResponse()")
    class StatusResponse {

        @Test
        @DisplayName("formats with all values")
        void formatsCorrectly() {
            var response = Protocol.statusResponse(30, 120, true);

            assertThat(response).isEqualTo("OK 30 120 true");
        }

        @Test
        @DisplayName("formats with zeros")
        void formatsZeros() {
            var response = Protocol.statusResponse(0, 0, false);

            assertThat(response).isEqualTo("OK 0 0 false");
        }
    }

    @Nested
    @DisplayName("parseStatus()")
    class ParseStatus {

        @Test
        @DisplayName("parses valid running status")
        void validRunning() {
            var status = Protocol.parseStatus("OK 45 3600 true");

            assertThat(status).isNotNull();
            assertThat(status.currentSec()).isEqualTo(45);
            assertThat(status.totalSec()).isEqualTo(3600);
            assertThat(status.running()).isTrue();
        }

        @Test
        @DisplayName("parses valid idle status")
        void validIdle() {
            var status = Protocol.parseStatus("OK 0 1800 false");

            assertThat(status).isNotNull();
            assertThat(status.currentSec()).isZero();
            assertThat(status.running()).isFalse();
        }

        // Если кто-то изменит формат в statusResponse(), но забудет про parseStatus() — тест упадёт.
        @Test
        @DisplayName("roundtrip: format then parse")
        void roundtrip() {
            var original = Protocol.statusResponse(123, 456, true);
            var parsed = Protocol.parseStatus(original);

            assertThat(parsed).isNotNull();
            assertThat(parsed.currentSec()).isEqualTo(123);
            assertThat(parsed.totalSec()).isEqualTo(456);
            assertThat(parsed.running()).isTrue();
        }

        @ParameterizedTest
        @DisplayName("returns null for invalid format")
        @ValueSource(strings = {
                "ERROR something",  // не OK
                "OK 10",            // недостаточно частей
                "OK abc 20 true",   // не число
        })
        void invalidFormat(String response) {
            assertThat(Protocol.parseStatus(response)).isNull();
        }

        @Test
        @DisplayName("returns null for null")
        void nullInput() {
            assertThat(Protocol.parseStatus(null)).isNull();
        }
    }

    @Nested
    @DisplayName("response type checks")
    class ResponseTypeChecks {

        @ParameterizedTest
        @CsvSource({
                "OK, true",
                "OK 123, true",
                "ERROR, false",
                "ok, false"
        })
        @DisplayName("isOk checks prefix correctly")
        void isOk(String response, boolean expected) {
            assertThat(Protocol.isOk(response)).isEqualTo(expected);
        }

        @ParameterizedTest
        @CsvSource({
                "ERROR, true",
                "ERROR message, true",
                "OK, false",
                "error, false"
        })
        @DisplayName("isError checks prefix correctly")
        void isError(String response, boolean expected) {
            assertThat(Protocol.isError(response)).isEqualTo(expected);
        }

        @ParameterizedTest
        @CsvSource({
                "BYE, true",
                "BYE 3600, true",
                "OK, false"
        })
        @DisplayName("isBye checks prefix correctly")
        void isBye(String response, boolean expected) {
            assertThat(Protocol.isBye(response)).isEqualTo(expected);
        }

        @Test
        @DisplayName("all checks return false for null")
        void nullReturnsFlase() {
            assertThat(Protocol.isOk(null)).isFalse();
            assertThat(Protocol.isError(null)).isFalse();
            assertThat(Protocol.isBye(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("errorMessage()")
    class ErrorMessage {

        @Test
        @DisplayName("extracts message from error")
        void extractsMessage() {
            assertThat(Protocol.errorMessage("ERROR Timer not running"))
                    .isEqualTo("Timer not running");
        }

        @Test
        @DisplayName("returns empty when no message")
        void noMessage() {
            assertThat(Protocol.errorMessage("ERROR")).isEmpty();
        }

        @Test
        @DisplayName("returns empty for non-error")
        void nonError() {
            assertThat(Protocol.errorMessage("OK")).isEmpty();
        }

        @Test
        @DisplayName("returns empty for null")
        void nullInput() {
            assertThat(Protocol.errorMessage(null)).isEmpty();
        }
    }
}