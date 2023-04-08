package org.bot.ua.service.impl;

import lombok.extern.log4j.Log4j;
import org.bot.ua.dao.*;
import org.bot.ua.entity.*;
import org.bot.ua.service.FilesGroupService;
import org.bot.ua.service.ZipService;
import org.bot.ua.service.enums.LinkType;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


@Service
@Log4j
public class ZipServiceImpl implements ZipService {

    private final GroupsBinaryContentDAO groupsBinaryContentDAO;
    private final ZipBinaryContentDAO zipBinaryContentDAO;
    private final ProducerServiceImpl producerService;
    private final CommonServiceImpl commonService;
    private final RawsDataDAO rawsDataDAO;
    private final FilesGroupService filesGroupService;


    public ZipServiceImpl(GroupsBinaryContentDAO groupsBinaryContentDAO, ZipBinaryContentDAO zipBinaryContentDAO, ProducerServiceImpl producerService, CommonServiceImpl commonService, RawsDataDAO rawsDataDAO, FilesGroupService filesGroupService) {
        this.groupsBinaryContentDAO = groupsBinaryContentDAO;
        this.zipBinaryContentDAO = zipBinaryContentDAO;
        this.producerService = producerService;
        this.commonService = commonService;
        this.rawsDataDAO = rawsDataDAO;
        this.filesGroupService = filesGroupService;
    }

/*if (rawsDataDAO.existsByAppUserAndMapContainsKey(appUser, groupId)) {

        } else {
            log.info("Files not found with group ID: " + groupId);
            return "Unable to find files with this key: " + groupId + ", please use correct key, or files may have been automatically deleted.";
        }*/






    @Override
    public String generateFilesArchiveById(AppUser appUser, String groupId) {


        if (!rawsDataDAO.existsByAppUserAndMapContainsKey(appUser, groupId)) {
            log.info("Files not found with group ID: " + groupId);
            return "Unable to find files with this key: " + groupId + ", please use correct key, or files may have been automatically deleted.";
        }
        RawsData rawsDataList = rawsDataDAO.findByAppUserAndMapContainsKey(appUser, groupId);
        if (rawsDataList.getMap().isEmpty()) {
            log.info("Files not found with group ID: " + groupId);
            return "Unable to download group of files, please try later, or send the files one at a time, or use correct key.";
        }
        List<GroupsBinaryContent> group = groupsBinaryContentDAO.findAllIdByGroupId(groupId);
        if (!group.isEmpty()) {
            if (isAllGroupsZipped(group)) {
                log.info("Tried to recreate zip archive for group ID:" + groupId);
                return "The link has already been created";
            }
        }



        filesGroupService.processFileMapMessage(rawsDataList.getMap());
        List<GroupsBinaryContent> groupList = groupsBinaryContentDAO.findAllIdByGroupId(groupId);
        if (groupList.isEmpty()) {
            log.info("Files not found with group ID: " + groupId);
            return "Unable to download group of files, please try later, or send the files one at a time, or use correct key.";
        }



        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ZipOutputStream zos = new ZipOutputStream(baos);
            for (GroupsBinaryContent file : groupList) {
                ZipEntry zipEntry = new ZipEntry(file.getFileName());
                zos.putNextEntry(zipEntry);
                zos.write(file.getFileAsArrayOfBytes());
                zos.closeEntry();
            }
            zos.close();
            byte[] zipBytes = baos.toByteArray();
            String size = commonService.generateFileSizeInMb(zipBytes);

            ZipBinaryContent zipBinaryContent = ZipBinaryContent.builder()
                    .fileAsArrayOfBytes(zipBytes)
                    .fileName("files.zip")
                    .fileSize(size)
                    .mimeType("application/zip")
                    .appUser(appUser)
                    .build();
            zipBinaryContentDAO.save(zipBinaryContent);


            for (GroupsBinaryContent file : groupList) {
                GroupsBinaryContent groups = GroupsBinaryContent.builder()
                        .id(file.getId())
                        .fileAsArrayOfBytes(file.getFileAsArrayOfBytes())
                        .fileName(file.getFileName())
                        .fileSize(file.getFileSize())
                        .groupId(file.getGroupId())
                        .appUser(file.getAppUser())
                        .zipBinaryContent(zipBinaryContent)
                        .build();
                groupsBinaryContentDAO.saveAndFlush(groups);
            }


            sendingFilesLink(zipBinaryContent, appUser.getTelegramUserId());
            log.info("Successfully created archive for group ID:" + groupId);
            return "Successfully created archive";
        } catch (IOException e) {
            log.error("Error creating zip archive: " + e.getMessage());
            return "Unable to create archive, files with key: " + groupId + "don't exist.";
        }
    }

    private boolean isAllGroupsZipped(List<GroupsBinaryContent> groupList) {
        for (GroupsBinaryContent group : groupList) {
            if (group.getZipBinaryContent() == null) {
                return false;
            }
        }
        return true;
    }


    private void sendAnswer(String output, Long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(output);
        producerService.producerAnswer(sendMessage);
    }


    private void sendingFilesLink(ZipBinaryContent files, Long chatId) {
        String link = commonService.generateLink(files.getId(), LinkType.GET_FILES);
        var answer = "Files successfully upload! "
                + "Link for downloading: " + link;
        sendAnswer(answer, chatId);
    }

    /*private void resendingFilesLink(ZipBinaryContent files, Long chatId) {
        String link = commonService.regenerateLink(files.getId(), LinkType.GET_FILES);
        var answer = "Link for downloading: " + link;
        sendAnswer(answer, chatId);
    }*/
}
