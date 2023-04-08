package org.bot.ua.service;

import org.bot.ua.entity.AppUser;


public interface ZipService {
    String generateFilesArchiveById(AppUser appUser, String groupId);
}
