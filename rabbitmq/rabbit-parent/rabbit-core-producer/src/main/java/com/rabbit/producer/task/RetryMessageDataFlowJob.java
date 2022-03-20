package com.rabbit.producer.task;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.dataflow.DataflowJob;
import com.rabbit.producer.broker.RabbitBroker;
import com.rabbit.producer.constant.BrokerMessageStatus;
import com.rabbit.producer.entity.BrokerMessage;
import com.rabbit.producer.service.MessageStoreService;
import com.rabbit.task.annotation.ElasticJobConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@ElasticJobConfig(
        name = "com.rabbit.producer.task.RetryMessageDataFlowJob",
        cron = "0/10 * * * * ?",
        description = "可靠性投递消息补偿任务",
        overwrite = true,
        shardingTotalCount = 1
)
public class RetryMessageDataFlowJob implements DataflowJob<BrokerMessage> {

    @Autowired
    private MessageStoreService messageStoreService;
    @Autowired
    private RabbitBroker rabbitBroker;

    private static final int MAX_RETRY_TIME = 3;

    @Override
    public List<BrokerMessage> fetchData(ShardingContext shardingContext) {
        List<BrokerMessage> list = messageStoreService.fetchTimeOutMessage4Retry(BrokerMessageStatus.SENDING);
        log.info("----------抓取没接收到confirm的数据集合，数量{}  ------------", list.size());
        return list;
    }

    @Override
    public void processData(ShardingContext shardingContext, List<BrokerMessage> list) {
        list.forEach(brokerMessage -> {
            if (brokerMessage.getTryCount() >= MAX_RETRY_TIME) {
                messageStoreService.failure(brokerMessage.getMessageId());
                log.warn("-----消息重试次数达到最大，消息设置为最终失败，消息id:{} -----", brokerMessage.getMessageId());
            } else {
                messageStoreService.updateTryCount(brokerMessage.getMessageId());
                rabbitBroker.reliantSend(brokerMessage.getMessage());
            }
        });
    }
}
