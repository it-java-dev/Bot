package org.bot.ua.dao;

import org.bot.ua.entity.AppUser;
import org.bot.ua.entity.FilesGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FilesGroupDAO extends JpaRepository<FilesGroup, Long> {

    List<FilesGroup> findAllByAppUser(AppUser appUser);

}
