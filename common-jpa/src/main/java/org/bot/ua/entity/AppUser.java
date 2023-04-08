package org.bot.ua.entity;

import lombok.*;
import org.bot.ua.entity.enums.UserState;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode(exclude = "id")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "app_user")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long telegramUserId;
    private LocalDateTime firstLoginData;
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private Boolean isActive;
    private Boolean isAdmin;

    // saving in bd as varchar 255(String)
    @Enumerated(EnumType.STRING)
    private UserState state;
}
