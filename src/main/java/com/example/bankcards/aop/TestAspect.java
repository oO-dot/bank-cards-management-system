package com.example.bankcards.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Aspect
@Component
@Order(1)
public class TestAspect {

    @Pointcut("@within(org.springframework.stereotype.Controller)")
    public void isControllerLayer() {
    }

    @Pointcut("within(com.example.bankcards.service.impl.*ServiceImpl)")
    public void isServiceLayer() {
    }

//    @Pointcut("this(org.springframework.stereotype.Repository)") // анализирует proxy класс (будем смотреть являетсял ли прокси класс объектом репозиторий(если да, то вернет true))
    @Pointcut("target(org.springframework.stereotype.Repository)") // анализирует не сам proxy класс, а объект на который ссылается proxy класс(proxy класс тут также будет ссылкаться на объект класса репозиторий)
    public void isRepositoryLayer() {
    }

    @Pointcut("isControllerLayer() && @annotation(org.springframework.web.bind.annotation.GetMapping)") // анализирует весь проект на наличие аннотации @GetMapping(все методы всех классов, будет работать ДОЛГО(можем ускорить указав перед @annotation isControllerLayer() && , т.к. мы будем ожидать от него этих аннотаций))
    public void hasGetMapping() {
    }

    @Pointcut("isControllerLayer() && args(org.springframework.ui.Model,..)") // модель и сколько угодно любых других параметров
    public void hasModelArg() {
    }

    @Pointcut("bean(userServiceImpl)") // ищем бин userServiceImpl
    public void isUserServiceLayerBean() {
    }

    @Pointcut("bean(*ServiceImpl)") // ищем любой bean где есть ServiceImpl в конце
    public void isServiceLayerBean() {
    }

    @Pointcut("execution(public * com.example.bankcards.service.impl.*ServiceImpl.getUserById(*))")
    public void anyServiceGetUserByIdMethod() {
    }

    @Pointcut("execution(public * getUserById(*))")
    public void anyGetUserByIdMethod() {
    }

    // Advice

    @Before("anyServiceGetUserByIdMethod() " +
            "&& args(id) " +
            "&& target(service) " +
            "&& this(serviceProxy)" +
            "&& @within(transactional)")
    public void addLogging(JoinPoint joinPoint, Object id, Object service, Object serviceProxy, Transactional transactional) {
        log.info("Before invoked getUserById method in class {}, with id {}", service, id);
    }

    @AfterReturning(value = "anyServiceGetUserByIdMethod()" +
                    "&& target(service)",
                    returning = "result")
    public void addLoggingAfterReturning(Object result, Object service) {
        log.info("AfterReturning invoked getUserById method in class {}, with result {}", service, result);
    }

    @AfterThrowing(value = "anyServiceGetUserByIdMethod()" +
            "&& target(service)",
            throwing = "ex")
    public void addLoggingAfterThrowing(Throwable ex, Object service) {
        log.info("Throwing invoked getUserById method in class {}, with result {}", service, ex);
    }

    @After("anyServiceGetUserByIdMethod() && target(service)")
    public void addLoggingAfterReturning(Object service) {
        log.info("After invoked getUserById method in class {}", service);
    }

}
