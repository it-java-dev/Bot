package org.bot.ua.service;


import org.telegram.telegrambots.meta.api.objects.Update;

/*Gets answer from rabbitmq(name of queue and update as data) and deliver to controller*/
public interface UpdateProducer {
    void producer(String rabbitQueue, Update update);
}
