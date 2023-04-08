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
@Table(name = "groups_binary_content")
public class GroupsBinaryContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private byte[] fileAsArrayOfBytes;

    private String groupId;

    private String fileSize;

    private String fileName;

    @ManyToOne
    @JoinColumn(name = "zip_id")
    private ZipBinaryContent zipBinaryContent;
    @OneToOne
    private AppUser appUser;
}
