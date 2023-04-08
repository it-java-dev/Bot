package org.bot.ua.service;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface ConsumerService {
    void consumeTextMessageUpdate(Update update);
    void consumeDocumentMessageUpdate(Update update);
    void consumeFileMessageUpdate(Update update);
    void consumeFilesGroupMessageUpdate(Update update);

}
