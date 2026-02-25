package com.efedorchenko.timely.protocol;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тесты для {@link Protocol} — константы и разбор типа команды.
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
}
