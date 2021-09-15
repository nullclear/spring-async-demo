package dev.yxy.demo.config;

import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncExecutionAspectSupport;
import org.springframework.aop.interceptor.AsyncExecutionInterceptor;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.*;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

/**
 * NOTE @EnableAsync 配置流程详解
 * 1. 开启注解才会导入选择器 {@link Import} {@link AsyncConfigurationSelector}
 * 2. {@link AbstractAsyncConfiguration} 可注入开发者自定义的 {@link AsyncConfigurer}
 * 3. 配置类 {@link ProxyAsyncConfiguration}, 内部Bean {@link AsyncAnnotationBeanPostProcessor#setBeanFactory(BeanFactory)}
 * 4. 生成Advisor {@link AsyncAnnotationAdvisor#AsyncAnnotationAdvisor(Supplier, Supplier)}
 * 5. 构建Advice {@link AnnotationAsyncExecutionInterceptor#AnnotationAsyncExecutionInterceptor(Executor)}
 * -------------------------------------------------------------------------------------------------------------
 * NOTE Async切面增强逻辑
 * {@link AsyncExecutionInterceptor#invoke(MethodInvocation)} 是切入增强的核心方法
 * 1. 内部其实就是一个Executor执行Task, 通过 {@link AsyncExecutionInterceptor#determineAsyncExecutor(Method)} 获取 {@link AsyncTaskExecutor}
 * 2. 先通过 {@link AsyncExecutionInterceptor#executors} 获取缓存, 没有缓存则走普通获取逻辑
 * 3. 先判断qualifier, 通过 {@link AnnotationAsyncExecutionInterceptor#getExecutorQualifier(Method)} 获取指定的 {@link Executor}
 * 4. 没有qualifier则通过 {@link AsyncExecutionInterceptor#defaultExecutor} 获取 {@link Executor}
 * 5. defaultExecutor 按优先级获取 {@link Executor}
 * ---开发者自定义的 {@link AsyncConfigurer#getAsyncExecutor()}
 * ---{@link AsyncExecutionAspectSupport#getDefaultExecutor(BeanFactory)} 根据 Bean类型 {@link TaskExecutor}
 * ---{@link AsyncExecutionAspectSupport#getDefaultExecutor(BeanFactory)} 根据 Bean名称 {@link AsyncExecutionAspectSupport#DEFAULT_TASK_EXECUTOR_BEAN_NAME}
 * ---{@link AsyncExecutionInterceptor#getDefaultExecutor(BeanFactory)} 直接 new {@link SimpleAsyncTaskExecutor}
 * <p>
 * Created by yuanxy on 2021/09/14
 */
@SuppressWarnings("JavadocReference")
@EnableAsync
@Configuration(proxyBeanMethods = false)
public class AsyncConfiguration implements AsyncConfigurer {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ThreadPoolTaskExecutor applicationTaskExecutor;

    /**
     * {@link Async} 默认使用的 Executor
     */
    @Override
    public Executor getAsyncExecutor() {
        return applicationTaskExecutor;
    }

    /**
     * 处理 {@link Void} 返回类型的方法中的异常
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) -> {
            Throwable t = ex.getCause() != null ? ex.getCause() : ex;
            if (logger.isErrorEnabled()) {
                logger.error("Unexpected exception occurred invoking async method: " + method, t);
            }
        };
    }
}
