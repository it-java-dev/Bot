package org.bot.ua.dao;

import org.bot.ua.entity.AppUser;
import org.bot.ua.entity.RawsData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RawsDataDAO extends JpaRepository<RawsData, Long> {
    @Query(value = "SELECT COUNT(*) > 0 FROM raws_data WHERE app_user_id = :appUser AND map ->> :groupId IS NOT NULL", nativeQuery = true)
    boolean existsByAppUserAndMapContainsKey(@Param("appUser") AppUser appUser, @Param("groupId") String groupId);

    @Query(value = "SELECT * FROM raws_data WHERE app_user_id = :appUser AND map ->> :groupId IS NOT NULL", nativeQuery = true)
    RawsData findByAppUserAndMapContainsKey(@Param("appUser") AppUser appUser, @Param("groupId") String groupId);


    /*@Query(value = "SELECT map FROM raws_data WHERE app_user_id = :appUser AND map ->> :groupId IS NOT NULL", nativeQuery = true)
    Map<String, List<Update>> findByAppUserAndMapContainsKey(@Param("appUser") AppUser appUser, @Param("groupId") String groupId);*/


    /*@Query(value = "SELECT map FROM raws_data WHERE app_user_id = :appUser AND map ? :groupId", nativeQuery = true)
    Map<String, List<Update>> findByAppUserAndMapContainsKey(@Param("appUser") AppUser appUser, @Param("groupId") String groupId);*/

    /*Map<String, List<Update>> findByAppUserAndMapContainsKey(AppUser appUser, String groupId);*/


    /*List<RawsData> findAllByAppUser(AppUser appUser);*/

   /* List<RawsData> findAllByMediaGroupId(String mediaGroupId);*/

}
