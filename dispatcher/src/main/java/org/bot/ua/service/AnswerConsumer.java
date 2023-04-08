package org.bot.ua.service;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;


/*takes object sandMessage and deliver our update in rabbitmq,
* name of queue will be introduced in service as annotation*/
public interface AnswerConsumer {
    void consume(SendMessage sendMessage);
}
