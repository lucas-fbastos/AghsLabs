package io.luck.AghsLabs.service;

import io.luck.AghsLabs.config.CacheConfig;
import io.luck.AghsLabs.exception.ExternalApiException;
import io.luck.AghsLabs.model.dtos.FeaturedProPlayersResponse;
import io.luck.AghsLabs.model.entity.Hero;
import io.luck.AghsLabs.model.entity.ProPlayer;
import io.luck.AghsLabs.providers.opendota.model.OpenDotaPlayerMatchCandidate;
import io.luck.AghsLabs.providers.opendota.service.OpenDotaPlayerMatchDataService;
import io.luck.AghsLabs.repository.ProPlayerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FeaturedProPlayerServiceTest {

    @Test
    void returnsOnePlayerPerRoleInDisplayOrderAndBulkResolvesHeroes() {
        var repository = mock(ProPlayerRepository.class);
        var matchDataService = mock(OpenDotaPlayerMatchDataService.class);
        var heroService = mock(HeroService.class);
        Map<Integer, ProPlayer> playersByRole = Map.of(
                1, player(101L, 1),
                4, player(104L, 4),
                3, player(103L, 3),
                2, player(102L, 2)
        );
        Map<Long, Long> heroesByAccount = Map.of(
                101L, 11L,
                104L, 14L,
                103L, 13L,
                102L, 12L
        );
        for (var entry : playersByRole.entrySet()) {
            when(repository.findByPlayerRoleOrderByLastLoginDescAccountIdAsc(
                    org.mockito.ArgumentMatchers.eq(entry.getKey()), any(Pageable.class)
            )).thenReturn(List.of(entry.getValue()));
            when(matchDataService.getLatestWonMatch(entry.getValue().getAccountId()))
                    .thenReturn(wonMatch(1000L + entry.getKey(), heroesByAccount.get(entry.getValue().getAccountId())));
        }
        when(heroService.getAllById(Set.of(11L, 12L, 13L, 14L))).thenReturn(List.of(
                hero(11L), hero(12L), hero(13L), hero(14L)
        ));
        var service = new FeaturedProPlayerService(repository, matchDataService, heroService);

        FeaturedProPlayersResponse result = service.getFeaturedPlayers();

        assertNotNull(result.generatedAt());
        assertEquals(List.of(1, 4, 3, 2), result.roles().stream()
                .map(FeaturedProPlayersResponse.FeaturedRole::playerRole)
                .toList());
        assertEquals(4, result.roles().size());
        assertEquals(101L, result.roles().getFirst().player().accountId());
        assertEquals(11L, result.roles().getFirst().player().recentWin().hero().id());
        verify(heroService).getAllById(Set.of(11L, 12L, 13L, 14L));
    }

    @Test
    void skipsUnavailableCandidateAndBackfillsFromTheBoundedPool() {
        var repository = mock(ProPlayerRepository.class);
        var matchDataService = mock(OpenDotaPlayerMatchDataService.class);
        var heroService = mock(HeroService.class);
        ProPlayer unavailable = player(1L, 1);
        ProPlayer selected = player(2L, 1);
        when(repository.findByPlayerRoleOrderByLastLoginDescAccountIdAsc(
                org.mockito.ArgumentMatchers.eq(1), any(Pageable.class)
        )).thenReturn(List.of(unavailable, selected));
        when(matchDataService.getLatestWonMatch(1L))
                .thenThrow(new ExternalApiException("private profile"));
        when(matchDataService.getLatestWonMatch(2L)).thenReturn(wonMatch(99L, 7L));
        when(heroService.getAllById(Set.of(7L))).thenReturn(List.of(hero(7L)));
        var service = new FeaturedProPlayerService(repository, matchDataService, heroService);

        FeaturedProPlayersResponse result = service.getFeaturedPlayers();

        assertEquals(1, result.roles().size());
        assertEquals(2L, result.roles().getFirst().player().accountId());
    }

    @Test
    void featureResponseDeclaresDedicatedWholePayloadCache() throws Exception {
        Cacheable annotation = FeaturedProPlayerService.class
                .getMethod("getFeaturedPlayers")
                .getAnnotation(Cacheable.class);

        assertNotNull(annotation);
        assertEquals(CacheConfig.FEATURED_PRO_PLAYERS, annotation.cacheNames()[0]);
        assertEquals("'landing'", annotation.key());
    }

    @Test
    void repeatedCallsReuseTheWholeResponseThroughSpringCaching() {
        var repository = mock(ProPlayerRepository.class);
        var matchDataService = mock(OpenDotaPlayerMatchDataService.class);
        var heroService = mock(HeroService.class);
        ProPlayer selected = player(101L, 1);
        when(repository.findByPlayerRoleOrderByLastLoginDescAccountIdAsc(
                org.mockito.ArgumentMatchers.eq(1), any(Pageable.class)
        )).thenReturn(List.of(selected));
        when(matchDataService.getLatestWonMatch(101L)).thenReturn(wonMatch(1001L, 11L));
        when(heroService.getAllById(Set.of(11L))).thenReturn(List.of(hero(11L)));

        try (var context = new AnnotationConfigApplicationContext()) {
            context.register(CacheConfig.class);
            context.registerBean(ProPlayerRepository.class, () -> repository);
            context.registerBean(OpenDotaPlayerMatchDataService.class, () -> matchDataService);
            context.registerBean(HeroService.class, () -> heroService);
            context.registerBean(FeaturedProPlayerService.class);
            context.refresh();

            var service = context.getBean(FeaturedProPlayerService.class);
            FeaturedProPlayersResponse first = service.getFeaturedPlayers();
            FeaturedProPlayersResponse second = service.getFeaturedPlayers();

            assertEquals(first, second);
            verify(repository, times(1)).findByPlayerRoleOrderByLastLoginDescAccountIdAsc(
                    org.mockito.ArgumentMatchers.eq(1), any(Pageable.class)
            );
            verify(matchDataService, times(1)).getLatestWonMatch(101L);
        }
    }

    private ProPlayer player(Long accountId, Integer role) {
        var player = new ProPlayer();
        player.setAccountId(accountId);
        player.setPlayerRole(role);
        player.setPersonaName("Player " + accountId);
        player.setAvatarImageFull("https://avatars.test/" + accountId);
        player.setTeamName("Team " + role);
        player.setTeamTag("T" + role);
        return player;
    }

    private OpenDotaPlayerMatchCandidate wonMatch(Long matchId, Long heroId) {
        return OpenDotaPlayerMatchCandidate.builder()
                .matchId(matchId)
                .playerSlot(0)
                .radiantWin(true)
                .duration(1800)
                .heroId(heroId)
                .startTime(1_700_000_000L)
                .kills(8)
                .deaths(2)
                .assists(10)
                .build();
    }

    private Hero hero(Long id) {
        var hero = new Hero();
        hero.setId(id);
        hero.setLocalizedName("Hero " + id);
        hero.setImage("/hero-" + id + ".png");
        return hero;
    }
}
