package io.luck.AghsLabs.resource;

import io.luck.AghsLabs.model.dtos.FeaturedProPlayersResponse;
import io.luck.AghsLabs.service.FeaturedProPlayerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/players")
public class FeaturedProPlayerResource {

    private final FeaturedProPlayerService featuredProPlayerService;

    public FeaturedProPlayerResource(FeaturedProPlayerService featuredProPlayerService) {
        this.featuredProPlayerService = featuredProPlayerService;
    }

    @GetMapping("/featured")
    public FeaturedProPlayersResponse getFeaturedPlayers() {
        return featuredProPlayerService.getFeaturedPlayers();
    }
}
