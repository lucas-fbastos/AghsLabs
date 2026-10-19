package io.luck.AghsLabs.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
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
@Table(name = "hero", schema ="public")
@NoArgsConstructor
public class Hero extends AssignedIdEntity<Long> {

    @Id
    private Long id;
    private String name;

    private String localizedName;

    private String primaryAttribute;

    private String attackType;

    private String image;

    private String icon;

    @ManyToMany
    @Fetch(FetchMode.SUBSELECT)
    @JoinTable(schema = "public")
    private List<Role> roles;

}
