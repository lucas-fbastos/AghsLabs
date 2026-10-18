package io.luck.AghsLabs.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String PLAYER_SEARCH = "playerSearch";
    public static final String PLAYER_MATCH_CANDIDATES = "playerMatchCandidates";
    public static final String MATCH_DETAILS = "matchDetails";
    public static final String COMPARISON_OVERVIEWS = "comparisonOverviews";
    public static final String DEEP_COMPARISONS = "deepComparisons";
    public static final String FEATURED_PRO_PLAYERS = "featuredProPlayers";

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager();
        manager.registerCustomCache(PLAYER_SEARCH, Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(Duration.ofMinutes(5))
                .build());
        manager.registerCustomCache(PLAYER_MATCH_CANDIDATES, Caffeine.newBuilder()
                .maximumSize(1_000)
                .expireAfterWrite(Duration.ofMinutes(10))
                .build());
        manager.registerCustomCache(MATCH_DETAILS, Caffeine.newBuilder()
                .maximumSize(5_000)
                .expireAfterWrite(Duration.ofHours(12))
                .build());
        manager.registerCustomCache(COMPARISON_OVERVIEWS, Caffeine.newBuilder()
                .maximumSize(1_000)
                .expireAfterWrite(Duration.ofMinutes(10))
                .build());
        manager.registerCustomCache(DEEP_COMPARISONS, Caffeine.newBuilder()
                .maximumSize(5_000)
                .expireAfterWrite(Duration.ofHours(1))
                .build());
        manager.registerCustomCache(FEATURED_PRO_PLAYERS, Caffeine.newBuilder()
                .maximumSize(1)
                .expireAfterWrite(Duration.ofDays(5))
                .build());
        return manager;
    }
}
