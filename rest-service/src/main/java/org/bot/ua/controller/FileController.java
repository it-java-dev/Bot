package org.bot.ua.controller;

import lombok.extern.log4j.Log4j;
import org.bot.ua.service.FileService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

// common path
// response raw_data as arrays of byte.(json/xml)
@Log4j
@RequestMapping("/type")
@RestController
public class FileController {
    private final FileService fileService;


    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/file")
    public void getFile(@RequestParam("id") String id, HttpServletResponse response) {
        var file = fileService.getFile(id);
        if (file == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        response.setContentType(MediaType.parseMediaType(file.getMimeType()).toString());
        response.setHeader("Content-disposition", "attachment; filename=" + file.getFileName());
        response.setStatus(HttpServletResponse.SC_OK);
        var binaryContent = file.getBinaryContent();
        try {
            var out = response.getOutputStream();
            out.write(binaryContent.getFileAsArrayOfBytes());
            out.close();
        } catch (IOException e) {
            log.error(e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}


