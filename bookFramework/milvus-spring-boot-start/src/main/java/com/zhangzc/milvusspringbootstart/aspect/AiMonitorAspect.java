package com.zhangzc.milvusspringbootstart.aspect;

import com.zhangzc.milvusspringbootstart.annotation.AiMonitor;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.output.TokenUsage;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Aspect
@Component
@Slf4j
public class AiMonitorAspect {

    @Around("@annotation(aiMonitor)")
    public Object monitor(ProceedingJoinPoint joinPoint, AiMonitor aiMonitor) throws Throwable {
        long startTime = System.currentTimeMillis();
        String methodName = aiMonitor.value();
        if (!StringUtils.hasText(methodName)) {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            methodName = signature.getDeclaringType().getSimpleName() + "." + signature.getMethod().getName();
        }

        log.debug("AI Operation [{}] started", methodName);

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;

            if (result instanceof Response) {
                Response<?> response = (Response<?>) result;
                TokenUsage tokenUsage = response.tokenUsage();
                if (tokenUsage != null) {
                    log.info("AI Operation [{}] completed in {} ms. Token Usage: Input={}, Output={}, Total={}", 
                            methodName, duration, 
                            tokenUsage.inputTokenCount(), 
                            tokenUsage.outputTokenCount(), 
                            tokenUsage.totalTokenCount());
                } else {
                    log.info("AI Operation [{}] completed in {} ms. (No Token Usage info)", methodName, duration);
                }
            } else {
                log.info("AI Operation [{}] completed in {} ms.", methodName, duration);
            }
            return result;
        } catch (Throwable e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("AI Operation [{}] failed after {} ms: {}", methodName, duration, e.getMessage());
            throw e;
        }
    }
}
