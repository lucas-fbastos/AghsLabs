package io.luck.AghsLabs.model;

import io.luck.AghsLabs.model.Enums.GameMode;
import io.luck.AghsLabs.model.MatchDetailsData.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.Duration;
import java.util.Set;
import java.util.List;
import java.math.BigInteger;
import tools.jackson.databind.JsonNode;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Match {
    private Integer version;
    private Long matchId;
    private Long averageMmr;
    private List<Teamfight> teamfights;
    private List<Pause> pauses;
    private List<Objective> objectives;
    private List<Long> radiantGoldAdv;
    private List<Long> radiantXpAdv;
    private List<DraftTiming> draftTimings;
    private List<MatchPlayer> players;
    private Long leagueId;
    private Long startTime;
    private Duration duration;
    private Long seriesId;
    private Integer seriesType;
    private Integer cluster;
    private Long replaySalt;
    private Boolean radiantWin;
    private Integer preGameDuration;
    private Long matchSeqNum;
    private Integer towerStatusRadiant;
    private Integer towerStatusDire;
    private Integer barracksStatusRadiant;
    private Integer barracksStatusDire;
    private Integer firstBloodTime;
    private Integer lobbyType;
    private Integer humanPlayers;
    private GameMode gameMode;
    private Integer flags;
    private Integer engine;
    private Integer radiantScore;
    private Integer direScore;
    private Long radiantTeamId;
    private String radiantName;
    private BigInteger radiantLogo;
    private Integer radiantTeamComplete;
    private Long direTeamId;
    private String direName;
    private BigInteger direLogo;
    private Integer direTeamComplete;
    private Long radiantCaptain;
    private Long direCaptain;
    private List<PickBan> picksBans;
    private OdData odData;
    private League league;
    private TeamInfo radiantTeamInfo;
    private TeamInfo direTeamInfo;
    private JsonNode metadata;
    private String replayUrl;
    private Integer patch;
    private Integer region;
    private Integer throwAmount;
    private Integer loss;
    private Set<MatchPlayer> radiantTeam;
    private Set<MatchPlayer> direTeam;
}
