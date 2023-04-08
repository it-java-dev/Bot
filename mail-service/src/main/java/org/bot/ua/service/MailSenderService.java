package org.bot.ua.service;

import org.bot.ua.dto.MailParams;

public interface MailSenderService {
    void send(MailParams mailParams);
}
