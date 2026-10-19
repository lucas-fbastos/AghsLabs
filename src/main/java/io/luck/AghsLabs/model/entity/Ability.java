package io.luck.AghsLabs.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "ability", schema = "public")
@NoArgsConstructor
public class Ability extends AssignedIdEntity<Long> {

    @Id
    private Long id;

    private String name;

    private String icon;

}
