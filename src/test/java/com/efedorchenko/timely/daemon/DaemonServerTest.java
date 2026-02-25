package com.efedorchenko.timely.daemon;

import com.efedorchenko.timely.client.DaemonClient;
import com.efedorchenko.timely.protocol.Protocol;
import com.efedorchenko.timely.protocol.ResponseDecoder;
import com.efedorchenko.timely.testutil.MutableClock;
import com.efedorchenko.timely.testutil.TestUtils;
import com.efedorchenko.timely.tracker.Tracker;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Интеграционные тесты для {@link DaemonServer}.
 *
 * <p>Каждый тест поднимает реальный сервер на случайном порту,
 * отправляет команды через {@link DaemonClient}, проверяет ответы.
 */
class DaemonServerTest {

    private MutableClock clock;
    private DaemonServer server;
    private DaemonClient client;
    private Thread serverThread;

    @BeforeEach
    void setUp() {
        int port = TestUtils.findFreePort();
        clock = new MutableClock(Instant.parse("2024-01-01T10:00:00Z"));
        Tracker tracker = new Tracker(clock);
        server = new DaemonServer(tracker, port);

        serverThread = new Thread(() -> {
            try {
                server.start();
            } catch (IOException _) {
                // Сервер остановлен - ок
            }
        });
        serverThread.start();

        TestUtils.waitForServer(port);

        client = new DaemonClient("localhost", port);
    }

    @AfterEach
    void tearDown() {
        server.stop();
        serverThread.interrupt();
    }

    @Nested
    @DisplayName("ping")
    class Ping {

        @Test
        @DisplayName("returns 'timely'")
        void returnsPong() throws IOException {
            var response = client.send(Protocol.CMD_PING);

            assertThat(response).isEqualTo(Protocol.RESP_PONG);
        }
    }

    @Nested
    @DisplayName("start")
    class Start {

        @Test
        @DisplayName("returns OK when timer not running")
        void returnsOk() throws IOException {
            var response = client.send(Protocol.CMD_START);

            assertThat(response).isEqualTo(Protocol.RESP_OK);
        }

        @Test
        @DisplayName("returns ERROR when timer already running")
        void returnsErrorWhenAlreadyRunning() throws IOException {
            client.send(Protocol.CMD_START);

            var response = client.send(Protocol.CMD_START);

            assertThat(ResponseDecoder.isError(response)).isTrue();
            assertThat(ResponseDecoder.errorMessage(response)).contains("already running");
        }
    }

    @Nested
    @DisplayName("stop")
    class Stop {

        @Test
        @DisplayName("returns OK with elapsed seconds")
        void returnsOkWithSeconds() throws IOException {
            client.send(Protocol.CMD_START);
            clock.advance(Duration.ofMinutes(5));

            var response = client.send(Protocol.CMD_STOP);

            assertThat(ResponseDecoder.isOk(response)).isTrue();
            assertThat(response).isEqualTo("OK 300");
        }

        @Test
        @DisplayName("returns ERROR when timer not running")
        void returnsErrorWhenNotRunning() throws IOException {
            var response = client.send(Protocol.CMD_STOP);

            assertThat(ResponseDecoder.isError(response)).isTrue();
            assertThat(ResponseDecoder.errorMessage(response)).contains("not running");
        }
    }

    @Nested
    @DisplayName("status")
    class Status {

        @Test
        @DisplayName("returns idle status when not running")
        void idleStatus() throws IOException {
            var response = client.send(Protocol.CMD_STATUS);

            var status = ResponseDecoder.parseStatus(response);
            assertThat(status).isNotNull();
            assertThat(status.running()).isFalse();
            assertThat(status.currentSec()).isZero();
            assertThat(status.totalSec()).isZero();
        }

        @Test
        @DisplayName("returns running status with correct times")
        void runningStatus() throws IOException {
            client.send(Protocol.CMD_START);
            clock.advance(Duration.ofSeconds(90));

            var response = client.send(Protocol.CMD_STATUS);

            var status = ResponseDecoder.parseStatus(response);
            assertThat(status).isNotNull();
            assertThat(status.running()).isTrue();
            assertThat(status.currentSec()).isEqualTo(90);
            assertThat(status.totalSec()).isEqualTo(90);
        }

        @Test
        @DisplayName("includes completed sessions in total")
        void includesCompletedSessions() throws IOException {
            // Первая сессия: 60 секунд
            client.send(Protocol.CMD_START);
            clock.advance(Duration.ofSeconds(60));
            client.send(Protocol.CMD_STOP);

            // Вторая сессия: 30 секунд (running)
            client.send(Protocol.CMD_START);
            clock.advance(Duration.ofSeconds(30));

            var response = client.send(Protocol.CMD_STATUS);

            var status = ResponseDecoder.parseStatus(response);
            assertThat(status.currentSec()).isEqualTo(30);
            assertThat(status.totalSec()).isEqualTo(90);
        }
    }

    @Nested
    @DisplayName("shutdown")
    class Shutdown {

        @Test
        @DisplayName("returns BYE with total seconds")
        void returnsByeWithTotal() throws IOException {
            client.send(Protocol.CMD_START);
            clock.advance(Duration.ofMinutes(10));
            client.send(Protocol.CMD_STOP);

            var response = client.send(Protocol.CMD_SHUTDOWN);

            assertThat(ResponseDecoder.isBye(response)).isTrue();
            assertThat(response).isEqualTo("BYE 600");
        }
    }

    @Nested
    @DisplayName("unknown command")
    class UnknownCommand {

        @Test
        @DisplayName("returns ERROR for unknown command")
        void returnsError() throws IOException {
            var response = client.send("unknown");

            assertThat(ResponseDecoder.isError(response)).isTrue();
            assertThat(ResponseDecoder.errorMessage(response)).contains("Unknown command");
        }
    }
}