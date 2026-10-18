package io.luck.AghsLabs.model.dtos;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public record FeaturedProPlayersResponse(
        Instant generatedAt,
        List<FeaturedRole> roles
) {
    public record FeaturedRole(
            Integer playerRole,
            String roleLabel,
            FeaturedPlayer player
    ) {}

    public record FeaturedPlayer(
            Long accountId,
            String personaName,
            String avatarFull,
            String teamName,
            String teamTag,
            FeaturedMatch recentWin
    ) {}

    public record FeaturedMatch(
            Long matchId,
            Instant matchStart,
            Duration duration,
            Integer kills,
            Integer deaths,
            Integer assists,
            FeaturedHero hero
    ) {}

    public record FeaturedHero(
            Long id,
            String localizedName,
            String image
    ) {}
}
