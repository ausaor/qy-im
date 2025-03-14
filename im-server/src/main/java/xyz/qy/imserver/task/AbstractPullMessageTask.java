package xyz.qy.imserver.task;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractPullMessageTask {
    public abstract void pullMessage();
}
