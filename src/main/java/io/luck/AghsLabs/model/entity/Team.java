package io.luck.AghsLabs.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table( schema ="public",name = "team")
public class Team extends AssignedIdEntity<Long> {
    @Id
    private Long teamId;
    private String name;
    private String tag;
    private String logoUrl;

    @Override
    @Transient
    @JsonIgnore
    public Long getId() {
        return teamId;
    }
}
