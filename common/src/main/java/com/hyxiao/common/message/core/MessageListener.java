package com.hyxiao.common.message.core;

/**
 * 消息监听器接口
 */
public interface MessageListener<T> {
    
    /**
     * 处理消息
     * 
     * @param message 消息内容
     * @param context 消息上下文
     * @return 处理结果
     */
    MessageProcessResult process(T message, MessageContext context);
    
    /**
     * 获取消息类型
     * 
     * @return 消息类型的Class
     */
    Class<T> getMessageType();
    
    /**
     * 获取监听的Topic
     * 
     * @return Topic名称
     */
    String getTopic();
    
    /**
     * 获取监听的Tag（可选）
     * 
     * @return Tag名称，如果为null则监听所有Tag
     */
    default String getTag() {
        return null;
    }
    
    /**
     * 获取消费者组名
     * 
     * @return 消费者组名
     */
    default String getConsumerGroup() {
        return "default-consumer-group";
    }
}