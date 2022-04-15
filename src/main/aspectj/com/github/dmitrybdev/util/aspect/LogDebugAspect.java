package com.github.dmitrybdev.util.aspect;

import com.github.dmitrybdev.util.log.handler.LogDebugAnnotationHandler;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class LogDebugAspect {
    @Around("execution(@com.github.dmitrybdev.util.log.LogDebug * *(..)) " +
            "|| execution(@com.github.dmitrybdev.util.log.LogDebugHide * *(..))")
    public Object logDebug(ProceedingJoinPoint thisJoinPoint) throws Throwable {
        return LogDebugAnnotationHandler.INSTANCE.log(thisJoinPoint, false);
    }

    @Around("execution(@com.github.dmitrybdev.util.log.LogTrace * *(..)) " +
            "|| execution(@com.github.dmitrybdev.util.log.LogTraceHide * *(..))")
    public Object logTrace(ProceedingJoinPoint thisJoinPoint) throws Throwable {
        return LogDebugAnnotationHandler.INSTANCE.log(thisJoinPoint, true);
    }
}
