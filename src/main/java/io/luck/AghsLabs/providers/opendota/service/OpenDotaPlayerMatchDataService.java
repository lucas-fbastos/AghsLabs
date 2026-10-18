package io.luck.AghsLabs.providers.opendota.service;

import io.luck.AghsLabs.config.CacheConfig;
import io.luck.AghsLabs.exception.ExternalApiException;
import io.luck.AghsLabs.providers.opendota.model.OpenDotaPlayerMatchCandidate;
import io.luck.AghsLabs.providers.opendota.model.OpenDotaPlayerMatch;
import io.luck.AghsLabs.providers.opendota.model.OpenDotaPlayerMatchDetails;
import io.luck.AghsLabs.providers.opendota.model.OpenDotaPlayerRatio;
import org.jspecify.annotations.NonNull;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Service
public class OpenDotaPlayerMatchDataService {

    private static final String[] COMPARISON_PROJECT = {
            "match_id", "account_id", "player_slot", "radiant_win", "duration",
            "game_mode", "lobby_type", "hero_id", "start_time", "version", "patch",
            "kills", "deaths", "assists", "leaver_status", "last_hits", "denies",
            "gold_per_min", "xp_per_min", "hero_damage", "tower_damage",
            "hero_healing", "lane", "lane_role", "item_0", "item_1", "item_2",
            "item_3", "item_4", "item_5"
    };
    private static final String[] FEATURED_WIN_PROJECT = {
            "match_id", "player_slot", "radiant_win", "duration", "game_mode",
            "hero_id", "start_time", "kills", "deaths", "assists"
    };

    private final RestClient restClient;

    public OpenDotaPlayerMatchDataService(RestClient restClient) {
        this.restClient = restClient;
    }

    public List<OpenDotaPlayerMatch> getPlayerRecentMatches(Long playerId){
        return restClient.get()
                .uri("players/{playerId}/recentMatches",playerId)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }

    @Cacheable(cacheNames = CacheConfig.PLAYER_MATCH_CANDIDATES,
            key = "#playerId + ':' + #limit", unless = "#result == null || #result.isEmpty()")
    public List<OpenDotaPlayerMatchCandidate> getPlayerMatches(@NonNull Long playerId, int limit) {
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("players/{playerId}/matches")
                            .queryParam("limit", limit)
                            .queryParam("project", (Object[]) COMPARISON_PROJECT)
                            .build(playerId))
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
        } catch (RestClientException exception) {
            throw new ExternalApiException("Unable to load OpenDota matches for player " + playerId, exception);
        }
    }

    public OpenDotaPlayerMatchCandidate getLatestWonMatch(@NonNull Long playerId) {
        try {
            List<OpenDotaPlayerMatchCandidate> matches = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("players/{playerId}/matches")
                            .queryParam("win", 1)
                            .queryParam("limit", 1)
                            .queryParam("project", (Object[]) FEATURED_WIN_PROJECT)
                            .build(playerId))
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
            return matches == null || matches.isEmpty() ? null : matches.getFirst();
        } catch (RestClientException exception) {
            throw new ExternalApiException("Unable to load a recent win for player " + playerId, exception);
        }
    }

    public OpenDotaPlayerRatio getPlayerRatio(@NonNull Long playerId){
        return restClient.get()
                .uri("players/{playerId}/wl",playerId)
                .retrieve()
                .body(OpenDotaPlayerRatio.class);
    }

    @Cacheable(cacheNames = CacheConfig.MATCH_DETAILS, key = "#matchId",
            unless = "#result == null || #result.players() == null")
    public OpenDotaPlayerMatchDetails getMatchDetails(@NonNull Long matchId){
        try {
            return restClient.get()
                    .uri("matches/{matchId}", matchId)
                    .retrieve()
                    .body(OpenDotaPlayerMatchDetails.class);
        } catch (RestClientException exception) {
            throw new ExternalApiException("Unable to load OpenDota match " + matchId, exception);
        }
    }
}
