package com.rabbit.producer.broker;

import com.rabbit.api.Message;
import com.rabbit.api.MessageType;
import com.rabbit.producer.constant.BrokerMessageConst;
import com.rabbit.producer.constant.BrokerMessageStatus;
import com.rabbit.producer.entity.BrokerMessage;
import com.rabbit.producer.service.MessageStoreService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * 真正发消息的类
 */
@Component
@Slf4j
public class RabbitBrokerImpl implements RabbitBroker {

    @Autowired
    private RabbitTemplateContainer rabbitTemplateContainer;
    @Autowired
    private MessageStoreService messageStoreService;

    /**
     * 迅速发消息
     *
     * @param message
     */
    @Override
    public void rapidSend(Message message) {
        message.setMessageType(MessageType.RAPID);
        sendKernel(message);
    }

    /**
     * 发送消息的和新方法
     * 使用异步发消息
     *
     * @param message
     */
    private void sendKernel(Message message) {
        AsyncQueue.submit(() -> {
            CorrelationData correlationData =
                    new CorrelationData(String.format("%s#%s#%s", message.getMessageId()
                            , System.currentTimeMillis(), message.getMessageType()));
            String topic = message.getTopic();
            String routingKey = message.getRoutingKey();
            rabbitTemplateContainer.getTemplate(message).convertAndSend(topic, routingKey, message, correlationData);
            log.info("#RabbitBrokerImpl.sendKernel# send to rabbitmq, messageId:{}", message.getMessageId());
        });
    }

    @Override
    public void confirmSend(Message message) {
        message.setMessageType(MessageType.CONFIRM);
        sendKernel(message);
    }

    /**
     * 通过对数据入库，和定时任务兜底来保障可靠性
     *
     * @param message
     */
    @Override
    public void reliantSend(Message message) {
        message.setMessageType(MessageType.RELIANT);
        BrokerMessage bm = messageStoreService.selectByMessageId(message.getMessageId());
        if (bm == null) {
            Date now = new Date();
            // 1. 把消息落库
            BrokerMessage brokerMessage = new BrokerMessage();
            brokerMessage.setMessageId(message.getMessageId());
            brokerMessage.setStatus(BrokerMessageStatus.SENDING.getCode());
            brokerMessage.setNextRetry(DateUtils.addMinutes(now, BrokerMessageConst.TIMEOUT));
            brokerMessage.setCreateTime(now);
            brokerMessage.setUpdateTime(now);
            brokerMessage.setMessage(message);
            messageStoreService.insert(brokerMessage);
        }
        // 发消息
        sendKernel(message);
    }

    @Override
    public void sendMessages() {
        List<Message> messages = MessageHolder.clear();
        messages.forEach(message -> {
            MessageHolderAsyncQueue.submit(() -> {
                CorrelationData correlationData =
                        new CorrelationData(String.format("%s#%s#%s", message.getMessageId()
                                , System.currentTimeMillis(), message.getMessageType()));
                String topic = message.getTopic();
                String routingKey = message.getRoutingKey();
                rabbitTemplateContainer.getTemplate(message).convertAndSend(topic, routingKey, message, correlationData);
                log.info("#RabbitBrokerImpl.sendMessages# send to rabbitmq, messageId:{}", message.getMessageId());
            });
        });
    }
}
