package com.rabbit.producer.broker;

import com.google.common.collect.Lists;
import com.rabbit.api.Message;

import java.util.List;

public class MessageHolder {

    private List<Message> messageList = Lists.newArrayList();

    public static final ThreadLocal<MessageHolder> holder = new ThreadLocal<MessageHolder>(){
        @Override
        protected MessageHolder initialValue() {
            return new MessageHolder();
        }
    };

    public static void add(Message message) {
        holder.get().messageList.add(message);
    }

    public static List<Message> clear() {
        List<Message> tmp = Lists.newArrayList(holder.get().messageList);
        holder.remove();
        return tmp;
    }
}
