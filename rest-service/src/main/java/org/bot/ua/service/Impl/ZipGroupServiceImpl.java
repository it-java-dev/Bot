package org.bot.ua.service.Impl;

import lombok.extern.log4j.Log4j;
import org.bot.ua.dao.ZipBinaryContentDAO;
import org.bot.ua.entity.ZipBinaryContent;
import org.bot.ua.service.ZipGroupService;
import org.bot.ua.utils.CryptoTool;
import org.springframework.stereotype.Service;


@Log4j
@Service
public class ZipGroupServiceImpl implements ZipGroupService {
    private final ZipBinaryContentDAO zipBinaryContentDAO;

    private final CryptoTool cryptoTool;

    public ZipGroupServiceImpl(ZipBinaryContentDAO zipBinaryContentDAO, CryptoTool cryptoTool) {
        this.zipBinaryContentDAO = zipBinaryContentDAO;
        this.cryptoTool = cryptoTool;
    }


    @Override
    public ZipBinaryContent getZip(String id) {
            var ids = cryptoTool.idOff(id);
            if (ids == null) {
                return null;
            }
            return zipBinaryContentDAO.findById(ids).orElse(null);
        }
    }
