package com.hyxiao.common.message.monitor;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 消息监控指标
 */
@Component
@RequiredArgsConstructor
public class MessageMetrics {
    
    private final MeterRegistry meterRegistry;
    
    /**
     * 记录消息发送成功
     */
    public void recordSendSuccess(String topic, String tag) {
        Counter.builder("message.send.success")
                .tag("topic", topic)
                .tag("tag", tag)
                .register(meterRegistry)
                .increment();
    }
    
    /**
     * 记录消息发送失败
     */
    public void recordSendFailure(String topic, String tag, String error) {
        Counter.builder("message.send.failure")
                .tag("topic", topic)
                .tag("tag", tag)
                .tag("error", error)
                .register(meterRegistry)
                .increment();
    }
    
    /**
     * 记录消息发送耗时
     */
    public Timer.Sample startSendTimer(String topic, String tag) {
        return Timer.start(meterRegistry);
    }
    
    /**
     * 结束发送计时
     */
    public void endSendTimer(Timer.Sample sample, String topic, String tag) {
        sample.stop(Timer.builder("message.send.duration")
                .tag("topic", topic)
                .tag("tag", tag)
                .register(meterRegistry));
    }
    
    /**
     * 记录消息消费成功
     */
    public void recordConsumeSuccess(String topic, String tag) {
        Counter.builder("message.consume.success")
                .tag("topic", topic)
                .tag("tag", tag)
                .register(meterRegistry)
                .increment();
    }
    
    /**
     * 记录消息消费失败
     */
    public void recordConsumeFailure(String topic, String tag, String error) {
        Counter.builder("message.consume.failure")
                .tag("topic", topic)
                .tag("tag", tag)
                .tag("error", error)
                .register(meterRegistry)
                .increment();
    }
    
    /**
     * 记录消息重试
     */
    public void recordRetry(String topic, String tag, int retryCount) {
        Counter.builder("message.retry")
                .tag("topic", topic)
                .tag("tag", tag)
                .tag("retry_count", String.valueOf(retryCount))
                .register(meterRegistry)
                .increment();
    }
}