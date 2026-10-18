package io.luck.AghsLabs.service;

import io.luck.AghsLabs.model.*;
import io.luck.AghsLabs.mapper.LiveMatchMapper;
import io.luck.AghsLabs.providers.opendota.model.OpenDotaLiveMatch;
import io.luck.AghsLabs.repository.HeroRepository;
import io.luck.AghsLabs.model.entity.Hero;
import io.luck.AghsLabs.model.entity.Item;
import io.luck.AghsLabs.mapper.MatchDetailsMapper;
import io.luck.AghsLabs.providers.opendota.model.OpenDotaMatchPlayer;
import io.luck.AghsLabs.providers.opendota.model.OpenDotaPlayerMatch;
import io.luck.AghsLabs.providers.opendota.model.OpenDotaPlayerRatio;
import io.luck.AghsLabs.providers.opendota.service.OpenDotaLiveDataService;
import io.luck.AghsLabs.providers.opendota.service.OpenDotaPlayerMatchDataService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class MatchService {

    private final HeroService heroService;
    private final HeroRepository heroRepository;
    private final ItemService itemService;
    private final MatchDetailsMapper matchDetailsMapper;
    private final OpenDotaPlayerMatchDataService openDotaPlayerMatchDataService;
    private final OpenDotaLiveDataService openDotaLiveDataService;

    public MatchService(HeroService heroService,
                        HeroRepository heroRepository,
                        ItemService itemService,
                        MatchDetailsMapper matchDetailsMapper,
                        OpenDotaPlayerMatchDataService openDotaPlayerMatchDataService,
                        OpenDotaLiveDataService openDotaLiveDataService) {
        this.heroService = heroService;
        this.heroRepository = heroRepository;
        this.itemService = itemService;
        this.matchDetailsMapper = matchDetailsMapper;
        this.openDotaLiveDataService = openDotaLiveDataService;
        this.openDotaPlayerMatchDataService = openDotaPlayerMatchDataService;
    }

    public List<MatchSummary> getRecentMatches(Long playerId) {

        var recentMatches = openDotaPlayerMatchDataService.getPlayerRecentMatches(playerId);

        if (recentMatches == null || recentMatches.isEmpty())
            return Collections.emptyList();

        var heroesIds = recentMatches.stream()
                .map(OpenDotaPlayerMatch::heroId)
                .collect(Collectors.toSet());

        List<Hero> heroes = heroService.getAllById(heroesIds);
        Map<Long, Hero> heroMap = heroes.stream()
                .collect(Collectors.toMap(
                        Hero::getId,
                        Function.identity()
                ));

        return recentMatches.stream()
                .map(match -> MatchSummary.from(
                        match,
                        heroMap.get(match.heroId())
                ))
                .toList();
    }

    public OpenDotaPlayerRatio getWinLose(Long playerId) {
        var openDotaPlayerRatio = openDotaPlayerMatchDataService.getPlayerRatio(playerId);
        if(openDotaPlayerRatio == null)
            return OpenDotaPlayerRatio.DEFAULT;
        return openDotaPlayerRatio;
    }

    public Match getMatchDetails(Long matchId) {
        var openDotaMatchDetails = openDotaPlayerMatchDataService.getMatchDetails(matchId);
        var players = openDotaMatchDetails.players();

        var heroIds = players.stream()
                .map(OpenDotaMatchPlayer::heroId)
                .collect(Collectors.toSet());
        Map<Long, Hero> heroesById = heroService.getAllById(heroIds).stream()
                .collect(Collectors.toMap(Hero::getId, Function.identity()));

        var itemIds = players.stream()
                .flatMap(player -> player.getItems().stream())
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toSet());

        Map<Long, Item> itemsById = itemService.getAllById(itemIds).stream()
                .collect(Collectors.toMap(Item::getItemId, Function.identity()));

        return matchDetailsMapper.convert(openDotaMatchDetails, heroesById, itemsById);
    }

    public MatchChat getMatchChat(Long matchId) {
        var details = openDotaPlayerMatchDataService.getMatchDetails(matchId);
        return details == null ? null : matchDetailsMapper.convertChat(details);
    }

    public MatchWards getMatchWards(Long matchId) {
        var details = openDotaPlayerMatchDataService.getMatchDetails(matchId);
        return details == null ? null : matchDetailsMapper.convertWards(details);
    }

    public Set<LiveMatch> getLiveMatches(Integer limit) {
        var matches = openDotaLiveDataService.getLiveMatches();
        if (matches == null || matches.isEmpty()) return Collections.emptySet();

        var limitedMatches = limit == null ? matches : matches.stream().limit(limit).toList();
        var heroIds = limitedMatches.stream()
                .filter(match -> match.players() != null)
                .flatMap(match -> match.players().stream())
                .map(OpenDotaLiveMatch.OpenDotaLivePlayer::heroId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        var matchHeroes = new HashSet<>(heroRepository.findAllById(heroIds));
        return LiveMatchMapper.convert(limitedMatches, matchHeroes);
    }
}
