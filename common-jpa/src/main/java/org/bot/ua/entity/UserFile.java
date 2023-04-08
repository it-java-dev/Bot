package org.bot.ua.entity;


import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@EqualsAndHashCode(exclude = "id")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_file")
public class UserFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String telegramFileId;
    private String fileName;
    @ManyToOne(cascade = CascadeType.REMOVE)
    private BinaryContent binaryContent;
    private String mimeType;
    private String fileSizeMb;
    @OneToOne
    private AppUser appUser;

}
