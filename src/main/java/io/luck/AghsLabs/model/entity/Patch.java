package io.luck.AghsLabs.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@Builder
@Entity
@Table(name="patch",schema = "public")
@NoArgsConstructor
public class Patch extends AssignedIdEntity<Integer> {
    @Id
    private Integer id;
    private Instant date;
    private String name;

}
