package io.luck.AghsLabs.providers.opendota.service;

import io.luck.AghsLabs.config.CacheConfig;
import org.junit.jupiter.api.Test;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OpenDotaPlayerMatchDataServiceTest {

    @Test
    void comparisonCandidatesUseFullMatchesEndpointWithLimitAndProjection() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://api.opendota.test/api/");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo(allOf(
                        containsString("/players/42/matches"),
                        containsString("limit=3"),
                        containsString("project=match_id"),
                        containsString("project=item_0"))))
                .andRespond(withSuccess("""
                        [{"match_id":99,"player_slot":0,"hero_id":7,"duration":1800,
                          "start_time":1700000000,"item_0":42}]
                        """, MediaType.APPLICATION_JSON));
        var service = new OpenDotaPlayerMatchDataService(builder.build());

        var result = service.getPlayerMatches(42L, 3);

        assertEquals(1, result.size());
        assertEquals(99L, result.getFirst().matchId());
        assertEquals(42L, result.getFirst().item0());
        server.verify();
    }

    @Test
    void matchDetailsMethodDeclaresSharedCache() throws Exception {
        Cacheable annotation = OpenDotaPlayerMatchDataService.class
                .getMethod("getMatchDetails", Long.class)
                .getAnnotation(Cacheable.class);

        assertNotNull(annotation);
        assertEquals(CacheConfig.MATCH_DETAILS, annotation.cacheNames()[0]);
    }

    @Test
    void latestWonMatchRequestsOneWinWithOnlyFeaturedFields() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://api.opendota.test/api/");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo(allOf(
                        containsString("/players/42/matches"),
                        containsString("win=1"),
                        containsString("limit=1"),
                        containsString("project=match_id"),
                        containsString("project=hero_id"),
                        containsString("project=assists"))))
                .andRespond(withSuccess("""
                        [{"match_id":99,"player_slot":0,"radiant_win":true,"hero_id":7,
                          "start_time":1700000000,"duration":1800,"kills":4,"deaths":1,"assists":9}]
                        """, MediaType.APPLICATION_JSON));
        var service = new OpenDotaPlayerMatchDataService(builder.build());

        var result = service.getLatestWonMatch(42L);

        assertNotNull(result);
        assertEquals(99L, result.matchId());
        assertEquals(7L, result.heroId());
        assertEquals(true, result.playerWon());
        server.verify();
    }
}
