package com.efedorchenko.timely.protocol;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Тесты для {@link ResponseDecoder} — разбор ответов daemon → CLI.
 *
 * <p>Edge cases вроде множественных пробелов или некорректного ввода
 * от внешних источников намеренно не покрыты.
 * Это внутренний протокол, где обе стороны под нашим контролем.
 *
 * <p>Roundtrip тест ({@link ResponseEncoder} → {@link ResponseDecoder})
 * гарантирует консистентность формата.
 */
class ResponseDecoderTest {

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
            assertThat(ResponseDecoder.isOk(response)).isEqualTo(expected);
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
            assertThat(ResponseDecoder.isError(response)).isEqualTo(expected);
        }

        @ParameterizedTest
        @CsvSource({
                "BYE, true",
                "BYE 3600, true",
                "OK, false"
        })
        @DisplayName("isBye checks prefix correctly")
        void isBye(String response, boolean expected) {
            assertThat(ResponseDecoder.isBye(response)).isEqualTo(expected);
        }

        @Test
        @DisplayName("all checks return false for null")
        void nullReturnsFalse() {
            assertThat(ResponseDecoder.isOk(null)).isFalse();
            assertThat(ResponseDecoder.isError(null)).isFalse();
            assertThat(ResponseDecoder.isBye(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("errorMessage()")
    class ErrorMessage {

        @Test
        @DisplayName("extracts message from error")
        void extractsMessage() {
            assertThat(ResponseDecoder.errorMessage("ERROR Timer not running"))
                    .isEqualTo("Timer not running");
        }

        @Test
        @DisplayName("returns empty when no message")
        void noMessage() {
            assertThat(ResponseDecoder.errorMessage("ERROR")).isEmpty();
        }

        @Test
        @DisplayName("returns empty for non-error")
        void nonError() {
            assertThat(ResponseDecoder.errorMessage("OK")).isEmpty();
        }

        @Test
        @DisplayName("returns empty for null")
        void nullInput() {
            assertThat(ResponseDecoder.errorMessage(null)).isEmpty();
        }
    }

    @Nested
    @DisplayName("parseStatus()")
    class ParseStatus {

        @Test
        @DisplayName("parses valid running status")
        void validRunning() {
            var status = ResponseDecoder.parseStatus("OK 45 3600 true");

            assertThat(status).isNotNull();
            assertThat(status.currentSec()).isEqualTo(45);
            assertThat(status.totalSec()).isEqualTo(3600);
            assertThat(status.running()).isTrue();
        }

        @Test
        @DisplayName("parses valid idle status")
        void validIdle() {
            var status = ResponseDecoder.parseStatus("OK 0 1800 false");

            assertThat(status).isNotNull();
            assertThat(status.currentSec()).isZero();
            assertThat(status.running()).isFalse();
        }

        @Test
        @DisplayName("roundtrip: encode then decode")
        void roundtrip() {
            var encoded = ResponseEncoder.statusResponse(123, 456, true);
            var decoded = ResponseDecoder.parseStatus(encoded);

            assertThat(decoded).isNotNull();
            assertThat(decoded.currentSec()).isEqualTo(123);
            assertThat(decoded.totalSec()).isEqualTo(456);
            assertThat(decoded.running()).isTrue();
        }

        @ParameterizedTest
        @DisplayName("returns null for invalid format")
        @ValueSource(strings = {
                "ERROR something",
                "OK 10",
                "OK abc 20 true",
        })
        void invalidFormat(String response) {
            assertThat(ResponseDecoder.parseStatus(response)).isNull();
        }

        @Test
        @DisplayName("returns null for null")
        void nullInput() {
            assertThat(ResponseDecoder.parseStatus(null)).isNull();
        }
    }

    @Nested
    @DisplayName("parseSeconds()")
    class ParseSeconds {

        @Test
        @DisplayName("parses seconds from OK response")
        void fromOkResponse() {
            assertThat(ResponseDecoder.parseSeconds("OK 123")).isEqualTo(123);
        }

        @Test
        @DisplayName("parses seconds from BYE response")
        void fromByeResponse() {
            assertThat(ResponseDecoder.parseSeconds("BYE 3600")).isEqualTo(3600);
        }

        @Test
        @DisplayName("roundtrip: okWithSeconds encode then decode")
        void roundtripOk() {
            var encoded = ResponseEncoder.okWithSeconds(42);
            assertThat(ResponseDecoder.parseSeconds(encoded)).isEqualTo(42);
        }

        @Test
        @DisplayName("roundtrip: bye encode then decode")
        void roundtripBye() {
            var encoded = ResponseEncoder.bye(99);
            assertThat(ResponseDecoder.parseSeconds(encoded)).isEqualTo(99);
        }

        @Test
        @DisplayName("throws on response without seconds")
        void noSeconds() {
            assertThatThrownBy(() -> ResponseDecoder.parseSeconds("OK"))
                    .isInstanceOf(NumberFormatException.class);
        }
    }
}
