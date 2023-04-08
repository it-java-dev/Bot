package org.bot.ua.service.impl;

import lombok.extern.log4j.Log4j;
import org.bot.ua.dao.AppUserDAO;
import org.bot.ua.dao.FilesGroupDAO;
import org.bot.ua.dao.GroupsBinaryContentDAO;
import org.bot.ua.entity.*;
import org.bot.ua.service.FilesGroupService;
import org.bot.ua.service.ProducerService;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.bot.ua.entity.enums.UserState.BASIC_STATE;


//gets telegram message, downloading file and save it at database
@Log4j
@Service
public class FilesGroupServiceImpl implements FilesGroupService {
    private final FilesGroupDAO filesGroupDAO;
    private final GroupsBinaryContentDAO groupsBinaryContentDAO;

    private final AppUserDAO appUserDAO;

    private final CommonServiceImpl commonService;

    private final ProducerService producerService;


    public FilesGroupServiceImpl(FilesGroupDAO filesGroupDAO, GroupsBinaryContentDAO groupsBinaryContentDAO, AppUserDAO appUserDAO, CommonServiceImpl commonService, ProducerService producerService) {
        this.filesGroupDAO = filesGroupDAO;
        this.groupsBinaryContentDAO = groupsBinaryContentDAO;
        this.appUserDAO = appUserDAO;
        this.commonService = commonService;

        this.producerService = producerService;
    }

    /*@Override
    public FilesGroup processFiles(Message telegramMessage, AppUser appUser) {
        var isPhoto = telegramMessage.hasPhoto();
        if (isPhoto) {
            return processPhotos(telegramMessage, appUser);
        } else {
            return processDocs(telegramMessage, appUser);
        }
    }*/


    @Override
    public void processFileMapMessage(Map<String, List<Update>> map) {
        if (!map.isEmpty()) {
            String groupId = map.keySet().iterator().next();
            List<Update> updates = map.get(groupId);
            final var chatId = updates.get(0).getMessage().getChatId();
            var appUser = findOrSaveAppUser(updates.get(0));
            try {
                processListFiles(updates, appUser);
                /*sendAnswer(groupId, chatId);*/
            } catch (Exception e) {
                log.error(e);
                String error = "Unable to download files, please try later";
                sendAnswer(error, chatId);
            }
        }
    }

    private AppUser findOrSaveAppUser(Update update) {
        User telegramUser = update.getMessage().getFrom();
        // the object is find and saved in the database
        var optional = appUserDAO.findByTelegramUserId(telegramUser.getId());
        if (optional.isEmpty()) {
            // the object is not yet represented in the database
            AppUser transientAppUser = AppUser.builder()
                    .telegramUserId(telegramUser.getId())
                    .firstLoginData(LocalDateTime.now())
                    .username(telegramUser.getUserName())
                    .firstName(telegramUser.getFirstName())
                    .lastName(telegramUser.getLastName())
                    .isActive(false)
                    .isAdmin(false)
                    .state(BASIC_STATE)
                    .build();
            return appUserDAO.save(transientAppUser);
        }
        return optional.get();
    }

    private void sendAnswer(String output, Long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(output);
        producerService.producerAnswer(sendMessage);
    }

    @Override
    public void processListFiles(List<Update> updates, AppUser appUser) {

        for (Update upd : updates) {
            var isPhoto = upd.getMessage().hasPhoto();
            if (isPhoto) {
                processPhotos(upd.getMessage(), appUser);
            } else {
                processDocs(upd.getMessage(), appUser);
            }
        }
    }

    public FilesGroup processPhotos(Message telegramMessage, AppUser appUser) {
        // In telegram lib object photo represented as PhotoSize (and gets Integer)
        var photoSizeCount = telegramMessage.getPhoto().size();
        var photoIndex = photoSizeCount > 1 ? telegramMessage.getPhoto().size() - 1 : 0;
        var telegramPhoto = telegramMessage.getPhoto().get(photoIndex);
        //getFileId
        String fileId = telegramPhoto.getFileId();
        //https get quary to telegram server

        ResponseEntity<String> response = commonService.getFilePath(fileId);
        if (response.getStatusCode().equals(HttpStatus.OK)) {
            //gets BinaryContent saved at bd and which has id
            GroupsBinaryContent persistentGroupsBinaryContent = getPersistentTemporaryBinaryPhotosContent(response, telegramMessage, appUser);
            //creating entity on base of telegramDoc and then save it at database
            FilesGroup transientPhotos = buildTransientPhotos(telegramPhoto, persistentGroupsBinaryContent, appUser);
            return filesGroupDAO.save(transientPhotos);
        } else {
            throw new RuntimeException("Bad response from telegram service: " + response);
        }
    }

/*
    public void processListPhotos(Message telegramMessage, AppUser appUser) {
        // the object is find and saved in the database
//        var appUser = appUserDAO.findByTelegramUserId(telegramMessage.getFrom().getId());

        // In telegram lib object photo represented as PhotoSize (and gets Integer)
        var photoSizeCount = telegramMessage.getPhoto().size();
        var photoIndex = photoSizeCount > 1 ? telegramMessage.getPhoto().size() - 1 : 0;
        var telegramPhoto = telegramMessage.getPhoto().get(photoIndex);
        //getFileId
        String fileId = telegramPhoto.getFileId();
        //https get quary to telegram server

        ResponseEntity<String> response = commonService.getFilePath(fileId);
        if (response.getStatusCode().equals(HttpStatus.OK)) {
            //gets BinaryContent saved at bd and which has id
            GroupsBinaryContent persistentGroupsBinaryContent = getPersistentTemporaryBinaryPhotosContent(response, telegramMessage, appUser);
            //creating entity on base of telegramDoc and then save it at database
            FilesGroup transientPhotos = buildTransientPhotos(telegramPhoto, persistentGroupsBinaryContent, appUser);
            filesGroupDAO.save(transientPhotos);
        } else {
            throw new RuntimeException("Bad response from telegram service: " + response);
        }
    }
*/

    private GroupsBinaryContent getPersistentTemporaryBinaryPhotosContent(ResponseEntity<String> response, Message message, AppUser appUser) {
        //gets file path from method
        String filePath = commonService.getFilePath(response);
        // download file as array of byte
        byte[] fileInByte = commonService.downloadFile(filePath);
        //not saved yet at database
        String size = commonService.generateFileSizeInMb(fileInByte);
        String name = commonService.generateUniqueFileName(filePath);


        GroupsBinaryContent transientGroupsBinaryContent = GroupsBinaryContent.builder()
                .fileAsArrayOfBytes(fileInByte)
                .fileSize(size)
                .fileName(name)
                .groupId(message.getMediaGroupId())
                .appUser(appUser)
                .build();

        return groupsBinaryContentDAO.save(transientGroupsBinaryContent);
    }

    private FilesGroup buildTransientPhotos(PhotoSize telegramPhoto, GroupsBinaryContent persistentGroupsBinaryContent, AppUser appUser) {
        return FilesGroup.builder()
                .telegramFileId(telegramPhoto.getFileId())
                .groupsBinaryContent(persistentGroupsBinaryContent)
                .fileSizeMb(persistentGroupsBinaryContent.getFileSize())
                .fileName(persistentGroupsBinaryContent.getFileName())
                .mimeType("image/jpeg")
                .groupId(persistentGroupsBinaryContent.getGroupId())
                .appUser(appUser)
                .build();
    }

    public FilesGroup processDocs(Message telegramMessage, AppUser appUser) {
        //gets doc from message
        Document telegramDocument = telegramMessage.getDocument();
        //getFileId
        String fileId = telegramDocument.getFileId();
        String groupId = telegramMessage.getMediaGroupId();

        //https get quary to telegram server
        ResponseEntity<String> response = commonService.getFilePath(fileId);
        if (response.getStatusCode().equals(HttpStatus.OK)) {
            //gets BinaryContent saved at bd and which has id
            GroupsBinaryContent persistentGroupBinaryDocumentsContent = getPersistentGroupBinaryDocumentsContent(response, telegramDocument, telegramMessage, appUser);
            //creating entity on base of telegramDoc and then save it at database
            FilesGroup transientFileGroup = buildTransientFilesGroup(telegramDocument, persistentGroupBinaryDocumentsContent, groupId, appUser);
            return filesGroupDAO.save(transientFileGroup);
        } else {
            throw new RuntimeException("Bad response from telegram service: " + response);
        }

    }

    private GroupsBinaryContent getPersistentGroupBinaryDocumentsContent(ResponseEntity<String> response, Document document, Message message, AppUser appUser) {
        //gets file path from method
        String filePath = commonService.getFilePath(response);
        // download file as array of byte
        byte[] fileInByte = commonService.downloadFile(filePath);
        //not saved yet at database
        String size = commonService.generateFileSizeInMb(fileInByte);
        GroupsBinaryContent transientGroupsBinaryContent = GroupsBinaryContent.builder()
                .fileAsArrayOfBytes(fileInByte)
                .fileName(document.getFileName())
                .groupId(message.getMediaGroupId())
                .fileSize(size)
                .appUser(appUser)
                .build();

        return groupsBinaryContentDAO.save(transientGroupsBinaryContent);
    }

    private FilesGroup buildTransientFilesGroup(Document telegramDocument, GroupsBinaryContent persistentGroupsBinaryContent, String groupId, AppUser appUser) {
        return FilesGroup.builder()
                .telegramFileId(telegramDocument.getFileId())
                .fileName(telegramDocument.getFileName())
                .groupsBinaryContent(persistentGroupsBinaryContent)
                .mimeType(telegramDocument.getMimeType())
                .fileSizeMb(persistentGroupsBinaryContent.getFileSize())
                .groupId(groupId)
                .appUser(appUser)
                .build();
    }
}
