package org.bot.ua.service;

import org.bot.ua.entity.AppUser;

public interface UserService {
    String registerUser(AppUser appUser);
    String setEmail(AppUser appUser, String email);
}
