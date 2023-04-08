package org.bot.ua.service.impl;

import lombok.extern.log4j.Log4j;
import org.bot.ua.dao.AppUserDAO;
import org.bot.ua.dto.MailParams;
import org.bot.ua.entity.AppUser;
import org.bot.ua.service.UserService;
import org.bot.ua.utils.CryptoTool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import static org.bot.ua.entity.enums.UserState.BASIC_STATE;
import static org.bot.ua.entity.enums.UserState.WAIT_FOE_EMAIL_STATE;

@Service
@Log4j
public class UserServiceImpl implements UserService {
    private final AppUserDAO appUserDAO;
    private final CryptoTool cryptoTool;
    @Value("${service.mail.uri}")
    private String mailServiceUri;

    public UserServiceImpl(AppUserDAO appUserDAO, CryptoTool cryptoTool) {
        this.appUserDAO = appUserDAO;
        this.cryptoTool = cryptoTool;
    }

    @Override
    public String registerUser(AppUser appUser) {
        if (appUser.getIsActive()) {
            return "You are already registered";
        } else if (appUser.getEmail() != null) {
            return "An email has already been sent to you," +
                    " please follow the link in the email to confirm your registration";

        }
        appUser.setState(WAIT_FOE_EMAIL_STATE);
        appUserDAO.save(appUser);
        return "Enter please, your email:";
    }

    @Override
    public String setEmail(AppUser appUser, String email) {
        try {
            InternetAddress emailAddress = new InternetAddress(email);
            emailAddress.validate();
        } catch (AddressException e) {
            return "Enter please, correct email. For cancel command enter /cancel";
        }
        var optional = appUserDAO.findByEmail(email);
        if (optional.isEmpty()) {
            appUser.setEmail(email);
            appUser.setState(BASIC_STATE);
            appUser = appUserDAO.save(appUser);

            var cryptoUserId = cryptoTool.hashOff(appUser.getId());
            var response = sendRequestToMailService(cryptoUserId, email);
            if (response.getStatusCode() != HttpStatus.OK) {
                var msg = String.format("Sending latter on email %s, failed", email);
                log.error(msg);
                appUser.setEmail(null);
                appUserDAO.save(appUser);
                return msg;
            }
            return "An email has been sent to you, " +
                    "please follow the link in the email to confirm your registration.";
        } else {
            return "This email address is already in use, " +
                    "please enter a valid email address. " +
                    "For cancel command enter /cancel";
        }
    }



    private ResponseEntity<String> sendRequestToMailService(String cryptoUserId, String email) {
        var restTemplate = new RestTemplate();
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var mailParams = MailParams.builder()
                .id(cryptoUserId)
                .emailTo(email)
                .build();
        var request = new HttpEntity<>(mailParams, headers);
        return restTemplate.exchange(mailServiceUri,
                HttpMethod.POST,
                request,
                String.class);
    }
}
