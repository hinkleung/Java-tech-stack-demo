package com.rabbit.common.serializer.impl;


import com.rabbit.api.Message;
import com.rabbit.common.serializer.Serializer;
import com.rabbit.common.serializer.SerializerFactory;

public class JacksonSerializerFactory implements SerializerFactory {

    public static final SerializerFactory INSTANCE = new JacksonSerializerFactory();

    @Override
    public Serializer create() {
        return JacksonSerializer.createParametricType(Message.class);
    }

}
