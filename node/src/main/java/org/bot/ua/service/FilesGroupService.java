package org.bot.ua.service;

import org.bot.ua.entity.FilesGroup;
import org.bot.ua.entity.AppUser;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.Map;

public interface FilesGroupService {
    /*FilesGroup processFiles(Message telegramMessage, AppUser appUser);*/
    void processListFiles(List<Update> updates, AppUser appUser);

    void processFileMapMessage(Map<String, List<Update>> map);

}
