package org.bot.ua.service;


import org.bot.ua.entity.FilesGroup;
import org.bot.ua.entity.ZipBinaryContent;

public interface ZipGroupService {
    ZipBinaryContent getZip(String id);
}
