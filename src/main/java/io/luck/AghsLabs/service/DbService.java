package io.luck.AghsLabs.service;

import io.luck.AghsLabs.model.entity.*;
import io.luck.AghsLabs.repository.*;
import io.luck.AghsLabs.providers.opendota.model.OpenDotaHero;
import io.luck.AghsLabs.providers.opendota.model.OpenDotaItem;
import io.luck.AghsLabs.providers.opendota.model.OpenDotaPatch;
import io.luck.AghsLabs.providers.opendota.model.OpenDotaProPlayer;
import io.luck.AghsLabs.providers.opendota.model.OpenDotaAbility;
import io.luck.AghsLabs.providers.opendota.service.OpenDotaAbilityDataService;
import io.luck.AghsLabs.providers.opendota.service.OpenDotaHeroDataService;
import io.luck.AghsLabs.providers.opendota.service.OpenDotaItemDataService;
import io.luck.AghsLabs.providers.opendota.service.OpenDotaPatchDataService;
import io.luck.AghsLabs.providers.opendota.service.OpenDotaProPlayerDataService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service
public class DbService {

    private final HeroRepository heroRepository;
    private final RoleRepository roleRepository;
    private final ProPlayerRepository proPlayerRepository;
    private final OpenDotaHeroDataService openDotaHeroDataService;
    private final OpenDotaItemDataService openDotaItemDataService;
    private final OpenDotaPatchDataService openDotaPatchDataService;
    private final OpenDotaProPlayerDataService openDotaProPlayerDataService;
    private final ItemAttributeRepository itemAttributeRepository;
    private final ItemRepository itemRepository;
    private final PatchRepository patchRepository;
    private final AbilityRepository abilityRepository;
    private final OpenDotaAbilityDataService openDotaAbilityDataService;

    private static final String[] ROLES = {"Carry","Nuker","Initiator","Disabler","Support","Escape","Durable","Pusher"};

    public DbService(
            HeroRepository heroRepository,
            RoleRepository roleRepository,
            ProPlayerRepository proPlayerRepository,
            OpenDotaHeroDataService openDotaHeroDataService,
            OpenDotaItemDataService openDotaItemDataService,
            OpenDotaPatchDataService openDotaPatchDataService,
            OpenDotaProPlayerDataService openDotaProPlayerDataService,
            ItemAttributeRepository itemAttributeRepository,
            ItemRepository itemRepository,
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
        this.itemAttributeRepository = itemAttributeRepository;
        this.itemRepository = itemRepository;
        this.patchRepository = patchRepository;
        this.abilityRepository = abilityRepository;
        this.openDotaAbilityDataService = openDotaAbilityDataService;
    }

    public void instantiateTestDatabase(){
        seedHeroRoles();
        saveHeroesFromApi();
        seedProPlayers();
        seedItems();
        seedPatch();
        seedAbilities();
    }

    private void seedHeroRoles(){ for(String role : ROLES) roleRepository.save(new Role(role));}

    public void seedProPlayers(){
        List<ProPlayer> proPlayersToPersist = openDotaProPlayerDataService.getProPlayers()
                .stream()
                .filter(p-> p.computedMmr() >= ProPlayer.MINIMUM_MMR)
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

    private void saveHeroesFromApi(){
        List<Hero> heroesToPersist = openDotaHeroDataService.getAll()
                .values()
                .stream()
                .map(this::toHeroEntity)
                .toList();
        heroRepository.saveAll(heroesToPersist);
    }

    private Hero toHeroEntity(OpenDotaHero hero) {
        List<Role> roles = roleRepository.findByDescriptionIn(hero.roles());
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

    private void seedItems(){
        for (OpenDotaItem openDotaItem : openDotaItemDataService.getAll().values()) {
            Item item = new Item(
                    openDotaItem.id(),
                    openDotaItem.displayName(),
                    openDotaItem.cost(),
                    openDotaItem.img(),
                    openDotaItem.cooldown(),
                    openDotaItem.charges(),
                    Boolean.TRUE.equals(openDotaItem.created()),
                    List.of(),
                    openDotaItem.quality(),
                    openDotaItem.notes(),
                    openDotaItem.lore()
            );
            Item persistedItem = itemRepository.save(item);
            List<ItemAttribute> attributeList = openDotaItem.attributes() == null
                    ? List.of()
                    : openDotaItem.attributes().stream()
                            .map(attribute -> new ItemAttribute(
                                    null,
                                    attribute.key(),
                                    attribute.display() != null ? attribute.display() : attribute.header(),
                                    attribute.value(),
                                    attribute.footer(),
                                    persistedItem
                            ))
                            .toList();

            if(!attributeList.isEmpty())
                this.itemAttributeRepository.saveAll(attributeList);
        }
    }

    private void seedPatch(){
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
