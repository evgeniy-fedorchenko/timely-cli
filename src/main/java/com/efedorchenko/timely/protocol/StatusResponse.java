package com.efedorchenko.timely.protocol;

public record StatusResponse(
        long currentSec,
        long totalSec,
        boolean running
) { }
