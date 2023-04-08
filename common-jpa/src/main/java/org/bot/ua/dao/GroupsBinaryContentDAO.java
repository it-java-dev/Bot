package org.bot.ua.dao;

import org.bot.ua.entity.GroupsBinaryContent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupsBinaryContentDAO extends JpaRepository<GroupsBinaryContent,Long> {
    List<GroupsBinaryContent> findAllIdByGroupId (String groupId);
}
