package org.bot.ua.controller;

import lombok.extern.log4j.Log4j;
import org.bot.ua.service.ZipGroupService;
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
public class ZipController {
    private final ZipGroupService zipGroupService;

    public ZipController(ZipGroupService zipGroupService) {
        this.zipGroupService = zipGroupService;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/files")
    public void getFiles(@RequestParam("id") String id, HttpServletResponse response) {
        var zip = zipGroupService.getZip(id);
        if (zip == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        response.setContentType(MediaType.parseMediaType(zip.getMimeType()).toString());
        response.setHeader("Content-disposition", "attachment; filename=" + zip.getFileName());
        response.setStatus(HttpServletResponse.SC_OK);
        try {
            var out = response.getOutputStream();
            out.write(zip.getFileAsArrayOfBytes());
            out.close();
        } catch (IOException e) {
            log.error(e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}


