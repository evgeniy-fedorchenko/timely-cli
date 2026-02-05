package com.efedorchenko.timely.tracker;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

public interface Tracker {
    TimeSession.Active start();

    TimeSession.Active start(String project);

    TimeSession.Completed stop();

    Optional<TimeSession.Active> currentSession();

    List<TimeSession.Completed> completedSessions();

    boolean isRunning();

    Duration totalCompletedTime();

    Duration currentSessionDuration();

    Duration totalTime();

    TrackerImpl.StatusInfo status();
}
