package com.rabbit.api;

import com.rabbit.exception.MessageRunTimeException;

public interface MessageListener {

    void onMessage(Message message) throws MessageRunTimeException;

}
