package io.luck.AghsLabs.resource;

import io.luck.AghsLabs.model.Match;
import io.luck.AghsLabs.model.MatchChat;
import io.luck.AghsLabs.model.MatchWards;
import io.luck.AghsLabs.providers.opendota.model.OpenDotaPlayerRatio;
import io.luck.AghsLabs.service.MatchService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MatchResourceTest {

    @Test
    void wardsRouteReturnsOnlyPlayerIdentityAndWardLogs() throws Exception {
        var service = mock(MatchService.class);
        var log = new MatchWards.WardLog(60, "obs", "[110, 120]", 0, 0,
                110.5, 120.0, 0.0, false, 12, null);
        when(service.getMatchWards(123L)).thenReturn(new MatchWards(List.of(
                new MatchWards.PlayerWards(0, 1L, 7L, List.of(log), List.of(), List.of(), List.of()))));
        var mvc = MockMvcBuilders.standaloneSetup(new MatchResource(service)).build();

        var body = mvc.perform(get("/match/123/wards"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        var jsonMapper = JsonMapper.builder().build();
        assertEquals(jsonMapper.readTree("""
                {"players":[{"playerSlot":0,"accountId":1,"heroId":7,
                  "obsLog":[{"time":60,"type":"obs","key":"[110, 120]","slot":0,
                    "playerSlot":0,"x":110.5,"y":120.0,"z":0.0,"entityLeft":false,
                    "entityHandle":12,"attackerName":null}],
                  "senLog":[],"obsLeftLog":[],"senLeftLog":[]}]}
                """), jsonMapper.readTree(body));
        verify(service).getMatchWards(123L);
    }

    @Test
    void missingWardsReturnsNotFound() throws Exception {
        var service = mock(MatchService.class);
        var mvc = MockMvcBuilders.standaloneSetup(new MatchResource(service)).build();

        mvc.perform(get("/match/123/wards")).andExpect(status().isNotFound());
        verify(service).getMatchWards(123L);
    }

    @Test
    void chatRouteReturnsOnlyChatAndWordCounts() throws Exception {
        var service = mock(MatchService.class);
        when(service.getMatchChat(123L)).thenReturn(new MatchChat(
                List.of(new MatchChat.Chat(60, "chat", "gg", 0, 0, 42L)), Map.of("gg", 1L), Map.of()));
        var mvc = MockMvcBuilders.standaloneSetup(new MatchResource(service)).build();

        var body = mvc.perform(get("/match/123/chat"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        var jsonMapper = JsonMapper.builder().build();
        assertEquals(jsonMapper.readTree("""
                {"chat":[{"time":60,"type":"chat","key":"gg","slot":0,"playerSlot":0,"accountId":42}],
                 "allWordCounts":{"gg":1},"myWordCounts":{}}
                """), jsonMapper.readTree(body));
        verify(service).getMatchChat(123L);
    }

    @Test
    void missingChatReturnsNotFound() throws Exception {
        var service = mock(MatchService.class);
        var mvc = MockMvcBuilders.standaloneSetup(new MatchResource(service)).build();

        mvc.perform(get("/match/123/chat")).andExpect(status().isNotFound());
        verify(service).getMatchChat(123L);
    }

    @Test
    void existingMatchRoutesKeepTheirPaths() throws Exception {
        var service = mock(MatchService.class);
        when(service.getMatchDetails(123L)).thenReturn(Match.builder().build());
        when(service.getRecentMatches(123L)).thenReturn(List.of());
        when(service.getWinLose(123L)).thenReturn(OpenDotaPlayerRatio.DEFAULT);
        var mvc = MockMvcBuilders.standaloneSetup(new MatchResource(service)).build();

        mvc.perform(get("/matches/details/123")).andExpect(status().isOk());
        mvc.perform(get("/matches/123/recent")).andExpect(status().isOk());
        mvc.perform(get("/matches/123/winLose")).andExpect(status().isOk());
        verify(service).getMatchDetails(123L);
        verify(service).getRecentMatches(123L);
        verify(service).getWinLose(123L);
    }
}
