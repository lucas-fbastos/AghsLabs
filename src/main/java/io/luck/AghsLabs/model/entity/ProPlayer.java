package io.luck.AghsLabs.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table( schema ="public",name = "pro_player")
public class ProPlayer extends Player{
    private String teamName;
    private String teamTag;
    private Integer playerRole;
    private Long teamId;
    private Boolean isLocked;
    private Long lockedUntil;

    public static Integer MINIMUM_MMR = 9000;
}
