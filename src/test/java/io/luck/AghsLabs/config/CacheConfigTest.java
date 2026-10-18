package io.luck.AghsLabs.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CacheConfigTest {

    @Test
    void allComparisonCachesAreBoundedCaffeineCaches() {
        var manager = new CacheConfig().cacheManager();

        for (String name : new String[]{
                CacheConfig.PLAYER_SEARCH,
                CacheConfig.PLAYER_MATCH_CANDIDATES,
                CacheConfig.MATCH_DETAILS,
                CacheConfig.COMPARISON_OVERVIEWS,
                CacheConfig.DEEP_COMPARISONS,
                CacheConfig.FEATURED_PRO_PLAYERS
        }) {
            var cache = manager.getCache(name);
            assertNotNull(cache, name);
            cache.put("key", "value");
            assertEquals("value", cache.get("key", String.class));
        }
    }
}
