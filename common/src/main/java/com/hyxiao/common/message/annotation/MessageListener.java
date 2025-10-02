package com.hyxiao.common.message.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * 消息监听器注解
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface MessageListener {
    
    /**
     * Topic名称
     */
    String topic();
    
    /**
     * Tag名称（可选）
     */
    String tag() default "";
    
    /**
     * 消费者组名
     */
    String consumerGroup() default "";
}