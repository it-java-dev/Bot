package org.bot.ua.service.impl;

import lombok.extern.log4j.Log4j;
import org.bot.ua.exception.UploadFileException;
import org.bot.ua.service.CommonService;
import org.bot.ua.service.enums.LinkType;
import org.bot.ua.utils.CryptoTool;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;


@Log4j
@Service
public class CommonServiceImpl implements CommonService {

    @Value("${token}")
    private String token;
    @Value("${service.file_info.uri}")
    private String fileInfoUri;
    @Value("${service.file_storage.uri}")
    private String fileStorageUri;
    @Value("${link.address}")
    private String linkAddress;
    private final CryptoTool cryptoTool;

    public CommonServiceImpl(CryptoTool cryptoTool) {
        this.cryptoTool = cryptoTool;
    }

    @Override
    public String generateLink(Long docId, LinkType linkType) {
        var hash = cryptoTool.hashOff(docId);
        return "http://" + linkAddress + "/" + linkType + "?id=" + hash;
    }

    /*@Override
    public String regenerateLink(Long docId, LinkType linkType) {
        var hash = cryptoTool.hashOff(docId);
        return "http://" + linkAddress + "/" + linkType + "?id=" + hash;
    }*/


    @Override
    public String generateFileSizeInMb(byte[] fileInBytes) {
        try {
            double fileSizeInMB = (double) fileInBytes.length / (1024 * 1024);
            // two decimal places
            String formattedFileSize = String.format("%.4f", fileSizeInMB).replace(',', '.');
            return formattedFileSize;
        } catch (Exception e) {
            log.error("Error generating String file size in mb: " + e.getMessage());
            return null;
        }
    }
    @Override
    public byte[] downloadFile(String filePath) {
        String fullUri = fileStorageUri.replace("{token}", token)
                .replace("{filePath}", filePath);
        URL urlObj;

        try {
            urlObj = new URL(fullUri);
        } catch (MalformedURLException e) {
            throw new UploadFileException(e);
        }

        try (InputStream is = urlObj.openStream()) {
            return is.readAllBytes();
        } catch (IOException e) {
            e.printStackTrace();
            throw new UploadFileException(urlObj.toExternalForm(), e);
        }
    }

    @Override
    public ResponseEntity<String> getFilePath(String fileId) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> request = new HttpEntity<>(headers);

        return restTemplate.exchange(
                fileInfoUri,
                HttpMethod.GET,
                request,
                String.class,
                token, fileId
        );
    }
    @Override
    public String getFilePath(ResponseEntity<String> response) {
        //convert response to json object
        JSONObject jsonObject = new JSONObject(response.getBody());
        //gets file_path

        return String.valueOf(jsonObject
                .getJSONObject("result")
                .getString("file_path"));
    }

    @Override
    public String generateUniqueFileName(String filePath) {
        try {
            // get the file name with extension
            String fileName = filePath.substring(filePath.lastIndexOf('/') + 1);
            // generate a unique id without dashes
            String uniqueId = UUID.randomUUID().toString().replaceAll("-", "");
            // insert the unique id before the extension
            String newFileName = fileName.replace(".", "_" + uniqueId + ".");
            return newFileName;
        } catch (Exception e) {
            // handle any exceptions here
            log.error("Error generating unique file name: " + e.getMessage());
            return null;
        }
    }
}
