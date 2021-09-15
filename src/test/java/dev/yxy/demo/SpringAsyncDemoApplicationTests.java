package dev.yxy.demo;

import dev.yxy.demo.service.IAsyncService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@SpringBootTest
class SpringAsyncDemoApplicationTests {

    @Autowired
    private IAsyncService asyncService;

    @Autowired
    private ThreadPoolTaskExecutor applicationTaskExecutor;

    @Test
    public void testAsync() throws InterruptedException {
        asyncService.testTaskExecution();
        Thread.sleep(1000);

        asyncService.testAsync();
        applicationTaskExecutor.execute(() -> asyncService.testTaskExecution());
        Thread.sleep(1000);
    }
}
