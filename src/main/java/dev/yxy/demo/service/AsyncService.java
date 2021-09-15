package dev.yxy.demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Created by yuanxy on 2021/09/15
 */
@Service
public class AsyncService implements IAsyncService {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Async
    @Override
    public void testAsync() {
        if (logger.isDebugEnabled()) {
            logger.debug("[testAsync] - {}", Thread.currentThread().getName());
        }
    }

    @Override
    public void testTaskExecution() {
        if (logger.isDebugEnabled()) {
            logger.debug("[testTaskExecution] - {}", Thread.currentThread().getName());
        }
    }
}
