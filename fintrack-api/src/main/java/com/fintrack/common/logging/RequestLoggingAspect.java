package com.fintrack.common.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * AOP aspect for logging REST controller method invocations.
 * Logs method name, endpoint, HTTP method, user ID, and duration.
 * Does NOT log request/response bodies to avoid exposing sensitive data.
 */
@Aspect
@Component
public class RequestLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingAspect.class);
    private static final long SLOW_REQUEST_THRESHOLD_MS = 500;

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void restControllerMethods() {
    }

    @Pointcut("execution(public * *(..))")
    public void publicMethods() {
    }

    @Around("restControllerMethods() && publicMethods()")
    public Object logRequest(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        HttpServletRequest request = getCurrentRequest();
        String httpMethod = request != null ? request.getMethod() : "UNKNOWN";
        String endpoint = request != null ? request.getRequestURI() : "UNKNOWN";

        Long userId = getCurrentUserId();
        if (userId != null) {
            MDC.put("userId", userId.toString());
        }

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;

            logCompletion(className, methodName, httpMethod, endpoint, userId, duration);
            return result;
        } catch (Throwable ex) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("Request failed: {}.{} {} {} userId={} duration_ms={}",
                    className, methodName, httpMethod, endpoint,
                    userId != null ? userId : "anonymous", duration);
            throw ex;
        } finally {
            MDC.remove("userId");
        }
    }

    private void logCompletion(String className, String methodName,
                               String httpMethod, String endpoint,
                               Long userId, long duration) {
        String userIdStr = userId != null ? userId.toString() : "anonymous";

        if (duration >= SLOW_REQUEST_THRESHOLD_MS) {
            log.warn("Slow request: {}.{} {} {} userId={} duration_ms={}",
                    className, methodName, httpMethod, endpoint, userIdStr, duration);
        } else {
            log.info("Request completed: {}.{} {} {} userId={} duration_ms={}",
                    className, methodName, httpMethod, endpoint, userIdStr, duration);
        }
    }

    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof UserIdProvider userIdProvider) {
            return userIdProvider.getUserId();
        }
        return null;
    }

    /**
     * Interface for principal objects that can provide a user ID.
     * The UserEntity or security principal should implement this.
     */
    public interface UserIdProvider {
        Long getUserId();
    }
}
