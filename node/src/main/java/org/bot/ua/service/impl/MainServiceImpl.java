package org.bot.ua.service.impl;

import lombok.extern.log4j.Log4j;
import org.bot.ua.dao.*;
import org.bot.ua.entity.*;
import org.bot.ua.entity.UserFile;
import org.bot.ua.service.UserService;
import org.bot.ua.service.FileService;
import org.bot.ua.service.MainService;
import org.bot.ua.service.enums.LinkType;
import org.bot.ua.service.enums.ServiceCommand;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.bot.ua.entity.enums.UserState.BASIC_STATE;
import static org.bot.ua.entity.enums.UserState.WAIT_FOE_EMAIL_STATE;
import static org.bot.ua.service.enums.ServiceCommand.*;

/*MainServiceImpl create link between DB throw RawDataDAO and ConsumerServiceImpl,
 * is designed for transferring message from rabbitmq*/
@Service
@Log4j
public class MainServiceImpl implements MainService {
    private final RawDataDAO rawDataDAO;
    private final RawsDataDAO rawsDataDAO;
    private final ProducerServiceImpl producerService;
    private final AppUserDAO appUserDAO;
    private final FileService fileService;
    private final UserService userService;
    private final FilesGroupDAO filesGroupDAO;
    private final UserFileDAO appFileDAO;
    private final ZipServiceImpl zipService;
    private final ZipBinaryContentDAO zipBinaryContentDAO;
    private final CommonServiceImpl commonService;

    public MainServiceImpl(RawDataDAO rawDataDAO,
                           RawsDataDAO rawsDataDAO, ProducerServiceImpl producerService,
                           AppUserDAO appUserDAO, FileService fileService,
                           UserService userService,
                           FilesGroupDAO filesGroupDAO,
                           UserFileDAO appFileDAO,
                           ZipServiceImpl zipService, ZipBinaryContentDAO zipBinaryContentDAO, CommonServiceImpl commonService) {
        this.rawDataDAO = rawDataDAO;
        this.rawsDataDAO = rawsDataDAO;
        this.producerService = producerService;
        this.appUserDAO = appUserDAO;
        this.fileService = fileService;
        this.userService = userService;
        this.filesGroupDAO = filesGroupDAO;
        this.appFileDAO = appFileDAO;
        this.zipService = zipService;
        this.zipBinaryContentDAO = zipBinaryContentDAO;
        this.commonService = commonService;
    }



    @Override
    public void processTextMessage(Update update) {
        saveRawData(update);
        var appUser = findOrSaveAppUser(update);
        var userState = appUser.getState();
        var text = update.getMessage().getText();
        var output = "";

        var serviceCommand = ServiceCommand.fromValue(text);

        if (CANCEL.equals(serviceCommand)) {
            output = cancelProcess(appUser);
        } else if (BASIC_STATE.equals(userState)) {
            output = processServiceCommand(appUser, text);
        } else if (WAIT_FOE_EMAIL_STATE.equals(userState)) {
            output = userService.setEmail(appUser, text);
        } else {
            log.debug("Unknown user state: " + userState);
            output = "Unknown command! Please enter /cancel or try again.";
        }
        var chatId = update.getMessage().getChatId();
        sendAnswer(output, chatId);
    }

    private void saveRawData(Update update) {
        RawData rawData = RawData.builder()
                .event(update)
                .build();
        rawDataDAO.save(rawData);
    }
    private void saveRawsData(AppUser appUser, Long chatId, Update update) {
        final String groupId = update.getMessage().getMediaGroupId();

        try {
            String key = update.getMessage().getMediaGroupId();
            RawsData rawsData = rawsDataDAO.findByAppUserAndMapContainsKey(appUser, key);

            if (rawsData == null) {
                Map<String, List<Update>> map = new HashMap<>();
                List<Update> value = new ArrayList<>();
                value.add(update);
                map.put(key, value);

                rawsData = RawsData.builder()
                        .appUser(appUser)
                        .map(map)
                        .build();
                sendAnswer("File group received, please wait...", chatId);
                sendAnswer(groupId, chatId);
            } else {
                Map<String, List<Update>> map = rawsData.getMap();
                List<Update> value = map.get(key);
                if (value == null) {
                    value = new ArrayList<>();
                }
                value.add(update);
                map.put(key, value);
            }

            rawsDataDAO.save(rawsData);
        } catch (Exception e) {
            log.error(e.getMessage());
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

    private String cancelProcess(AppUser appUser) {
        appUser.setState(BASIC_STATE);
        appUserDAO.save(appUser);
        return "Command canceled";
    }

    private String processServiceCommand(AppUser appUser, String text) {
        var serviceCommand = ServiceCommand.fromValue(text);

        if (REGISTRATION.equals(serviceCommand)) {
            return userService.registerUser(appUser);
        } else if (isValidKeyInput(text)) {
            String groupId = text.substring(KEY.toString().length());
            return zipService.generateFilesArchiveById(appUser, groupId.trim());
        } else if (HELP.equals(serviceCommand)) {
            return help(appUser);
        } else if (START.equals(serviceCommand)) {
            return getWelcomeMessage();
        } else if (DELETE_ALL_MY_GROUP.equals(serviceCommand)) {
            return deleteAllMyFilesGroupFromDataBase(appUser);
        } else if (DELETE_ALL_MY_FILES.equals(serviceCommand)) {
            return deleteAllMyFilesFromDataBase(appUser);
        } else if (checkIfAdminCommand(appUser, text) == true) {
            return "Admin command";
        } else {
            return "Unknown command! To see a list of available commands enter /help";
        }
    }


    public boolean isValidKeyInput(String input) {
        if (input.startsWith(String.valueOf(KEY))) {
            String numbers = input.substring(4).trim();
            if (numbers.isEmpty() || !numbers.matches("\\d+")) {
                return false;
            }
            return true;
        }
        return false;
    }


    private String help(AppUser appUser) {
        if (appUser.getIsAdmin()) {
            return "The list of available commands:\n"
                    + "/cancel - cancel the execution of the current command.\n"
                    + "/registration - registration of user.\n"
                    + "/deleteAllMyFiles - delete all your single files.\n"
                    + "/deleteAllMyGroups - delete all your group of files.\n"
                    + "/key - enter your key to get archive of files in format as \n" +
                    "@key xxxxxxxxxxxxxxxxx\n" +
                    "(where xxxxxxxxxxxxxxxxxx is the group of numbers you get after sending a group of files)\n\n"
                    + "Admin commands:\n"
                    + "/broadcast - send a message to all users of the bot.\n"
                    + "/users - show list of users.\n" +
                    "(Username, Email, LoginData, isActive, isAdmin)\n"
                    + "/deleteAll - delete all files(photos/documents) from db.";
        } else {
            return "The list of available commands:\n"
                    + "/cancel - cancel the execution of the current command.\n"
                    + "/registration - registration of user.\n"
                    + "/deleteAllMyFiles - delete all your single files.\n"
                    + "/deleteAllMyGroups - delete all your group of files.\n"
                    + "/key - enter your key to get archive of files in format as \n" +
                    "@key xxxxxxxxxxxxxxxxx\n" +
                    "(where xxxxxxxxxxxxxxxxxx is the group of numbers you get after sending a group of files)\n\n";
        }
    }

    public String getWelcomeMessage() {
        String message = "Hello and welcome to FileFusionBot!\n\n" +
                "Please note that this bot is still in beta version and may have some issues. It was created as a diploma project and is designed to help you easily share and download documents and photos.\n\n" +
                "If you're not sure whether you need this bot, please don't use it.\n\n" +
                "To get started, please complete the registration process by typing /registration and following the instructions. Once you're registered, simply send a document or photo and the bot will create a link for you to download the file. If you have any questions, type /help for more information.\n\n" +
                "Thank you for choosing FileFusionBot!";
        return message;
    }


    private String deleteAllMyFilesGroupFromDataBase(AppUser appUser) {
        try {
            List<FilesGroup> filesGroups = filesGroupDAO.findAllByAppUser(appUser);
            filesGroupDAO.deleteAll(filesGroups);

            List<ZipBinaryContent> filesZipGroups = zipBinaryContentDAO.findAllByAppUser(appUser);
            zipBinaryContentDAO.deleteAll(filesZipGroups);

            return "deleted successfully";
        } catch (Exception e) {
            log.error(e);
            return "deletion failed, please try later or the files will be deleted automatically later";
        }
    }

    private String deleteAllMyFilesFromDataBase(AppUser appUser) {
        try {
            List<UserFile> userFiles = appFileDAO.findAllByAppUser(appUser);
            appFileDAO.deleteAll(userFiles);
            return "deleted successfully";
        } catch (Exception e) {
            log.error(e);
            return "deletion failed, please try later or files will be deleted automatically later";
        }
    }

    private boolean checkIfAdminCommand(AppUser appUser, String text) {
        if (appUser == null || !appUser.getIsAdmin()) {
            return false;
        }
        if (text.startsWith(String.valueOf(BROADCAST))) {
            log.info("Admin command received: " + BROADCAST);
            text = text.substring(BROADCAST.toString().length());
            broadcast(text);
            return true;
        } else if (text.startsWith(String.valueOf(LIST_USERS))) {
            log.info("Admin command received: " + LIST_USERS);
            listUsers(appUser);
            return true;
        } else if (text.startsWith(String.valueOf(DELETE_ALL))) {
            log.info("Admin command received: " + DELETE_ALL);
            deleteAllFromDB();
            return true;
        }

        return false;
    }



    private void broadcast(String text) {
        List<AppUser> appUsers = appUserDAO.findAll();
        appUsers.forEach(user -> sendAnswer(text, user.getTelegramUserId()));
    }

    private void listUsers(AppUser appUser) {
        StringBuilder sb = new StringBuilder("All user list:\r\n");
        List<AppUser> appUsers = appUserDAO.findAll();

        appUsers.forEach(customUser -> sb
                .append(appUser.getUsername())
                .append(' ')
                .append(appUser.getEmail())
                .append(' ')
                .append(appUser.getFirstLoginData().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")))
                .append(' ')
                .append(appUser.getIsActive())
                .append(' ')
                .append(appUser.getIsAdmin())
                .append("\r\n")
        );

        sendAnswer(sb.toString(), appUser.getTelegramUserId());
    }

    private String deleteAllFromDB() {
        List<FilesGroup> filesGroups = filesGroupDAO.findAll();
        filesGroupDAO.deleteAll(filesGroups);
        List<UserFile> userFile = appFileDAO.findAll();
        appFileDAO.deleteAll(userFile);
        return "OK";
    }

    @Override
    public void processFileMessage(Update update) {
        saveRawData(update);
        var appUser = findOrSaveAppUser(update);
        var chatId = update.getMessage().getChatId();
        if (isNotAllowToSendContent(chatId, appUser) == true) {
        } else {
            try {
                UserFile userfile = fileService.processFile(update.getMessage(), appUser);
                sendingFileLink(userfile, chatId);
            } catch (Exception e) {
                log.error(e);
                String error = "Unable to download document, please try later";
                sendAnswer(error, chatId);
            }
        }
    }

    @Override
    public void processFilesMessage(Update update) {
        var appUser = findOrSaveAppUser(update);
        var chatId = update.getMessage().getChatId();
        if (isNotAllowToSendContent(chatId, appUser) == true) {
        } else {
            try {
                saveRawsData(appUser, chatId, update);
            } catch (Exception e) {
                log.error(e);
                String error = "Unable to download files, please try later";
                sendAnswer(error, chatId);
            }
        }
    }

    private void sendingFileLink(UserFile userfile, Long chatId) {
        String link = commonService.generateLink(userfile.getId(), LinkType.GET_FILE);
        var answer = "File successfully upload! "
                + "Link for downloading: " + link;
        sendAnswer(answer, chatId);
    }


    private boolean isNotAllowToSendContent(Long chatId, AppUser appUser) {
        var userState = appUser.getState();

        if (!appUser.getIsActive()) {
            var error = "Register or confirm your email to upload files";
            sendAnswer(error, chatId);
            return true;
        } else if (!BASIC_STATE.equals(userState)) {
            var error = "Cancel the current command with the /cancel command to download files";
            sendAnswer(error, chatId);
            return true;
        } else {
            return false;
        }
    }

    private void sendAnswer(String output, Long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(output);
        producerService.producerAnswer(sendMessage);
    }

}

