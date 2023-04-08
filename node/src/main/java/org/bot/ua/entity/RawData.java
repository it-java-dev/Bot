package org.bot.ua.entity;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.*;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.persistence.*;

@Getter
@Setter
@EqualsAndHashCode(exclude = "id")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "raw_data")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class RawData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*Optimized binary format spaces removed, object sorting not preserved, duplicate keys not preserved, but in db saved as json format*/
    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private Update event;


    @Override
    public String toString() {
        return "RawData{" +
                "id=" + id +
                ", event=" + event +
                '}';
    }
}
