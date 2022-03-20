package com.rabbit.producer.autoconfigure;

import com.rabbit.task.annotation.EnableElasticJob;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 配置类
 */
@EnableElasticJob
@Configuration
@ComponentScan({"com.rabbit.producer.*"})
public class RabbitProducerAutoConfiguration {


}
