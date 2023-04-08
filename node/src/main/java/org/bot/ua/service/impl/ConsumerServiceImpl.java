package org.bot.ua.service.impl;

import lombok.extern.log4j.Log4j;
import org.bot.ua.service.ConsumerService;
import org.bot.ua.service.MainService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;


import static org.bot.ua.model.RabbitQueue.*;

@Service
@Log4j
public class ConsumerServiceImpl implements ConsumerService {
    private final MainService mainService;

    public ConsumerServiceImpl(MainService mainService) {
        this.mainService = mainService;
    }

    @Override
    @RabbitListener(queues = TEXT_MASSAGE_UPDATE)
    public void consumeTextMessageUpdate(Update update) {
        log.debug("NODE: Text message is received");
        mainService.processTextMessage(update);
    }


    @Override
    @RabbitListener(queues = FILE_MASSAGE_UPDATE)
    public void consumeFileMessageUpdate(Update update) {
        log.debug("NODE: UserFile message is received");
        mainService.processFileMessage(update);
    }

    @Override
    @RabbitListener(queues = FILES_MASSAGE_UPDATE)
    public void consumeFilesGroupMessageUpdate(Update update) {
        log.debug("NODE: Files group message is received");
        mainService.processFilesMessage(update);
    }

    @Override
    @RabbitListener(queues = DOCUMENT_MASSAGE_UPDATE)
    public void consumeDocumentMessageUpdate(Update update) {
        if (update.hasMessage() && update.getMessage().getMediaGroupId() != null) {
            mainService.processFilesMessage(update);
        } else {
            log.debug("NODE: Document message is received");
            mainService.processFileMessage(update);
        }
    }


}
