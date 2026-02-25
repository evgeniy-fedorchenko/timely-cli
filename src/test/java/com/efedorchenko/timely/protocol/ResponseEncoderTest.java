package com.efedorchenko.timely.protocol;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тесты для {@link ResponseEncoder} — построение ответов daemon → CLI.
 */
class ResponseEncoderTest {

    @Nested
    @DisplayName("statusResponse()")
    class StatusResp {

        @Test
        @DisplayName("formats with all values")
        void formatsCorrectly() {
            var response = ResponseEncoder.statusResponse(30, 120, true);
            assertThat(response).isEqualTo("OK 30 120 true");
        }

        @Test
        @DisplayName("formats with zeros")
        void formatsZeros() {
            var response = ResponseEncoder.statusResponse(0, 0, false);
            assertThat(response).isEqualTo("OK 0 0 false");
        }
    }

    @Nested
    @DisplayName("simple responses")
    class SimpleResponses {

        @Test
        @DisplayName("ok()")
        void ok() {
            assertThat(ResponseEncoder.ok()).isEqualTo("OK");
        }

        @Test
        @DisplayName("okWithSeconds()")
        void okWithSeconds() {
            assertThat(ResponseEncoder.okWithSeconds(42)).isEqualTo("OK 42");
        }

        @Test
        @DisplayName("error()")
        void error() {
            assertThat(ResponseEncoder.error("bad")).isEqualTo("ERROR bad");
        }

        @Test
        @DisplayName("bye()")
        void bye() {
            assertThat(ResponseEncoder.bye(100)).isEqualTo("BYE 100");
        }

        @Test
        @DisplayName("pong()")
        void pong() {
            assertThat(ResponseEncoder.pong()).isEqualTo("timely");
        }
    }
}
