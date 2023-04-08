package org.bot.ua.dao;

import org.bot.ua.entity.RawData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RawDataDAO extends JpaRepository<RawData, Long> {
    /*@Query(value = "SELECT * FROM raw_data WHERE event->>'mediaGroupId' = :groupId", nativeQuery = true)
    List<RawData> findAllByMediaGroupId(@Param("groupId") String groupId);*/
}
