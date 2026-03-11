package com.zhangzc.milvusspringbootstart.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * AI Model Monitoring Annotation
 * Used to track execution time and token usage for AI operations
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AiMonitor {
    /**
     * Operation name
     */
    String value() default "";
}
