package com.hyxiao.common.message.core;

/**
 * 消息发送器接口
 */
public interface MessageSender {
    
    /**
     * 发送消息
     * 
     * @param topic 主题
     * @param tag 标签
     * @param key 消息键
     * @param message 消息内容
     * @return 消息发送结果
     */
    MessageSendResult send(String topic, String tag, String key, Object message);
    
    /**
     * 发送延时消息
     * 
     * @param topic 主题
     * @param tag 标签
     * @param key 消息键
     * @param message 消息内容
     * @param delayMinutes 延时分钟数
     * @return 消息发送结果
     */
    MessageSendResult sendDelay(String topic, String tag, String key, Object message, int delayMinutes);
    
    /**
     * 异步发送消息
     * 
     * @param topic 主题
     * @param tag 标签
     * @param key 消息键
     * @param message 消息内容
     * @param callback 发送回调
     */
    void sendAsync(String topic, String tag, String key, Object message, MessageSendCallback callback);
}