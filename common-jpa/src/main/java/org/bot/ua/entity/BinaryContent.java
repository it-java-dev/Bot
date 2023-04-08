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
@Table(name = "binary_content")
public class BinaryContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private byte[] fileAsArrayOfBytes;

    private String fileSize;

    private String fileName;

    @OneToOne
    private AppUser appUser;
    /*@OneToOne
    private GroupsBinaryContent groupsBinaryContent;*/
}
