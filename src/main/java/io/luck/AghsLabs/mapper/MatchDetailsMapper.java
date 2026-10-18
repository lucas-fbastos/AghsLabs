package io.luck.AghsLabs.mapper;

import io.luck.AghsLabs.model.Enums.Faction;
import io.luck.AghsLabs.model.Enums.GameMode;
import io.luck.AghsLabs.model.Match;
import io.luck.AghsLabs.model.MatchChat;
import io.luck.AghsLabs.model.MatchWards;
import io.luck.AghsLabs.model.MatchPlayer;
import io.luck.AghsLabs.model.entity.Hero;
import io.luck.AghsLabs.model.entity.Item;
import io.luck.AghsLabs.providers.opendota.model.OpenDotaPlayerMatchDetails;
import org.springframework.stereotype.Component;
import java.time.Duration;
import java.util.*;

@Component
public class MatchDetailsMapper {
    private final MatchPlayerMapper matchPlayerMapper;

    public MatchDetailsMapper(MatchPlayerMapper matchPlayerMapper) {
        this.matchPlayerMapper = matchPlayerMapper;
    }

    public Match convert(OpenDotaPlayerMatchDetails source, Map<Long, Hero> heroesById, Map<Long, Item> itemsById) {
        Set<MatchPlayer> players = matchPlayerMapper.convert(source.players(), heroesById, itemsById);
        Set<MatchPlayer> radiant = new LinkedHashSet<>();
        Set<MatchPlayer> dire = new LinkedHashSet<>();

        boolean invalidMmr = players.stream()
                .anyMatch(p -> p.computedMmr() == null || p.computedMmr() == 0);

        long averageMmr = 0;
        if(!invalidMmr){
            averageMmr = players.stream()
                    .mapToInt(MatchPlayer::computedMmr)
                    .sum()
                    / players.size();
        }

        for (var player : players) {
            if (Faction.RADIANT.name().equals(player.team()))
                radiant.add(player);
            else
                dire.add(player);
        }
        averageMmr = averageMmr/players.size();

       return Match.builder()
                .version(source.version())
                .matchId(source.matchId())
                .teamfights(MatchDetailsDataMapper.mapList(source.teamfights(), MatchDetailsDataMapper::convert))
                .pauses(MatchDetailsDataMapper.mapList(source.pauses(), MatchDetailsDataMapper::convert))
                .objectives(MatchDetailsDataMapper.mapList(source.objectives(), MatchDetailsDataMapper::convert))
                .radiantGoldAdv(source.radiantGoldAdv())
                .radiantXpAdv(source.radiantXpAdv())
                .draftTimings(MatchDetailsDataMapper.mapList(source.draftTimings(), MatchDetailsDataMapper::convert))
                .leagueId(source.leagueId())
                .startTime(source.startTime())
                .duration(source.duration() == null ? null : Duration.ofSeconds(source.duration()))
                .seriesId(source.seriesId())
                .seriesType(source.seriesType())
                .cluster(source.cluster())
                .replaySalt(source.replaySalt())
                .radiantWin(source.radiantWin())
                .preGameDuration(source.preGameDuration())
                .matchSeqNum(source.matchSeqNum())
                .towerStatusRadiant(source.towerStatusRadiant())
                .towerStatusDire(source.towerStatusDire())
                .barracksStatusRadiant(source.barracksStatusRadiant())
                .barracksStatusDire(source.barracksStatusDire())
                .firstBloodTime(source.firstBloodTime())
                .lobbyType(source.lobbyType())
                .humanPlayers(source.humanPlayers())
                .gameMode(source.gameMode() == null ? null : GameMode.getById(source.gameMode()).orElse(null))
                .flags(source.flags())
                .engine(source.engine())
                .radiantScore(source.radiantScore())
                .direScore(source.direScore())
                .radiantTeamId(source.radiantTeamId())
                .radiantName(source.radiantName())
                .radiantLogo(source.radiantLogo())
                .radiantTeamComplete(source.radiantTeamComplete())
                .direTeamId(source.direTeamId())
                .direName(source.direName())
                .direLogo(source.direLogo())
                .direTeamComplete(source.direTeamComplete())
                .radiantCaptain(source.radiantCaptain())
                .direCaptain(source.direCaptain())
                .picksBans(MatchDetailsDataMapper.mapList(source.picksBans(), MatchDetailsDataMapper::convert))
                .odData(MatchDetailsDataMapper.convert(source.odData()))
                .league(MatchDetailsDataMapper.convert(source.league()))
                .radiantTeamInfo(MatchDetailsDataMapper.convert(source.radiantTeam()))
                .direTeamInfo(MatchDetailsDataMapper.convert(source.direTeam()))
                .metadata(source.metadata())
                .replayUrl(source.replayUrl())
                .patch(source.patch())
                .region(source.region())
                .throwAmount(source.throwAmount())
                .loss(source.loss())
                .players(new ArrayList<>(players))
                .averageMmr(averageMmr)
                .radiantTeam(radiant)
                .direTeam(dire)
                .build();
    }

    public MatchChat convertChat(OpenDotaPlayerMatchDetails source) {
        Map<Integer, Long> accountIdsByPlayerSlot = new HashMap<>();
        if (source.players() != null) {
            for (var player : source.players()) {
                if (player.playerSlot() != null) {
                    accountIdsByPlayerSlot.put(player.playerSlot(), player.accountId());
                }
            }
        }
        return new MatchChat(
                MatchDetailsDataMapper.mapList(source.chat(), entry -> MatchDetailsDataMapper.convert(
                        entry, entry == null ? null : accountIdsByPlayerSlot.get(entry.playerSlot()))),
                source.allWordCounts(),
                source.myWordCounts()
        );
    }

    public MatchWards convertWards(OpenDotaPlayerMatchDetails source) {
        if (source.players() == null) return new MatchWards(List.of());
        return new MatchWards(
                MatchDetailsDataMapper.mapList(
                    source.players(), player ->
                        new MatchWards.PlayerWards(
                                player.playerSlot(),
                                player.accountId(),
                                player.heroId(),
                                MatchDetailsDataMapper.mapList(player.obsLog(), MatchDetailsDataMapper::convert),
                                MatchDetailsDataMapper.mapList(player.senLog(), MatchDetailsDataMapper::convert),
                                MatchDetailsDataMapper.mapList(player.obsLeftLog(), MatchDetailsDataMapper::convert),
                                MatchDetailsDataMapper.mapList(player.senLeftLog(), MatchDetailsDataMapper::convert)
                        )
                )
        );
    }
}
