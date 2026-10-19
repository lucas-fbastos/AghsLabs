package io.luck.AghsLabs.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;


@Setter
@Getter
@Entity
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@Table(name="item_attribute",schema = "public")
@NoArgsConstructor
public class ItemAttribute {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long Id;
    private String key;
    private String header;
    private String value;
    private String footer;
    @ManyToOne
    @JoinColumn(name="itemId", nullable=false)
    @JsonIgnore
    private Item item;
}
