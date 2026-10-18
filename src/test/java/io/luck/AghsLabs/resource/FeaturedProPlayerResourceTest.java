package io.luck.AghsLabs.resource;

import io.luck.AghsLabs.model.dtos.FeaturedProPlayersResponse;
import io.luck.AghsLabs.service.FeaturedProPlayerService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FeaturedProPlayerResourceTest {

    @Test
    void featuredRouteReturnsSingularPlayerContract() throws Exception {
        var service = mock(FeaturedProPlayerService.class);
        var response = new FeaturedProPlayersResponse(
                Instant.parse("2026-10-13T10:00:00Z"),
                List.of(new FeaturedProPlayersResponse.FeaturedRole(
                        1,
                        "Core",
                        new FeaturedProPlayersResponse.FeaturedPlayer(
                                42L,
                                "Pro",
                                "https://avatars.test/pro",
                                "Team",
                                "TAG",
                                new FeaturedProPlayersResponse.FeaturedMatch(
                                        99L,
                                        Instant.parse("2026-10-12T10:00:00Z"),
                                        Duration.ofMinutes(30),
                                        8,
                                        2,
                                        10,
                                        new FeaturedProPlayersResponse.FeaturedHero(
                                                7L, "Hero", "/hero.png"
                                        )
                                )
                        )
                ))
        );
        when(service.getFeaturedPlayers()).thenReturn(response);
        var mvc = MockMvcBuilders.standaloneSetup(new FeaturedProPlayerResource(service)).build();

        mvc.perform(get("/players/featured"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles[0].playerRole").value(1))
                .andExpect(jsonPath("$.roles[0].roleLabel").value("Core"))
                .andExpect(jsonPath("$.roles[0].player.accountId").value(42))
                .andExpect(jsonPath("$.roles[0].player.recentWin.matchId").value(99))
                .andExpect(jsonPath("$.roles[0].player.recentWin.hero.id").value(7));
        verify(service).getFeaturedPlayers();
    }
}
