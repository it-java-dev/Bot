package org.bot.ua.service;

import org.bot.ua.entity.UserFile;
import org.bot.ua.entity.AppUser;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface FileService {
    UserFile processFile(Message telegramMessage, AppUser appUser);
}
