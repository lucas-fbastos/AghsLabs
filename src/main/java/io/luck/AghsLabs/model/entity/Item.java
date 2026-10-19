package io.luck.AghsLabs.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import java.util.List;


@AllArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name="item",schema = "public")
@NoArgsConstructor
public class Item extends AssignedIdEntity<Long> {
    @Id
    @Column(name="item_id")
    private Long itemId;
    private String name;
    private Integer cost;
    private String image;
    private Integer cd;
    private Integer charges;
    private boolean created;
    @OneToMany(mappedBy="item")
    @Fetch(FetchMode.SUBSELECT)
    private List<ItemAttribute> attributes;
    private String quality;
    @Column(columnDefinition="TEXT")
    private String notes;
    @Column(columnDefinition="TEXT")
    private String lore;

    @Override
    @Transient
    @JsonIgnore
    public Long getId() {
        return itemId;
    }
}
