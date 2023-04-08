package org.bot.ua.service.impl;

import lombok.extern.log4j.Log4j;
import org.bot.ua.dao.BinaryContentDAO;
import org.bot.ua.dao.UserFileDAO;
import org.bot.ua.entity.UserFile;
import org.bot.ua.entity.AppUser;
import org.bot.ua.entity.BinaryContent;
import org.bot.ua.service.FileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;


//gets telegram message, downloading file and save it at database
@Log4j
@Service
public class FileServiceImpl implements FileService {
    private final UserFileDAO userFileDAO;
    private final BinaryContentDAO binaryContentDAO;
    private final CommonServiceImpl commonService;

    public FileServiceImpl(UserFileDAO userFileDAO, BinaryContentDAO binaryContentDAO, CommonServiceImpl commonService) {
        this.userFileDAO = userFileDAO;
        this.binaryContentDAO = binaryContentDAO;
        this.commonService = commonService;
    }

    @Override
    public UserFile processFile(Message telegramMessage, AppUser appUser) {
        var isPhoto = telegramMessage.hasPhoto();
        if (isPhoto) {
            return processPhoto(telegramMessage, appUser);
        } else {
            return processDoc(telegramMessage, appUser);
        }
    }


    public UserFile processPhoto(Message telegramMessage, AppUser appUser) {
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
            BinaryContent persistentBinaryContent = getPersistentBinaryPhotoContent(response, appUser);
            //creating entity on base of telegramDoc and then save it at database
            UserFile transientUserFile = buildTransientPhoto(telegramPhoto, persistentBinaryContent, appUser);
            return userFileDAO.save(transientUserFile);
        } else {
            throw new RuntimeException("Bad response from telegram service: " + response);
        }
    }

    private BinaryContent getPersistentBinaryPhotoContent(ResponseEntity<String> response, AppUser appUser) {
        //gets file path from method
        String filePath = commonService.getFilePath(response);
        // download file ass array of byte
        byte[] fileInByte = commonService.downloadFile(filePath);
        String size = commonService.generateFileSizeInMb(fileInByte);
        String name = commonService.generateUniqueFileName(filePath);
        //not saved yet at database
        BinaryContent transientBinaryContent = BinaryContent.builder()
                .fileAsArrayOfBytes(fileInByte)
                .fileSize(size)
                .fileName(name)
                .appUser(appUser)
                .build();

        return binaryContentDAO.save(transientBinaryContent);
    }

    private UserFile buildTransientPhoto(PhotoSize telegramPhoto, BinaryContent persistentBinaryContent, AppUser appUser) {
        return UserFile.builder()
                .telegramFileId(telegramPhoto.getFileId())
                .binaryContent(persistentBinaryContent)
                .fileSizeMb(persistentBinaryContent.getFileSize())
                .fileName(persistentBinaryContent.getFileName())
                .mimeType("image/jpeg")
                .appUser(appUser)
                .build();
    }

    public UserFile processDoc(Message telegramMessage, AppUser appUser) {
        //gets doc from message
        Document telegramDocument = telegramMessage.getDocument();
        //getFileId
        String fileId = telegramDocument.getFileId();
        //https get quary to telegram server
        ResponseEntity<String> response = commonService.getFilePath(fileId);
        if (response.getStatusCode().equals(HttpStatus.OK)) {
            //gets BinaryContent saved at bd and which has id
            BinaryContent persistentBinaryContent = getPersistentBinaryDocContent(response, telegramDocument, appUser);
            //creating entity on base of telegramDoc and then save it at database
            UserFile transientUserFile = buildTransientDoc(telegramDocument, persistentBinaryContent, appUser);
            return userFileDAO.save(transientUserFile);
        } else {
            throw new RuntimeException("Bad response from telegram service: " + response);
        }

    }
    private BinaryContent getPersistentBinaryDocContent(ResponseEntity<String> response, Document document, AppUser appUser) {
        //gets file path from method
        String filePath = commonService.getFilePath(response);
        // download file as array of byte
        byte[] fileInByte = commonService.downloadFile(filePath);
        //not saved yet at database
        String size = commonService.generateFileSizeInMb(fileInByte);
        BinaryContent transientBinaryContent = BinaryContent.builder()
                .fileAsArrayOfBytes(fileInByte)
                .fileName(document.getFileName())
                .fileSize(size)
                .appUser(appUser)
                .build();

        return binaryContentDAO.save(transientBinaryContent);
    }

    private UserFile buildTransientDoc(Document telegramDocument, BinaryContent persistentBinaryContent, AppUser appUser) {
        return UserFile.builder()
                .telegramFileId(telegramDocument.getFileId())
                .fileName(telegramDocument.getFileName())
                .binaryContent(persistentBinaryContent)
                .mimeType(telegramDocument.getMimeType())
                .fileSizeMb(persistentBinaryContent.getFileSize())
                .appUser(appUser)
                .build();
    }

}
