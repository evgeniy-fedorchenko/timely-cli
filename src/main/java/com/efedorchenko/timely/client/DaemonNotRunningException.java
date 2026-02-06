package com.efedorchenko.timely.client;

import java.io.IOException;

/**
 * Daemon не запущен или не отвечает.
 */
public final class DaemonNotRunningException extends IOException {
    public DaemonNotRunningException() {
        super("Daemon not running. Start with: timely daemon");
    }
}
