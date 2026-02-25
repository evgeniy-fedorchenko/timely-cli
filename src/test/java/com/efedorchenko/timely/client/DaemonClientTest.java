package com.efedorchenko.timely.client;

import com.efedorchenko.timely.daemon.DaemonServer;
import com.efedorchenko.timely.testutil.TestUtils;
import com.efedorchenko.timely.tracker.Tracker;
import org.junit.jupiter.api.*;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Тесты для {@link DaemonClient}.
 *
 * <p>Основная логика клиента покрыта в {@link com.efedorchenko.timely.daemon.DaemonServerTest}.
 * Здесь тестируем поведение клиента при недоступном сервере и проверку isDaemonRunning().
 */
class DaemonClientTest {

    @Nested
    @DisplayName("when daemon is not running")
    class DaemonNotRunning {

        private DaemonClient client;

        @BeforeEach
        void setUp() {
            // Подключаемся к порту где точно никого нет
            int unusedPort = TestUtils.findFreePort();
            client = new DaemonClient("localhost", unusedPort);
        }

        @Test
        @DisplayName("isDaemonRunning() returns false")
        void isDaemonRunningReturnsFalse() {
            assertThat(client.isDaemonRunning()).isFalse();
        }

        @Test
        @DisplayName("send() throws DaemonNotRunningException")
        void sendThrowsException() {
            assertThatThrownBy(() -> client.send("ping"))
                    .isInstanceOf(DaemonNotRunningException.class)
                    .hasMessageContaining("Daemon not running");
        }
    }

    @Nested
    @DisplayName("when daemon is running")
    class DaemonRunning {

        private DaemonServer server;
        private DaemonClient client;
        private Thread serverThread;

        @BeforeEach
        void setUp() {
            int port = TestUtils.findFreePort();
            server = new DaemonServer(new Tracker(), port);

            serverThread = new Thread(() -> {
                try {
                    server.start();
                } catch (IOException _) {
                    // Сервер остановлен
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

        @Test
        @DisplayName("isDaemonRunning() returns true")
        void isDaemonRunningReturnsTrue() {
            assertThat(client.isDaemonRunning()).isTrue();
        }

        @Test
        @DisplayName("send() returns response")
        void sendReturnsResponse() throws IOException {
            var response = client.send("ping");

            assertThat(response).isEqualTo("timely");
        }
    }
}