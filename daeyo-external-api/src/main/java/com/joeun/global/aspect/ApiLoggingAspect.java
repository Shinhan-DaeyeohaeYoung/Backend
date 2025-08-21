package com.joeun.global.aspect;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Enumeration;

@Slf4j
@Aspect
@Component
public class ApiLoggingAspect {

    private final ThreadLocal<Long> startTime = new ThreadLocal<>();

    @Pointcut("execution(* com.joeun.api.*.controller.*Controller.*(..))")
    private void cut(){}

    @Before("cut()")
    public void beforeRequestApi(JoinPoint joinPoint) {

        startTime.set(System.currentTimeMillis());

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        Method method = getMethod(joinPoint);

        log.info("API 요청 - 이름: {}, 경로: {}, HTTP 메서드: {}", method.getName(), request.getRequestURI(), request.getMethod());
        log.info("요청 IP: {}, 요청 시간: {}", request.getRemoteAddr(), LocalDateTime.now());

        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            log.debug("Header - {}: {}", headerName, request.getHeader(headerName));
        }

        log.debug("요청 파라미터: {}", request.getQueryString());
    }

    @AfterReturning(pointcut = "cut()", returning = "returnObj")
    public void afterRequestApi(JoinPoint joinPoint, Object returnObj) {
        Method method = getMethod(joinPoint);

        Long endTime = System.currentTimeMillis();
        Long responseTime = endTime - startTime.get();
        startTime.remove();
        log.info("API 응답 - 이름: {}", method.getName());
        log.info("응답 시간: {}, 처리 시간: {}ms", LocalDateTime.now(), responseTime);
    }

    private Method getMethod(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return signature.getMethod();
    }
}
