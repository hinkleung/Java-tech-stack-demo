package com.rabbit.api;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
@Data
public class Message implements Serializable {

    private static final long serialVersionUID = 6213837812382139821L;

    /**
     * 消息的唯一ID
     */
    private String messageId;

    /**
     * 消息的主题，此处就是exchange
     * 默认exchange的type规定为topic
     */
    private String topic;

    /**
     * 消息的路由规则
     */
    private String routingKey = "";

    /**
     * 消息的附加属性
     */
    private Map<String, Object> attributes = new HashMap<>();

    /**
     * 延迟消息的参数配置
     */
    private int delayMills;

    /**
     * 消息的类型：默认为CONFIRM消息
     */
    private String messageType = MessageType.CONFIRM;

    public Message(String messageId, String topic, String routingKey, Map<String, Object> attributes, String messageType, int delayMills) {
        this.messageId = messageId;
        this.topic = topic;
        this.routingKey = routingKey;
        this.attributes = attributes;
        this.messageType = messageType;
        this.delayMills = delayMills;
    }

    public Message(String messageId, String topic, String routingKey, Map<String, Object> attributes, int delayMills) {
        this.messageId = messageId;
        this.topic = topic;
        this.routingKey = routingKey;
        this.attributes = attributes;
        this.delayMills = delayMills;
    }

    public Message() {
    }
}

