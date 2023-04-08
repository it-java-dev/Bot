package org.bot.ua.service.Impl;

import lombok.extern.log4j.Log4j;
import org.bot.ua.dao.UserFileDAO;
import org.bot.ua.entity.UserFile;
import org.bot.ua.service.FileService;
import org.bot.ua.utils.CryptoTool;
import org.springframework.stereotype.Service;


@Log4j
@Service
public class FileServiceImpl implements FileService {

    private final UserFileDAO userFileDAO;

    private final CryptoTool cryptoTool;

    public FileServiceImpl(UserFileDAO userFileDAO, CryptoTool cryptoTool) {
        this.userFileDAO = userFileDAO;
        this.cryptoTool = cryptoTool;
    }

    @Override
    public UserFile getFile(String hash) {
        var id = cryptoTool.idOff(hash);
        if (id == null) {
            return null;
        }
        return userFileDAO.findById(id).orElse(null);
    }
}
