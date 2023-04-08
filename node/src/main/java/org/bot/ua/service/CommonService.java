package org.bot.ua.service;

import org.bot.ua.service.enums.LinkType;
import org.springframework.http.ResponseEntity;

public interface CommonService {
    String generateLink(Long docId, LinkType linkType);
    String generateFileSizeInMb(byte[] fileInBytes);
    byte[] downloadFile(String filePath);
    ResponseEntity<String> getFilePath(String fileId);
    String getFilePath(ResponseEntity<String> response);

    String generateUniqueFileName(String filePath);
    /*String regenerateLink(Long docId, LinkType linkType);*/

}
