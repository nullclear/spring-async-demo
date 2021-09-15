package dev.yxy.demo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.task.TaskSchedulingAutoConfiguration;
import org.springframework.boot.task.TaskSchedulerBuilder;
import org.springframework.boot.task.TaskSchedulerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.scheduling.annotation.SchedulingConfiguration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.lang.reflect.Method;

/**
 * NOTE @EnableScheduling 配置流程详解
 * 1. 开启注解才会导入配置类 {@link SchedulingConfiguration}, 内部Bean {@link ScheduledAnnotationBeanPostProcessor}
 * 2. {@link TaskSchedulingAutoConfiguration#taskScheduler(TaskSchedulerBuilder)} 生成Bean {@link TaskScheduler},
 * ---有条件 {@link ConditionalOnBean} 和 {@link ConditionalOnMissingBean}
 * 3. 依次加载所有的实现 {@link Scheduled} 注解的类方法 {@link ScheduledAnnotationBeanPostProcessor#postProcessAfterInitialization(Object, String)}
 * 4. 处理每个定时任务 {@link ScheduledAnnotationBeanPostProcessor#processScheduled(Scheduled, Method, Object)}, 添加到注册器中 {@link ScheduledTaskRegistrar}
 * ---此时注册器中还没有 {@link TaskScheduler}, 所以在这里不会立即执行
 * 5. 调用应用事件完成注册 {@link ScheduledAnnotationBeanPostProcessor#onApplicationEvent(ContextRefreshedEvent)}
 * 6. 处理完成注册的逻辑 {@link ScheduledAnnotationBeanPostProcessor#finishRegistration()} 256行 搜索Bean {@link TaskScheduler}
 * 7. 为注册器设置执行器 {@link ScheduledTaskRegistrar#setTaskScheduler(TaskScheduler)}
 * 8. 处理完成注册的逻辑 {@link ScheduledAnnotationBeanPostProcessor#finishRegistration()} 302行 调用注册器方法 {@link ScheduledTaskRegistrar#afterPropertiesSet()}
 * 9. 开始执行所有定时任务 {@link ScheduledTaskRegistrar#scheduleTasks()}
 * Created by yuanxy on 2021/09/15
 */
@SuppressWarnings("JavadocReference")
@EnableScheduling
@Configuration(proxyBeanMethods = false)
public class TaskSchedulerConfig {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Scheduler定制，可以有多个，后者会覆盖前者
     * 详见 {@link TaskSchedulerBuilder#configure(ThreadPoolTaskScheduler)}
     */
    @Bean
    public TaskSchedulerCustomizer taskSchedulerCustomizer() {
        return taskScheduler -> {
            taskScheduler.setDaemon(true);
            taskScheduler.setErrorHandler(ex -> {
                Throwable t = ex.getCause() != null ? ex.getCause() : ex;
                if (logger.isErrorEnabled()) {
                    logger.error("[" + Thread.currentThread().getName() + "] Unexpected exception occurred invoking schedule method", t);
                }
            });
        };
    }
}
