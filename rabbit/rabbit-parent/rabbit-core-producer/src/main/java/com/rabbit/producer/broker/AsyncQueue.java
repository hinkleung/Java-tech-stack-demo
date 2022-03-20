package com.rabbit.producer.broker;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

@Slf4j
public class AsyncQueue {

    private static final int THREAD_SIZE = Runtime.getRuntime().availableProcessors();

    private static final int QUEUE_SIZE = 10000;

    private static final ExecutorService senderAsync =
            new ThreadPoolExecutor(THREAD_SIZE
                    , THREAD_SIZE * 2
                    , 60L
                    , TimeUnit.SECONDS
                    , new ArrayBlockingQueue<>(QUEUE_SIZE),
                    r -> {
                        Thread t = new Thread(r);
                        t.setName("rabbitmq_client_async_sender");
                        return t;
                    },
                    (r, executor) -> log.error("async sender is error rejected, runnable:{}, executor:{}", r, executor));

    public static void submit(Runnable runnable) {
        senderAsync.submit(runnable);
    }

}
