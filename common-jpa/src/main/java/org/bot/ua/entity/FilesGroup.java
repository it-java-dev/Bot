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
@Table(name = "files_group")
public class FilesGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String telegramFileId;
    private String fileName;

    @ManyToOne(cascade = CascadeType.REMOVE)
    private GroupsBinaryContent groupsBinaryContent;
    private String mimeType;
    private String fileSizeMb;
    @OneToOne
    private AppUser appUser;
    private String groupId;

}
