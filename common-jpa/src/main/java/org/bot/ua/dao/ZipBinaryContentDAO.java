package org.bot.ua.dao;

import org.bot.ua.entity.AppUser;
import org.bot.ua.entity.ZipBinaryContent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ZipBinaryContentDAO extends JpaRepository<ZipBinaryContent,Long> {
    List<ZipBinaryContent> findAllByAppUser(AppUser appUser);
}
