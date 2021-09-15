package dev.yxy.demo.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.autoconfigure.task.TaskExecutionProperties;
import org.springframework.boot.task.TaskExecutorBuilder;
import org.springframework.boot.task.TaskExecutorCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * NOTE TaskExecution 配置流程详解
 * 1. 存在此类型的Class {@link ConditionalOnClass} {@link ThreadPoolTaskExecutor} 自动配置类才会生效
 * 2. 构建者 {@link TaskExecutionAutoConfiguration#taskExecutorBuilder(TaskExecutionProperties, ObjectProvider, ObjectProvider)}
 * ---ObjectProvider的实例 {@link DefaultListableBeanFactory.DependencyObjectProvider}
 * 3. TaskExecution Executor的Bean {@link TaskExecutionAutoConfiguration#applicationTaskExecutor(TaskExecutorBuilder)} 懒加载模式
 * Created by yuanxy on 2021/09/15
 */
@SuppressWarnings("JavadocReference")
@Configuration(proxyBeanMethods = false)
public class TaskExecutorConfig {

    /**
     * Executor定制，可以有多个，后者会覆盖前者
     * 详见 {@link TaskExecutorBuilder#configure(ThreadPoolTaskExecutor)}
     */
    @Bean
    public TaskExecutorCustomizer taskExecutorCustomizer() {
        return taskExecutor -> taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
    }

    /**
     * 每个Task的装饰器，只能有一个
     */
    @Bean
    public TaskDecorator taskDecorator() {
        return runnable -> runnable;
    }
}
