package io.luck.AghsLabs.service;

import io.luck.AghsLabs.model.entity.Ability;
import io.luck.AghsLabs.providers.opendota.model.OpenDotaAbility;
import io.luck.AghsLabs.providers.opendota.model.OpenDotaAbilityId;
import io.luck.AghsLabs.providers.opendota.service.OpenDotaAbilityDataService;
import io.luck.AghsLabs.providers.opendota.service.OpenDotaHeroDataService;
import io.luck.AghsLabs.providers.opendota.service.OpenDotaItemDataService;
import io.luck.AghsLabs.providers.opendota.service.OpenDotaPatchDataService;
import io.luck.AghsLabs.providers.opendota.service.OpenDotaProPlayerDataService;
import io.luck.AghsLabs.repository.AbilityRepository;
import io.luck.AghsLabs.repository.HeroRepository;
import io.luck.AghsLabs.repository.PatchRepository;
import io.luck.AghsLabs.repository.ProPlayerRepository;
import io.luck.AghsLabs.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DbServiceTest {

    @Test
    void seedsAbilitiesByJoiningIdsWithAbilityData() {
        var abilityRepository = mock(AbilityRepository.class);
        var abilityDataService = mock(OpenDotaAbilityDataService.class);
        when(abilityDataService.getIds()).thenReturn(List.of(
                new OpenDotaAbilityId(5003L, "antimage_mana_break")
        ));
        when(abilityDataService.getAll()).thenReturn(Map.of(
                "antimage_mana_break",
                new OpenDotaAbility(
                        "Mana Break",
                        "/apps/dota2/images/dota_react/abilities/antimage_mana_break.png"
                )
        ));
        var service = new DbService(
                mock(HeroRepository.class),
                mock(RoleRepository.class),
                mock(ProPlayerRepository.class),
                mock(OpenDotaHeroDataService.class),
                mock(OpenDotaItemDataService.class),
                mock(OpenDotaPatchDataService.class),
                mock(OpenDotaProPlayerDataService.class),
                mock(ItemSeedService.class),
                mock(PatchRepository.class),
                abilityRepository,
                abilityDataService
        );

        service.seedAbilities();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Ability>> captor = ArgumentCaptor.forClass(List.class);
        verify(abilityRepository).saveAll(captor.capture());
        Ability ability = captor.getValue().getFirst();
        assertEquals(5003L, ability.getId());
        assertEquals("Mana Break", ability.getName());
        assertEquals("/apps/dota2/images/dota_react/abilities/antimage_mana_break.png", ability.getIcon());
    }
}
