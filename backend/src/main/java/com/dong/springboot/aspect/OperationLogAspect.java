package com.dong.springboot.aspect;

import com.dong.springboot.annotation.OperationLog;
import com.dong.springboot.dto.LoginUserCacheDTO;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class OperationLogAspect {

    private static final Logger log = LoggerFactory.getLogger(OperationLogAspect.class);

    @Around("@annotation(operationLog)")
    public Object around(ProceedingJoinPoint joinPoint,
                         OperationLog operationLog) throws Throwable {
        long start = System.nanoTime();
        Integer userId = currentUserId();
        String methodName = joinPoint.getSignature().toShortString();

        try {
            Object result = joinPoint.proceed();
            log.info("业务操作成功 userId={}, operation={}, method={}, costMs={}",
                    userId, operationLog.value(), methodName, elapsedMillis(start));
            return result;
        } catch (Throwable e) {
            log.warn("业务操作失败 userId={}, operation={}, method={}, costMs={}, exception={}",
                    userId, operationLog.value(), methodName,
                    elapsedMillis(start), e.getClass().getSimpleName());
            throw e;
        }
    }

    private Integer currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof LoginUserCacheDTO loginUser) {
            return loginUser.getUserId();
        }
        return null;
    }

    private long elapsedMillis(long start) {
        return (System.nanoTime() - start) / 1_000_000;
    }
}
