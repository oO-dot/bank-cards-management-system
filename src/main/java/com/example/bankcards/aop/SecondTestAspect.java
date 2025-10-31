package com.example.bankcards.aop;


import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@Order(2)
public class SecondTestAspect {

    @Around("com.example.bankcards.aop.TestAspect.anyServiceGetUserByIdMethod() && target(service) && args(id)")
    public Object addLoggingAround(ProceedingJoinPoint joinPoint, Object service, Object id) throws Throwable {
        log.info("AROUND before - invoked getUserById method in class {}, with id {}", service, id);
        try {
            Object result = joinPoint.proceed();
            log.info("AROUND after returning - invoked getUserById method in class {}, with id {}", service, result);
            return result;
        } catch (Throwable ex) {
            log.info("AROUND after throwing - invoked getUserById method in class {}, with id {}", service, ex.getClass(), ex.getMessage());
            throw ex;
        } finally {
            log.info("AROUND after (finally) - invoked getUserById method in class {}, with id {}", service);
        }
    }

}
