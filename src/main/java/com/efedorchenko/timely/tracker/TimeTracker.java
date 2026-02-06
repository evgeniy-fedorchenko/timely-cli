package com.efedorchenko.timely.tracker;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * Интерфейс тайм-трекера.
 */
public interface TimeTracker {

    TimeSession.Active start();
    TimeSession.Completed stop();

    Duration totalCompletedTime();
    Duration currentSessionDuration();
    Duration totalTime();

    StatusInfo status();

}