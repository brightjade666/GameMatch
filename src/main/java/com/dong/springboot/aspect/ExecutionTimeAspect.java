package com.dong.springboot.aspect;

import com.dong.springboot.annotation.LogExecutionTime;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
public class ExecutionTimeAspect {

    private static final Logger log = LoggerFactory.getLogger(ExecutionTimeAspect.class);

    @Around("@annotation(com.dong.springboot.annotation.LogExecutionTime)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.nanoTime();
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        String methodName = method.getDeclaringClass().getSimpleName() + "." + method.getName();

        try {
            Object result = joinPoint.proceed();
            log.info("接口执行成功 method={}, costMs={}", methodName, elapsedMillis(start));
            return result;
        } catch (Throwable e) {
            log.error("接口执行失败 method={}, costMs={}, exception={}",
                    methodName, elapsedMillis(start), e.getClass().getSimpleName());
            throw e;
        }
    }

    private long elapsedMillis(long start) {
        return (System.nanoTime() - start) / 1_000_000;
    }
}
