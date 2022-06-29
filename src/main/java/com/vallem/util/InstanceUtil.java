package com.vallem.util;

import com.amazonaws.services.ec2.model.Instance;

public class InstanceUtil {
    final static int INSTANCE_PENDING = 0;
    final static int INSTANCE_RUNNING = 16;
    final static int INSTANCE_SHUTTING_DOWN = 32;
    final static int INSTANCE_TERMINATED = 48;
    final static int INSTANCE_STOPPING = 64;
    final static int INSTANCE_STOPPED = 80;

    public static boolean isRunning(Instance instance) {
        return instance.getState().getCode() == INSTANCE_RUNNING;
    }

    public static boolean isStopped(Instance instance) {
        return instance.getState().getCode() == INSTANCE_STOPPED;
    }
}
