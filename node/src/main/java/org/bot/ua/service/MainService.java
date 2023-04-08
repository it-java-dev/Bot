package org.bot.ua.service;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface MainService {
    void processTextMessage(Update update);
    void processFileMessage(Update update);
    void processFilesMessage(Update update);
    /*void processFileMapMessage(Map<String, List<Update>> map);*/


}
