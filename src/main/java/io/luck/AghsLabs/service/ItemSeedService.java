package io.luck.AghsLabs.service;

import io.luck.AghsLabs.model.entity.Item;
import io.luck.AghsLabs.model.entity.ItemAttribute;
import io.luck.AghsLabs.providers.opendota.model.OpenDotaItem;
import io.luck.AghsLabs.repository.ItemAttributeRepository;
import io.luck.AghsLabs.repository.ItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ItemSeedService {

    private final ItemRepository itemRepository;
    private final ItemAttributeRepository itemAttributeRepository;

    public ItemSeedService(ItemRepository itemRepository, ItemAttributeRepository itemAttributeRepository) {
        this.itemRepository = itemRepository;
        this.itemAttributeRepository = itemAttributeRepository;
    }

    @Transactional
    public void seedItems(Collection<OpenDotaItem> openDotaItems) {
        List<Item> items = openDotaItems.stream()
                .map(openDotaItem -> Item.builder()
                        .itemId(openDotaItem.id())
                        .name(openDotaItem.displayName())
                        .cost(openDotaItem.cost())
                        .image(openDotaItem.img())
                        .cd(openDotaItem.cooldown())
                        .charges(openDotaItem.charges())
                        .created(Boolean.TRUE.equals(openDotaItem.created()))
                        .attributes(List.of())
                        .quality(openDotaItem.quality())
                        .notes(openDotaItem.notes())
                        .lore(openDotaItem.lore())
                        .build())
                .toList();

        Map<Long, Item> itemsById = itemRepository.saveAll(items).stream()
                .collect(Collectors.toMap(Item::getItemId, item -> item));

        List<ItemAttribute> attributes = openDotaItems.stream()
                .flatMap(openDotaItem -> openDotaItem.attributes().stream()
                        .map(attribute -> ItemAttribute.builder()
                                .key(attribute.key())
                                .header(attribute.display() != null ? attribute.display() : attribute.header())
                                .value(attribute.value())
                                .footer(attribute.footer())
                                .item(itemsById.get(openDotaItem.id()))
                                .build()))
                .toList();

        itemAttributeRepository.saveAll(attributes);
    }
}
