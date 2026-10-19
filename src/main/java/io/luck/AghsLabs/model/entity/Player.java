package io.luck.AghsLabs.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Entity
@SuperBuilder
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Table(name="player", schema ="public")
public class Player extends AssignedIdEntity<Long> {

    @Id
    private Long accountId;
    private Integer soloRank;
    private Integer rankTier;
    private Integer competitiveRank;
    private Integer leaderboardRank;
    private Integer mmrEstimate;
    private String personaName;
    private String name;
    private boolean plus;
    private Integer cheese;

    private String steamId;

    private String avatarImage;

    private String avatarImageMedium;

    private String avatarImageFull;

    private String profileURL;

    private String lastLogin;

    private String locCountryCode;

    private Boolean contributor;

    private Boolean subscriber;

    @Override
    @Transient
    @JsonIgnore
    public Long getId() {
        return accountId;
    }
}
