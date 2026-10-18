package io.luck.AghsLabs.service;

import io.luck.AghsLabs.config.CacheConfig;
import io.luck.AghsLabs.exception.ExternalApiException;
import io.luck.AghsLabs.model.Enums.ProPlayerRole;
import io.luck.AghsLabs.model.dtos.FeaturedProPlayersResponse;
import io.luck.AghsLabs.model.entity.Hero;
import io.luck.AghsLabs.model.entity.ProPlayer;
import io.luck.AghsLabs.providers.opendota.model.OpenDotaPlayerMatchCandidate;
import io.luck.AghsLabs.providers.opendota.service.OpenDotaPlayerMatchDataService;
import io.luck.AghsLabs.repository.ProPlayerRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class FeaturedProPlayerService {

    private static final int CANDIDATE_LIMIT = 12;
    private static final Logger LOGGER = Logger.getLogger(FeaturedProPlayerService.class.getName());

    private final ProPlayerRepository proPlayerRepository;
    private final OpenDotaPlayerMatchDataService playerMatchDataService;
    private final HeroService heroService;

    public FeaturedProPlayerService(
            ProPlayerRepository proPlayerRepository,
            OpenDotaPlayerMatchDataService playerMatchDataService,
            HeroService heroService
    ) {
        this.proPlayerRepository = proPlayerRepository;
        this.playerMatchDataService = playerMatchDataService;
        this.heroService = heroService;
    }

    @Cacheable(
            cacheNames = CacheConfig.FEATURED_PRO_PLAYERS,
            key = "'landing'",
            unless = "#result == null || #result.roles().isEmpty()"
    )
    public FeaturedProPlayersResponse getFeaturedPlayers() {
        Map<ProPlayerRole, Selection> selections = selectPlayers();
        Set<Long> heroIds = selections.values().stream()
                .map(selection -> selection.match().heroId())
                .collect(Collectors.toSet());
        Map<Long, Hero> heroesById = heroService.getAllById(heroIds).stream()
                .collect(Collectors.toMap(Hero::getId, Function.identity()));

        List<FeaturedProPlayersResponse.FeaturedRole> roles = new ArrayList<>();
        for (ProPlayerRole role : ProPlayerRole.values()) {
            Selection selection = selections.get(role);
            if (selection == null) continue;
            Hero hero = heroesById.get(selection.match().heroId());
            if (hero == null) continue;
            roles.add(toFeaturedRole(role, selection, hero));
        }

        return new FeaturedProPlayersResponse(Instant.now(), List.copyOf(roles));
    }

    private Map<ProPlayerRole, Selection> selectPlayers() {
        Map<ProPlayerRole, Selection> selections = new LinkedHashMap<>();
        Set<Long> selectedAccountIds = new HashSet<>();
        PageRequest candidatePage = PageRequest.of(0, CANDIDATE_LIMIT);

        for (ProPlayerRole role : ProPlayerRole.values()) {
            List<ProPlayer> candidates = proPlayerRepository
                    .findByPlayerRoleOrderByLastLoginDescAccountIdAsc(role.getValue(), candidatePage);
            for (ProPlayer player : candidates) {
                if (player.getAccountId() == null || selectedAccountIds.contains(player.getAccountId())) continue;
                OpenDotaPlayerMatchCandidate match;
                try {
                    match = playerMatchDataService.getLatestWonMatch(player.getAccountId());
                } catch (ExternalApiException exception) {
                    LOGGER.log(Level.WARNING,
                            "Skipping featured pro player " + player.getAccountId() + " for role " + role,
                            exception);
                    continue;
                }
                if (!isCompleteWin(match)) continue;
                selections.put(role, new Selection(player, match));
                selectedAccountIds.add(player.getAccountId());
                break;
            }
        }
        return selections;
    }

    private boolean isCompleteWin(OpenDotaPlayerMatchCandidate match) {
        return match != null
                && match.matchId() != null
                && match.heroId() != null
                && match.startTime() != null
                && Boolean.TRUE.equals(match.playerWon());
    }

    private FeaturedProPlayersResponse.FeaturedRole toFeaturedRole(
            ProPlayerRole role,
            Selection selection,
            Hero hero
    ) {
        ProPlayer player = selection.player();
        OpenDotaPlayerMatchCandidate match = selection.match();
        var featuredHero = new FeaturedProPlayersResponse.FeaturedHero(
                hero.getId(), hero.getLocalizedName(), hero.getImage()
        );
        var featuredMatch = new FeaturedProPlayersResponse.FeaturedMatch(
                match.matchId(),
                Instant.ofEpochSecond(match.startTime()),
                match.duration() == null ? null : Duration.ofSeconds(match.duration()),
                match.kills(),
                match.deaths(),
                match.assists(),
                featuredHero
        );
        var featuredPlayer = new FeaturedProPlayersResponse.FeaturedPlayer(
                player.getAccountId(),
                player.getPersonaName(),
                player.getAvatarImageFull(),
                player.getTeamName(),
                player.getTeamTag(),
                featuredMatch
        );
        return new FeaturedProPlayersResponse.FeaturedRole(
                role.getValue(), role.getLabel(), featuredPlayer
        );
    }

    private record Selection(ProPlayer player, OpenDotaPlayerMatchCandidate match) {}
}
