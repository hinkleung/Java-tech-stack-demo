package com.rabbit.api;

import com.rabbit.exception.MessageRunTimeException;

import java.util.List;

public interface MessageProducer {

    /**
     * 消息的发送
     * @param message
     * @throws MessageRunTimeException
     */
    void send(Message message) throws MessageRunTimeException;

    /**
     * 消息的发送，附带SendCallback回调执行相应的业务逻辑处理
     * @param message
     * @param sendCallback
     * @throws MessageRunTimeException
     */
    void send(Message message, SendCallback sendCallback) throws MessageRunTimeException;

    /**
     * 消息的批量发送(迅速消息)
     * @param messsage
     * @throws MessageRunTimeException
     */
    void send(List<Message> messsage) throws MessageRunTimeException;

}
