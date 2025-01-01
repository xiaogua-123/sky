package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;


/**
 * 自定义切面实现字段自动注解
 *
 * @author 汉中吴彦祖
 * @date 2024/12/31
 * @description 一个类(´ ・ ω ・ `)
 */
@Aspect
@Component
@Slf4j
public class AutoFillAspect {

    // 切入点
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill) ")
    public void autoFillPointcut() {
    }

    /**
     * 自动填充注解
     *
     * @param joinPoint 切点
     */
    @Before("autoFillPointcut()")
    public void autoFill(JoinPoint joinPoint) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        log.info("开始自动填充注解....");
        // 获取方法签名
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        // 获取被代理的方法
        Method method = methodSignature.getMethod();
        // 获取方法上的AutoFill注解
        AutoFill autoFill = method.getAnnotation(AutoFill.class);
        if (autoFill == null) {
            return;
        }
        // 获取操作类型
        OperationType operationType = autoFill.value();

        // 获取方法参数
        Object[] args = joinPoint.getArgs();
        if(args == null || args.length == 0){
            return;
        }
        Object entity = args[0];
        if (entity == null) {
            return;
        }
        Long currentId = BaseContext.getCurrentId();
        LocalDateTime now = LocalDateTime.now();
        // 根据不同操作类型进行对应字段的填充
        if(operationType == OperationType.INSERT){
            //为公共字段赋值-4
            log.info("新增操作，新增公共字段...");
            entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class).invoke(entity, now);
            entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class).invoke(entity, now);
            entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class).invoke(entity, currentId);
            entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class).invoke(entity, currentId);
        }else if(operationType == OperationType.UPDATE){
            //为公共字段赋值-2
            log.info("更新操作，更新公共字段...");
            entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class).invoke(entity, now);
            entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class).invoke(entity, currentId);
        }
    }
}