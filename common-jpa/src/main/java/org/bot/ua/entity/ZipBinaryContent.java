package org.bot.ua.entity;

import lombok.*;

import javax.persistence.*;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode(exclude = "id")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "zip_binary_content")
public class ZipBinaryContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private byte[] fileAsArrayOfBytes;

    private String fileSize;

    private String fileName;

    @OneToMany(mappedBy = "zipBinaryContent", cascade = CascadeType.ALL)
    private List<GroupsBinaryContent> groupId;
    @OneToOne
    private AppUser appUser;

    private String mimeType;
}
