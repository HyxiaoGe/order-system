package com.hyxiao.common.message.core;

/**
 * 消息发送回调接口
 */
public interface MessageSendCallback {
    
    /**
     * 发送成功回调
     * 
     * @param result 发送结果
     */
    void onSuccess(MessageSendResult result);
    
    /**
     * 发送失败回调
     * 
     * @param result 发送结果
     */
    void onFailure(MessageSendResult result);
}