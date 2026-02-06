package com.efedorchenko.timely.tracker;

import java.time.Duration;

/** Снимок состояния трекера. */
public record StatusInfo(
        TimeSession.Active currentSession,
        Duration currentSessionTime,
        Duration totalCompletedTime,
        Duration totalTime
) {

    public boolean isRunning() {
        return currentSession != null;
    }

//    public String projectName() {
//        return currentSession != null ? currentSession.project() : null;
//    }
}
