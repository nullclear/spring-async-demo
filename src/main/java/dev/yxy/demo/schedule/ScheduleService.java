package dev.yxy.demo.schedule;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Created by yuanxy on 2021/09/15
 */
@Service
public class ScheduleService {

    @Scheduled(cron = "0/5 * * * * ?")
    public void doSomeThing() {
        int i = 1 / 0;
    }
}
