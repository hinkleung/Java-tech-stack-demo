package com.rabbit.producer.broker;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.rabbit.api.Message;
import com.rabbit.api.MessageType;
import com.rabbit.common.convert.GenericMessageConverter;
import com.rabbit.common.convert.RabbitMessageConverter;
import com.rabbit.common.serializer.Serializer;
import com.rabbit.common.serializer.SerializerFactory;
import com.rabbit.common.serializer.impl.JacksonSerializerFactory;
import com.rabbit.exception.MessageRunTimeException;
import com.rabbit.producer.service.MessageStoreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 池化封装RabbitTemplate
 */
@Component
@Slf4j
public class RabbitTemplateContainer implements RabbitTemplate.ConfirmCallback {

    /**
     * key -> topic
     */
    private final Map<String, RabbitTemplate> rabbitMap = new ConcurrentHashMap<>();

    private Splitter splitter = Splitter.on("#");

    private SerializerFactory serializerFactory = JacksonSerializerFactory.INSTANCE;

    @Autowired
    private ConnectionFactory connectionFactory;
    @Autowired
    private MessageStoreService messageStoreService;

    public RabbitTemplate getTemplate(Message message) throws MessageRunTimeException {
        Preconditions.checkNotNull(message);
        String topic = message.getTopic();
        RabbitTemplate rabbitTemplate = rabbitMap.get(topic);
        if (rabbitTemplate != null) {
            return rabbitTemplate;
        }
        log.info("#RabbitTemplateContainer.getTemplate#topic:{} is not exists, create one", topic);
        rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setExchange(topic);
        rabbitTemplate.setRoutingKey(message.getRoutingKey());
        rabbitTemplate.setRetryTemplate(new RetryTemplate());
        // 对于Message的序列化
        Serializer serializer = serializerFactory.create();
        GenericMessageConverter gmc = new GenericMessageConverter(serializer);
        RabbitMessageConverter rmc = new RabbitMessageConverter(gmc);
        rabbitTemplate.setMessageConverter(rmc);

        String messageType = message.getMessageType();
        if (!MessageType.RAPID.equals(messageType)) {
            rabbitTemplate.setConfirmCallback(this);
        }

        rabbitMap.putIfAbsent(topic, rabbitTemplate);
        return rabbitTemplate;
    }

    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        List<String> strings = splitter.splitToList(correlationData.getId());
        String messageId = strings.get(0);
        long sendTime = Long.parseLong(strings.get(1));
        String messageType = strings.get(2);
        if (ack) {
            log.info("send message is OK, confirm messageId: {}, sendTime: {}", messageId, sendTime);
            // 如果当前消息类型为reliant 我们就去数据库进行更新
            if (MessageType.RELIANT.equals(messageType)) {
                messageStoreService.success(messageId);
            }
        } else {
            log.error("send message is FAIL, confirm messageId: {}, sendTime: {}", messageId, sendTime);
        }

    }

}
