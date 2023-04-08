package org.bot.ua.controller;

import lombok.extern.log4j.Log4j;
import org.bot.ua.service.UpdateProducer;
import org.bot.ua.utils.MessageUtils;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import static org.bot.ua.model.RabbitQueue.*;

@Component
@Log4j
public class UpdateController {
    private TelegramBot telegramBot;
    private final MessageUtils messageUtils;
    private final UpdateProducer updateProducer;

    public UpdateController(MessageUtils messageUtils, UpdateProducer updateProducer) {
        this.messageUtils = messageUtils;
        this.updateProducer = updateProducer;
    }


    public void registerBot(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    public void processUpdate(Update update) {
        if (update == null) {
            log.error("Received update is null");
        } else if (update.hasMessage()) {
            distributeMessagesByType(update);
        } else {
            log.error("Unsupported massage type is received: " + update);
            setUnsupportedMessageTypeView(update);
        }
    }


        private void distributeMessagesByType(Update update) {
        var message = update.getMessage();
        var isGroup = message.getMediaGroupId() != null;
        if (message.hasText()) {
            processTextMessage(update);
        } else if (message.hasDocument() && isGroup) {
            processFileGroupMessage(update);
        } else if (message.hasDocument()) {
            processFileMessage(update);
        } else if (message.hasPhoto() && isGroup) {
            processFileGroupMessage(update);
        } else if (message.hasPhoto()) {
            processFileMessage(update);
        } else {
            setUnsupportedMessageTypeView(update);
        }

    }

    private void setUnsupportedMessageTypeView(Update update) {
        var sendMassage = messageUtils.generateSendMessageWithText(update, "Non supported type of massage");
        setView(sendMassage);
    }

    /*Method said, that file received and don't need repeat sending of file*/
    private void setFileIsReceivedView(Update update) {
        var sendMassage = messageUtils.generateSendMessageWithText(update, "File received, please wait...");
        setView(sendMassage);
    }

    public void setView(SendMessage sendMassage) {
        telegramBot.sendAnswerMessage(sendMassage);
    }

    private void processTextMessage(Update update) {
        updateProducer.producer(TEXT_MASSAGE_UPDATE, update);
    }

    /*private void processDocumentMessage(Update update) {
        updateProducer.producer(DOCUMENT_MASSAGE_UPDATE, update);
        setFileIsReceivedView(update);
    }

    private void processPhotoMessage(Update update) {
        updateProducer.producer(PHOTO_MASSAGE_UPDATE, update);
        setFileIsReceivedView(update);
    }*/

    private void processFileMessage(Update update) {
        updateProducer.producer(FILE_MASSAGE_UPDATE, update);
        setFileIsReceivedView(update);
    }

    private void processFileGroupMessage(Update update) {
        updateProducer.producer(FILES_MASSAGE_UPDATE, update);
    }
}
