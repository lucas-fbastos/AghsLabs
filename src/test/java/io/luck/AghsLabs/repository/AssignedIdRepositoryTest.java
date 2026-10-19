package io.luck.AghsLabs.repository;

import io.luck.AghsLabs.model.entity.*;
import io.luck.AghsLabs.providers.opendota.model.OpenDotaItem;
import io.luck.AghsLabs.service.ItemSeedService;
import jakarta.persistence.EntityManager;
import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:assigned-id-test;DB_CLOSE_DELAY=-1;NON_KEYWORDS=USER,KEY,VALUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.jpa.properties.hibernate.session_factory.statement_inspector="
                + "io.luck.AghsLabs.repository.AssignedIdRepositoryTest$SqlInspector"
})
@ActiveProfiles("test")
@Transactional
class AssignedIdRepositoryTest {

    @Autowired
    private EntityManager entityManager;
    @Autowired
    private HeroRepository heroRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private AbilityRepository abilityRepository;
    @Autowired
    private PatchRepository patchRepository;
    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private ProPlayerRepository proPlayerRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private ItemAttributeRepository itemAttributeRepository;
    @Autowired
    private ItemSeedService itemSeedService;

    @Test
    void saveAllInsertsAssignedIdsWithoutExistenceLookups() {
        assertInsertWithoutLookups(heroRepository, List.of(hero(1L), hero(2L)));
        assertInsertWithoutLookups(itemRepository, List.of(item(1L), item(2L)));
        assertInsertWithoutLookups(abilityRepository, List.of(
                new Ability(1L, "Mana Break", null), new Ability(2L, "Blink", null)
        ));
        assertInsertWithoutLookups(patchRepository, List.of(
                new Patch(1, Instant.EPOCH, "First"), new Patch(2, Instant.EPOCH, "Second")
        ));
        assertInsertWithoutLookups(playerRepository, List.of(
                Player.builder().accountId(1L).build(), Player.builder().accountId(2L).build()
        ));
        var firstPro = new ProPlayer();
        firstPro.setAccountId(3L);
        var secondPro = new ProPlayer();
        secondPro.setAccountId(4L);
        assertInsertWithoutLookups(proPlayerRepository, List.of(firstPro, secondPro));
        assertInsertWithoutLookups(new SimpleJpaRepository<>(Team.class, entityManager), List.of(
                new Team(1L, "First", "ONE", null), new Team(2L, "Second", "TWO", null)
        ));
    }

    @Test
    void savingHeroesWithRolesDoesNotIntroduceExistenceLookups() {
        Role role = roleRepository.saveAndFlush(new Role("Carry"));
        entityManager.clear();
        Hero hero = hero(20L);
        hero.setRoles(List.of(role));

        SqlInspector.clear();
        heroRepository.saveAllAndFlush(List.of(hero));

        assertFalse(SqlInspector.statements().stream().anyMatch(sql -> startsWith(sql, "select")),
                () -> "Unexpected lookup: " + SqlInspector.statements());
        entityManager.clear();
        assertEquals(role.getId(), heroRepository.findById(20L).orElseThrow().getRoles().getFirst().getId());
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void itemSeederKeepsItemsManagedUntilAttributesAreInserted() {
        OpenDotaItem item = OpenDotaItem.builder()
                .id(30L)
                .attributes(List.of(new OpenDotaItem.Attribute("damage", "Damage", null, "10", null)))
                .build();

        try {
            SqlInspector.clear();
            itemSeedService.seedItems(List.of(item));

            assertFalse(SqlInspector.statements().stream().anyMatch(sql -> startsWith(sql, "select")),
                    () -> "Unexpected lookup: " + SqlInspector.statements());
            assertEquals(30L, itemAttributeRepository.findAll().getFirst().getItem().getItemId());
        } finally {
            itemAttributeRepository.deleteAllInBatch();
            itemRepository.deleteAllInBatch();
        }
    }

    @Test
    void saveAllCanUpdateAnEntityThatWasLoadedAndDetached() {
        heroRepository.saveAndFlush(hero(10L));
        entityManager.clear();

        Hero loaded = heroRepository.findById(10L).orElseThrow();
        assertFalse(loaded.isNew());
        entityManager.clear();
        loaded.setName("Updated hero");

        SqlInspector.clear();
        heroRepository.saveAllAndFlush(List.of(loaded));
        assertFalse(SqlInspector.statements().stream().anyMatch(sql -> startsWith(sql, "insert")));

        entityManager.clear();
        assertEquals("Updated hero", heroRepository.findById(10L).orElseThrow().getName());
    }

    @Test
    void persistenceTrackingDoesNotChangeJsonIdentifiers() {
        var mapper = JsonMapper.builder().build();
        var heroJson = mapper.valueToTree(hero(1L));
        assertEquals(1L, heroJson.get("id").asLong());

        var itemJson = mapper.valueToTree(item(2L));
        assertEquals(2L, itemJson.get("itemId").asLong());
        assertFalse(itemJson.has("id"));

        var playerJson = mapper.valueToTree(Player.builder().accountId(3L).build());
        assertEquals(3L, playerJson.get("accountId").asLong());
        assertFalse(playerJson.has("id"));

        var teamJson = mapper.valueToTree(new Team(4L, "Team", "TAG", null));
        assertEquals(4L, teamJson.get("teamId").asLong());
        assertFalse(teamJson.has("id"));

        for (var json : List.of(heroJson, itemJson, playerJson, teamJson)) {
            assertFalse(json.has("new"));
            assertFalse(json.has("newEntity"));
        }
    }

    private <ID, T extends AssignedIdEntity<ID>> void assertInsertWithoutLookups(
            JpaRepository<T, ID> repository, List<T> entities
    ) {
        entities.forEach(entity -> assertTrue(entity.isNew()));
        SqlInspector.clear();

        List<T> saved = repository.saveAllAndFlush(entities);

        assertFalse(SqlInspector.statements().stream().anyMatch(sql -> startsWith(sql, "select")),
                () -> "Unexpected lookup: " + SqlInspector.statements());
        for (int index = 0; index < entities.size(); index++) {
            assertSame(entities.get(index), saved.get(index));
            assertFalse(saved.get(index).isNew());
        }

        entityManager.clear();
        for (T entity : saved) {
            T loaded = repository.findById(Objects.requireNonNull(entity.getId())).orElseThrow();
            assertFalse(loaded.isNew());
        }
    }

    private Hero hero(Long id) {
        var hero = new Hero();
        hero.setId(id);
        hero.setName("Hero " + id);
        hero.setRoles(List.of());
        return hero;
    }

    private Item item(Long id) {
        var item = new Item();
        item.setItemId(id);
        item.setAttributes(List.of());
        return item;
    }

    private static boolean startsWith(String sql, String keyword) {
        return sql.stripLeading().toLowerCase(Locale.ROOT).startsWith(keyword);
    }

    public static class SqlInspector implements StatementInspector {
        private static final ThreadLocal<List<String>> SQL = ThreadLocal.withInitial(ArrayList::new);

        @Override
        public String inspect(String sql) {
            SQL.get().add(sql);
            return sql;
        }

        static List<String> statements() {
            return SQL.get();
        }

        static void clear() {
            SQL.get().clear();
        }
    }
}
