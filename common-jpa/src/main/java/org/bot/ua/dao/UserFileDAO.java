package org.bot.ua.dao;

import org.bot.ua.entity.AppUser;
import org.bot.ua.entity.UserFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserFileDAO extends JpaRepository<UserFile, Long> {
    List<UserFile> findAllByAppUser(AppUser appUser);
}
