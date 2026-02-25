package com.efedorchenko.timely.client;

import com.efedorchenko.timely.output.Printer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

import static org.mockito.Mockito.*;

class TrackerServiceTest {

    @Mock DaemonClient client;
    @Mock Printer printer;

    private TrackerService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new TrackerService(client, printer);
    }

    @Nested
    @DisplayName("start()")
    class Start {

        @Test
        @DisplayName("OK — prints started")
        void okResponse() throws IOException {
            when(client.send("start")).thenReturn("OK");
            service.start();
            verify(printer).printStarted();
        }

        @Test
        @DisplayName("ERROR — prints error message")
        void errorResponse() throws IOException {
            when(client.send("start")).thenReturn("ERROR Timer already running");
            service.start();
            verify(printer).printError("Timer already running");
        }

        @Test
        @DisplayName("IOException — prints error")
        void ioException() throws IOException {
            when(client.send("start")).thenThrow(new IOException("Connection refused"));
            service.start();
            verify(printer).printError("Connection refused");
        }
    }

    @Nested
    @DisplayName("stop()")
    class Stop {

        @Test
        @DisplayName("OK — prints stopped with seconds")
        void okResponse() throws IOException {
            when(client.send("stop")).thenReturn("OK 3600");
            service.stop();
            verify(printer).printStopped(3600L);
        }

        @Test
        @DisplayName("ERROR — prints error message")
        void errorResponse() throws IOException {
            when(client.send("stop")).thenReturn("ERROR Timer is not running");
            service.stop();
            verify(printer).printError("Timer is not running");
        }

        @Test
        @DisplayName("IOException — prints error")
        void ioException() throws IOException {
            when(client.send("stop")).thenThrow(new IOException("Connection refused"));
            service.stop();
            verify(printer).printError("Connection refused");
        }
    }

    @Nested
    @DisplayName("status()")
    class Status {

        @Test
        @DisplayName("valid running status — prints status")
        void runningStatus() throws IOException {
            when(client.send("status")).thenReturn("OK 120 3600 true");
            service.status();
            verify(printer).printStatus(true, 120L, 3600L);
        }

        @Test
        @DisplayName("valid idle status — prints status")
        void idleStatus() throws IOException {
            when(client.send("status")).thenReturn("OK 0 3600 false");
            service.status();
            verify(printer).printStatus(false, 0L, 3600L);
        }

        @Test
        @DisplayName("invalid response — prints error")
        void invalidResponse() throws IOException {
            when(client.send("status")).thenReturn("GARBAGE");
            service.status();
            verify(printer).printError("Invalid response: GARBAGE");
        }

        @Test
        @DisplayName("IOException — prints error")
        void ioException() throws IOException {
            when(client.send("status")).thenThrow(new IOException("Connection refused"));
            service.status();
            verify(printer).printError("Connection refused");
        }
    }

    @Nested
    @DisplayName("shutdown()")
    class Shutdown {

        @Test
        @DisplayName("BYE — prints shutdown with total seconds")
        void byeResponse() throws IOException {
            when(client.send("shutdown")).thenReturn("BYE 7200");
            service.shutdown();
            verify(printer).printShutdown(7200L);
        }

        @Test
        @DisplayName("ERROR — prints error message")
        void errorResponse() throws IOException {
            when(client.send("shutdown")).thenReturn("ERROR something went wrong");
            service.shutdown();
            verify(printer).printError("something went wrong");
        }

        @Test
        @DisplayName("IOException — prints error")
        void ioException() throws IOException {
            when(client.send("shutdown")).thenThrow(new IOException("Connection refused"));
            service.shutdown();
            verify(printer).printError("Connection refused");
        }
    }
}
