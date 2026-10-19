package io.luck.AghsLabs.service;

import io.luck.AghsLabs.model.entity.*;
import io.luck.AghsLabs.repository.*;
import io.luck.AghsLabs.providers.opendota.model.OpenDotaHero;
import io.luck.AghsLabs.providers.opendota.model.OpenDotaPatch;
import io.luck.AghsLabs.providers.opendota.model.OpenDotaProPlayer;
import io.luck.AghsLabs.providers.opendota.model.OpenDotaAbility;
import io.luck.AghsLabs.providers.opendota.service.OpenDotaAbilityDataService;
import io.luck.AghsLabs.providers.opendota.service.OpenDotaHeroDataService;
import io.luck.AghsLabs.providers.opendota.service.OpenDotaItemDataService;
import io.luck.AghsLabs.providers.opendota.service.OpenDotaPatchDataService;
import io.luck.AghsLabs.providers.opendota.service.OpenDotaProPlayerDataService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


@Service
public class DbService {

    private final HeroRepository heroRepository;
    private final RoleRepository roleRepository;
    private final ProPlayerRepository proPlayerRepository;
    private final OpenDotaHeroDataService openDotaHeroDataService;
    private final OpenDotaItemDataService openDotaItemDataService;
    private final OpenDotaPatchDataService openDotaPatchDataService;
    private final OpenDotaProPlayerDataService openDotaProPlayerDataService;
    private final ItemSeedService itemSeedService;
    private final PatchRepository patchRepository;
    private final AbilityRepository abilityRepository;
    private final OpenDotaAbilityDataService openDotaAbilityDataService;

    private static final String[] ROLES = {"Carry", "Nuker", "Initiator", "Disabler", "Support", "Escape", "Durable", "Pusher"};

    public DbService(
            HeroRepository heroRepository,
            RoleRepository roleRepository,
            ProPlayerRepository proPlayerRepository,
            OpenDotaHeroDataService openDotaHeroDataService,
            OpenDotaItemDataService openDotaItemDataService,
            OpenDotaPatchDataService openDotaPatchDataService,
            OpenDotaProPlayerDataService openDotaProPlayerDataService,
            ItemSeedService itemSeedService,
            PatchRepository patchRepository,
            AbilityRepository abilityRepository,
            OpenDotaAbilityDataService openDotaAbilityDataService
    ) {
        this.heroRepository = heroRepository;
        this.roleRepository = roleRepository;
        this.proPlayerRepository = proPlayerRepository;
        this.openDotaHeroDataService = openDotaHeroDataService;
        this.openDotaItemDataService = openDotaItemDataService;
        this.openDotaPatchDataService = openDotaPatchDataService;
        this.openDotaProPlayerDataService = openDotaProPlayerDataService;
        this.itemSeedService = itemSeedService;
        this.patchRepository = patchRepository;
        this.abilityRepository = abilityRepository;
        this.openDotaAbilityDataService = openDotaAbilityDataService;
    }

    public void instantiateTestDatabase() {
        seedHeroRoles();
        saveHeroesFromApi();
        seedProPlayers();
        seedItems();
        seedPatch();
        seedAbilities();
    }

    private void seedHeroRoles() {
        var roles  = Arrays.stream(ROLES).map(Role::new).collect(Collectors.toSet());
        roleRepository.saveAll(roles);
    }



    public void seedProPlayers() {
        List<ProPlayer> proPlayersToPersist = openDotaProPlayerDataService.getProPlayers()
                .stream()
                .map(this::toProPlayerEntity)
                .toList();
        this.proPlayerRepository.saveAll(proPlayersToPersist);
    }

    private ProPlayer toProPlayerEntity(OpenDotaProPlayer player) {
        ProPlayer proPlayer = new ProPlayer(
                player.teamName(),
                player.teamTag(),
                player.fantasyRole(),
                player.teamId(),
                player.isLocked(),
                player.lockedUntil()
        );

        proPlayer.setAccountId(player.accountId());
        proPlayer.setMmrEstimate(player.computedMmr());
        proPlayer.setPersonaName(player.personaName());
        proPlayer.setName(player.name());
        proPlayer.setCheese(player.cheese());
        proPlayer.setSteamId(player.steamId());
        proPlayer.setAvatarImage(player.avatar());
        proPlayer.setAvatarImageMedium(player.avatarMedium());
        proPlayer.setAvatarImageFull(player.avatarFull());
        proPlayer.setProfileURL(player.profileUrl());
        proPlayer.setLastLogin(player.lastLogin());
        proPlayer.setLocCountryCode(player.locCountryCode());

        return proPlayer;
    }

    private void saveHeroesFromApi() {
        Map<String, Role> rolesByDescription = roleRepository.findAll()
                .stream()
                .collect(Collectors.toMap(Role::getDescription, role -> role));

        List<Hero> heroesToPersist = openDotaHeroDataService.getAll()
                .values()
                .stream()
                .map(hero -> this.toHeroEntity(hero, rolesByDescription))
                .toList();
        heroRepository.saveAll(heroesToPersist);
    }

    private Hero toHeroEntity(OpenDotaHero hero, Map<String, Role> rolesByDescription) {
        List<Role> roles = hero.roles().stream()
                .map(rolesByDescription::get)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        return new Hero(
                hero.id(),
                hero.name(),
                hero.localizedName(),
                hero.primaryAttribute(),
                hero.attackType(),
                hero.img(),
                hero.icon(),
                roles
        );
    }

    private void seedItems() {
        itemSeedService.seedItems(openDotaItemDataService.getAll().values());
    }

    private void seedPatch() {
        List<Patch> patchesToPersist = openDotaPatchDataService.getAll().stream()
                .map(this::toPatchEntity)
                .toList();
        this.patchRepository.saveAll(patchesToPersist);
    }

    private Patch toPatchEntity(OpenDotaPatch patch) {
        return new Patch(patch.id(), patch.date(), patch.name());
    }

    public void seedAbilities() {
        Map<String, OpenDotaAbility> abilitiesByName = openDotaAbilityDataService.getAll();
        if (abilitiesByName == null) {
            abilitiesByName = Map.of();
        }

        Map<String, OpenDotaAbility> abilityData = abilitiesByName;
        List<Ability> abilitiesToPersist = openDotaAbilityDataService.getIds().stream()
                .map(abilityId -> {
                    OpenDotaAbility ability = abilityData.get(abilityId.name());
                    return new Ability(
                            abilityId.id(),
                            ability == null ? null : ability.displayName(),
                            ability == null ? null : ability.img()
                    );
                })
                .toList();

        abilityRepository.saveAll(abilitiesToPersist);
    }
}
