package org.bot.ua.entity;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.*;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.persistence.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@EqualsAndHashCode(exclude = "id")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "raws_data")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class RawsData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "app_user_id", nullable = false)
    private AppUser appUser;

    /*Optimized binary format spaces removed, object sorting not preserved, duplicate keys not preserved, but in db saved as json format*/
   /* @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private Update update;

    @Column(name = "media_group_id")
    private String mediaGroupId;*/

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private Map<String, List<Update>> map = new HashMap<>();
}
