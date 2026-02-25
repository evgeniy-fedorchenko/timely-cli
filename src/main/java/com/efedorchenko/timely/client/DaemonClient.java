package com.efedorchenko.timely.client;

import com.efedorchenko.timely.AppProperties;
import com.efedorchenko.timely.protocol.Protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Клиент для общения с daemon.
 * <p>
 * Каждый вызов {@link #send} открывает новое соединение,
 * отправляет команду, читает ответ и закрывает соединение.
 */
public final class DaemonClient {

    private final String host;
    private final int port;

    public DaemonClient() {
        this(AppProperties.daemonHost(), AppProperties.daemonPort());
    }

    public DaemonClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * Отправляет команду и возвращает ответ.
     * Можно было бы держать соединение, но не будем усложнять
     *
     * @throws DaemonNotRunningException если daemon не запущен
     * @throws IOException при других ошибках сети
     */
    public String send(String command) throws IOException {
        try (var socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), AppProperties.connectTimeoutMs());
            socket.setSoTimeout(AppProperties.readTimeoutMs());

            try (var writer = new PrintWriter(socket.getOutputStream(), true);
                 var reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                writer.println(command);
                var response = reader.readLine();

                if (response == null) {
                    throw new IOException("No response from daemon");
                }
                return response;
            }
        } catch (ConnectException _) {
            throw new DaemonNotRunningException();
        }
    }

    /** Проверяет, запущен ли daemon (ping/pong). */
    public boolean isDaemonRunning() {
        try {
            String answer = send(Protocol.CMD_PING);
            return Protocol.RESP_PONG.equals(answer);
        } catch (IOException _) {
            return false;
        }
    }
}
