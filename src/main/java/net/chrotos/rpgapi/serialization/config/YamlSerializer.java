package net.chrotos.rpgapi.serialization.config;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.chrotos.rpgapi.actions.*;
import net.chrotos.rpgapi.actions.initialization.InitializationActions;
import net.chrotos.rpgapi.datastorage.config.YamlStore;
import net.chrotos.rpgapi.criteria.*;
import net.chrotos.rpgapi.gui.QuestLogGuiItem;
import net.chrotos.rpgapi.gui.Translatable;
import net.chrotos.rpgapi.quests.Quest;
import net.chrotos.rpgapi.quests.QuestCriterion;
import net.chrotos.rpgapi.quests.QuestStep;
import net.chrotos.rpgapi.selectors.IntegerRange;
import net.chrotos.rpgapi.selectors.Location;
import net.chrotos.rpgapi.selectors.LocationParameters;
import net.chrotos.rpgapi.selectors.Player;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class YamlSerializer implements QuestSerializer<YamlStore> {
    @NonNull
    private final YamlStore configStorage;

    @Override
    public Quest getQuest(@NonNull String id) {
        FileConfiguration config = configStorage.getRaw(id);

        Quest.QuestBuilder builder = Quest.builder()
                            .id(id)
                            .name(Objects.requireNonNull(config.getString("name")))
                            .questTab(config.getString("questTab"))
                            .hidden(config.getBoolean("hidden"))
                            .announce(config.getBoolean("announce"))
                            .title(config.getString("title"))
                            .subTitle(config.getString("subTitle"))
                            .level(config.getInt("level"));


        List<Map<?, ?>> questSteps;
        if (!config.contains("questSteps") || (questSteps = config.getMapList("questSteps")).size() < 1) {
            throw new IllegalStateException(String.format("For quest %s were no questSteps defined.", id));
        }
        for (Map<?, ?> step : questSteps) {
            builder.step(mapQuestStep(step, id));
        }

        if (config.contains("actions")) {
            builder.actions(mapActions(getMap(config.getConfigurationSection("actions"))));
        }

        if (config.contains("initializationActions")) {
            builder.initializationActions(mapInitializationActions(
                    getMap(config.getConfigurationSection("initializationActions"))));
        }

        builder.npc(config.getString("npc"));

        Quest quest = builder.build();
        for (QuestStep questStep : quest.getSteps()) {
            questStep.setQuest(quest);
        }

        return quest;
    }

    @Override
    public @NonNull List<Quest> getQuests() {
        return configStorage.getQuestIds().stream().map(this::getQuest).collect(Collectors.toList());
    }

    private QuestStep mapQuestStep(@NonNull Map<?, ?> section, @NonNull String id) {
        QuestStep.QuestStepBuilder builder = QuestStep.builder()
                                .actions(mapActions(getMap(section.get("actions"))));

        if (section.containsKey("level")) {
            builder.level((int) section.get("level"));
        }

        List<?> criteria = (List<?>) section.get("criteria");

        if (criteria == null || criteria.size() < 1) {
            throw new IllegalStateException(String.format("At least one quest step of quest %s does not come with a quest criterion", id));
        }

        for (Object criterion : criteria) {
            builder.criterion(mapQuestCriterion(getMap(criterion), id));
        }

        return builder.build();
    }

    private QuestCriterion mapQuestCriterion(@NonNull Map<?, ?> section, @NonNull String id) {
        QuestCriterion.QuestCriterionBuilder builder = QuestCriterion.builder();

        if (section.size() < 1) {
            throw new IllegalStateException(
                    String.format("A quest criterion of quest %s does not contain any criteria", id));
        }

        if (section.containsKey("quest")) {
            Map<?, ?> quest = getMap(section.get("quest"));
            if (quest != null) {
                net.chrotos.rpgapi.criteria.Quest.QuestBuilder<?, ?> questCriterion = net.chrotos.rpgapi.criteria.Quest.builder();
                questCriterion.id((String) quest.get("id"));
                mapGuiForCriterion(questCriterion, quest);

                builder.quest(questCriterion.build());
            }
        }

        if (section.containsKey("entityKill")) {
            Map<?, ?> entityKill = getMap(section.get("entityKill"));
            if (entityKill != null) {
                EntityKill.EntityKillBuilder<?, ?> entityKillBuilder = EntityKill.builder();
                entityKillBuilder.id((String) entityKill.get("id"));
                entityKillBuilder.type(entityKill.containsKey("entityType") ?
                        EntityType.valueOf((String) entityKill.get("entityType")) : null);
                entityKillBuilder.displayName((String) entityKill.get("displayName"));

                if (entityKill.containsKey("count")) {
                    entityKillBuilder.count((int) entityKill.get("count"));
                }

                if (entityKill.containsKey("location")) {
                    entityKillBuilder.location(mapLocationSelector(getMap(entityKill.get("location"))));
                }

                mapGuiForCriterion(entityKillBuilder, entityKill);

                builder.entityKill(entityKillBuilder.build());
            }
        }

        if (section.containsKey("location")) {
            Map<?, ?> location = getMap(section.get("location"));
            if (location != null) {
                builder.location(mapLocationCriterion(location));
            }
        }

        if (section.containsKey("itemPickup")) {
            Map<?, ?> itemPickup = getMap(section.get("itemPickup"));
            if (itemPickup != null) {
                ItemPickup.ItemPickupBuilder<?, ?> itemPickupBuilder = ItemPickup.builder();
                if (itemPickup.get("displayNames") != null) {
                    for (String displayName : (List<String>) itemPickup.get("displayNames")) {
                        itemPickupBuilder.displayName(displayName);
                    }
                }
                if (itemPickup.get("materials") != null) {
                    for (String material : (List<String>) itemPickup.get("materials")) {
                        itemPickupBuilder.material(Material.getMaterial(material));
                    }
                }

                mapGuiForCriterion(itemPickupBuilder, itemPickup);

                itemPickupBuilder.count(itemPickup.containsKey("count") ? (int) itemPickup.get("count") : 1);
                builder.itemPickup(itemPickupBuilder.build());
            }
        }

        if (section.containsKey("itemUse")) {
            Map<?, ?> itemUse = getMap(section.get("itemUse"));
            if (itemUse != null) {
                ItemUse.ItemUseBuilder<?, ?> itemUseBuilder = ItemUse.builder();

                if (itemUse.containsKey("displayNames")) {
                    for (String displayName : (List<String>) itemUse.get("displayNames")) {
                        itemUseBuilder.displayName(displayName);
                    }
                }
                if (itemUse.containsKey("materials")) {
                    for (String material : (List<String>) itemUse.get("materials")) {
                        itemUseBuilder.material(Material.getMaterial(material));
                    }
                }

                mapGuiForCriterion(itemUseBuilder, itemUse);

                itemUseBuilder.count(itemUse.containsKey("count") ? (int) itemUse.get("count") : 1);
                builder.itemUse(itemUseBuilder.build());
            }
        }

        if (section.containsKey("blockPlacement")) {
            Map<?, ?> blockPlacement = getMap(section.get("blockPlacement"));
            if (blockPlacement != null) {
                BlockPlacement.BlockPlacementBuilder<?, ?> blockPlacementBuilder = BlockPlacement.builder();
                for (String material : (List<String>) blockPlacement.get("materials")) {
                    blockPlacementBuilder.material(Material.getMaterial(material));
                }

                mapGuiForCriterion(blockPlacementBuilder, blockPlacement);

                blockPlacementBuilder.count(blockPlacement.containsKey("count") ? (int) blockPlacement.get("count") : 1);
                builder.blockPlacement(blockPlacementBuilder.build());
            }
        }

        if (section.containsKey("blockBreak")) {
            Map<?, ?> blockBreak = getMap(section.get("blockBreak"));
            if (blockBreak != null) {
                BlockBreak.BlockBreakBuilder<?, ?> blockBreakBuilder = BlockBreak.builder();
                for (String material : (List<String>) blockBreak.get("materials")) {
                    blockBreakBuilder.material(Material.getMaterial(material));
                }

                mapGuiForCriterion(blockBreakBuilder, blockBreak);

                blockBreakBuilder.count(blockBreak.containsKey("count") ? (int) blockBreak.get("count") : 1);
                builder.blockBreak(blockBreakBuilder.build());
            }
        }

        if (section.containsKey("blockHarvest")) {
            Map<?, ?> blockHarvest = getMap(section.get("blockHarvest"));
            if (blockHarvest != null) {
                BlockHarvest.BlockHarvestBuilder<?, ?> blockHarvestBuilder = BlockHarvest.builder();
                for (String material : (List<String>) blockHarvest.get("materials")) {
                    blockHarvestBuilder.material(Material.getMaterial(material));
                }

                mapGuiForCriterion(blockHarvestBuilder, blockHarvest);

                blockHarvestBuilder.count(blockHarvest.containsKey("count") ? (int) blockHarvest.get("count") : 1);
                builder.blockHarvest(blockHarvestBuilder.build());
            }
        }

        if (section.containsKey("entityDamage")) {
            Map<?, ?> entityDamage = getMap(section.get("entityDamage"));
            if (entityDamage != null) {
                EntityDamage.EntityDamageBuilder<?, ?> entityDamageBuilder = EntityDamage.builder();
                entityDamageBuilder.id((String) entityDamage.get("id"));
                entityDamageBuilder.type(entityDamage.containsKey("entityType") ?
                        EntityType.valueOf((String) entityDamage.get("entityType")) : null);
                entityDamageBuilder.displayName((String) entityDamage.get("displayName"));

                if (entityDamage.containsKey("damage")) {
                    entityDamageBuilder.damage((int) entityDamage.get("damage"));
                }

                if (entityDamage.containsKey("location")) {
                    entityDamageBuilder.location(mapLocationSelector(getMap(entityDamage.get("location"))));
                }

                mapGuiForCriterion(entityDamageBuilder, entityDamage);

                builder.entityDamage(entityDamageBuilder.build());
            }
        }

        if (section.containsKey("advancementDone")) {
            Map<?, ?> advancementDone = getMap(section.get("advancementDone"));
            if (advancementDone != null) {
                AdvancementDone.AdvancementDoneBuilder<?, ?> advancementDoneBuilder = AdvancementDone.builder();
                for (String key : (List<String>) advancementDone.get("keys")) {
                    NamespacedKey advancementKey = NamespacedKey.fromString(key);
                    assert Bukkit.getAdvancement(advancementKey) != null;
                    advancementDoneBuilder.key(NamespacedKey.fromString(key));
                }

                mapGuiForCriterion(advancementDoneBuilder, advancementDone);

                builder.advancementDone(advancementDoneBuilder.build());
            }
        }

        if (section.containsKey("inventory")) {
            Map<?, ?> inventory = getMap(section.get("inventory"));
            if (inventory != null) {
                Inventory.InventoryBuilder<?, ?> inventoryBuilder = Inventory.builder();
                if (inventory.containsKey("displayNames")) {
                    for (String displayName : (List<String>) inventory.get("displayNames")) {
                        inventoryBuilder.displayName(displayName);
                    }
                }

                if (inventory.containsKey("materials")) {
                    for (String material : (List<String>) inventory.get("materials")) {
                        inventoryBuilder.material(Material.getMaterial(material));
                    }
                }

                inventoryBuilder.count(inventory.containsKey("count") ? (int) inventory.get("count") : 1);

                if (inventory.containsKey("player")) {
                    inventoryBuilder.player(mapPlayerSelector(getMap(inventory.get("player"))));
                }

                mapGuiForCriterion(inventoryBuilder, inventory);

                builder.inventory(inventoryBuilder.build());
            }
        }

        return builder.build();
    }

    private Player mapPlayerSelector(@NonNull Map<?, ?> section) {
        Player.PlayerBuilder builder = Player.builder();

        if (section.containsKey("id")) {
            builder.id((String) section.get("id"));
        }

        if (section.containsKey("name")) {
            builder.name((String) section.get("name"));
        }

        if (section.containsKey("location")) {
            builder.location(mapLocationSelector(getMap(section.get("location"))));
        }

        return builder.build();
    }

    private net.chrotos.rpgapi.criteria.Location mapLocationCriterion(@NonNull Map<?, ?> section) {
        net.chrotos.rpgapi.criteria.Location.LocationBuilder<?, ?> builder = net.chrotos.rpgapi.criteria.Location.builder();

        builder.world(section.containsKey("world") ? (String) section.get("world") : "world");

        if (section.containsKey("min")) {
            Map<?, ?> min = getMap(section.get("min"));
            builder.min(mapLocationParameters(min));
        }

        if (section.containsKey("max")) {
            Map<?, ?> max = getMap(section.get("max"));
            builder.max(mapLocationParameters(max));
        }

        if (section.containsKey("exact")) {
            Map<?, ?> exact = getMap(section.get("exact"));
            builder.exact(mapLocationParameters(exact));
        }

        mapGuiForCriterion(builder, section);

        return builder.build();
    }

    private Location mapLocationSelector(Map<?, ?> section) {
        Location.LocationBuilder builder = Location.builder();

        if (section != null) {
            builder.world(section.containsKey("world") ? (String) section.get("world") : "world");

            if (section.containsKey("min")) {
                Map<?, ?>  min = getMap(section.get("min"));
                builder.min(mapLocationParameters(min));
            }

            if (section.containsKey("max")) {
                Map<?, ?>  max = getMap(section.get("max"));
                builder.max(mapLocationParameters(max));
            }

            if (section.containsKey("exact")) {
                Map<?, ?>  exact = getMap(section.get("exact"));
                builder.exact(mapLocationParameters(exact));
            }
        }

        return builder.build();
    }

    private LocationParameters mapLocationParameters(@NonNull Map<?, ?>  section) {
        assert section.containsKey("x");
        assert section.containsKey("y");
        assert section.containsKey("z");

        return LocationParameters.builder()
                .x((Integer) section.get("x"))
                .y((Integer) section.get("y"))
                .z((Integer) section.get("z"))
                .build();
    }

    private InitializationActions mapInitializationActions(Map<?, ?> section) {
        Actions actions = mapActions(section);

        InitializationActions.InitializationActionsBuilder builder = InitializationActions.builder()
                                    .loots(actions.getLoots())
                                    .lootTables(actions.getLootTables())
                                    .advancements(actions.getAdvancements())
                                    .commands(actions.getCommands())
                                    .once(section != null && section.containsKey("once") ?
                                            (boolean) section.get("once") : true);

        if (actions.getExperience() != null) {
            builder.experience(actions.getExperience());
        }

        if (actions.getTitle() != null) {
            builder.title(actions.getTitle());
        }

        return builder.build();
    }

    private Actions mapActions(Map<?, ?> section) {
        Actions.ActionsBuilder<?, ?> builder = Actions.builder();

        if (section != null) {
            if (section.containsKey("loots")) {
                List<?> loots = (List<?>) section.get("loots");
                for (Object loot : loots) {
                    builder.loot(mapLoot(getMap(loot)));
                }
            }

            if (section.containsKey("lootTables")) {
                List<?> lootTables = (List<?>) section.get("lootTables");
                for (Object lootTable : lootTables) {
                    builder.lootTable(mapLootTable(getMap(lootTable)));
                }
            }

            if (section.containsKey("experience")) {
                IntegerRange range = mapIntegerRange(getMap(section.get("experience")));
                builder.experience(Experience.builder().min(range.getMin()).max(range.getMax()).build());
            }

            if (section.containsKey("advancements")) {
                List<?> advancements = (List<?>) section.get("advancements");
                for (Object advancement : advancements) {
                    builder.advancement(mapAdvancement(getMap(advancement)));
                }
            }

            if (section.containsKey("title")) {
                Map<?, ?> title = getMap(section.get("title"));
                Title.TitleBuilder titleBuilder = Title.builder();

                titleBuilder.title((String) title.get("title"));

                if (title.containsKey("subTitle")) {
                    titleBuilder.subTitle((String) title.get("subTitle"));
                }

                builder.title(titleBuilder.build());
            }

            if (section.containsKey("commands")) {
                List<?> commands = (List<?>) section.get("commands");
                for (Object command : commands) {
                    builder.command(mapCommand(getMap(command)));
                }
            }
        }

        return builder.build();
    }

    private Command mapCommand(Map<?, ?> command) {
        Command.CommandBuilder builder = Command.builder()
                                                .command((String) command.get("command"));

        if (command.containsKey("asServer")) {
            builder.asServer((boolean) command.get("asServer"));
        }

        return builder.build();
    }

    private Advancement mapAdvancement(Map<?, ?> advancement) {
        NamespacedKey advancementKey = NamespacedKey.fromString((String) advancement.get("key"));
        assert Bukkit.getAdvancement(advancementKey) != null;

        return new Advancement(advancementKey);
    }

    private LootTable mapLootTable(Map<?, ?> lootTable) {
        NamespacedKey lootTableKey = NamespacedKey.fromString((String) lootTable.get("key"));

        assert Bukkit.getLootTable(lootTableKey) != null;

        LootTable.LootTableBuilder builder = LootTable.builder()
                                                .key(lootTableKey);

        if (lootTable.containsKey("lootingModifier")) {
            builder.lootingModifier((Integer) lootTable.get("lootingModifier"));
        }

        return builder.build();
    }

    private Loot mapLoot(Map<?, ?> loot) {
        Loot.LootBuilder builder = Loot.builder()
                .material(Material.getMaterial((String) loot.get("material")))
                .count(mapIntegerRange(getMap(loot.get("count"))));

        if (loot.containsKey("displayName")) {
            builder.displayName((String) loot.get("displayName"));
        }

        if (loot.containsKey("durability")) {
            builder.durability(((Integer)loot.get("durability")).shortValue());
        }

        return builder.build();
    }

    private IntegerRange mapIntegerRange(Map<?, ?> section) {
        IntegerRange.IntegerRangeBuilder<?, ?> range = IntegerRange.builder();

        if (section != null) {
            int min = 1;
            int max;

            if (section.containsKey("min")) {
                min = (Integer) section.get("min");
            }

            if (section.containsKey("max")) {
                max = (Integer) section.get("max");
            } else {
                max = min;
            }

            range.min(min).max(max);
        }

        return range.build();
    }

    private void mapGuiForCriterion(@NonNull Criterion.CriterionBuilder<?,?> criterionBuilder, @NonNull Map<?, ?> section) {
        if (section.containsKey("gui")) {
            criterionBuilder.gui(mapGui(getMap(section.get("gui"))));
        }
    }

    private QuestLogGuiItem mapGui(Map<?, ?> section) {
        if (section == null) {
            return null;
        }

        QuestLogGuiItem.QuestLogGuiItemBuilder builder = QuestLogGuiItem.builder();

        if (section.containsKey("material")) {
            builder.material(Material.getMaterial((String) section.get("material")));
        }

        if (section.containsKey("displayName")) {
            builder.displayName(mapTranslatable(getMap(section.get("displayName"))));
        }

        if (section.containsKey("lores")) {
            List<?> lores = (List<?>) section.get("lores");
            for (Object lore : lores) {
                builder.lore(mapTranslatable(getMap(lore)));
            }
        }

        return builder.build();
    }

    private Translatable mapTranslatable(Map<?, ?> section) {
        if (section == null) {
            return null;
        }

        Translatable.TranslatableBuilder builder = Translatable.builder();

        if (section.containsKey("text")) {
            builder.text((String) section.get("text"));
        }

        if (section.containsKey("key")) {
            builder.text((String) section.get("key"));
        }

        return builder.build();
    }

    private Map<?, ?> getMap(Object object) {
        if (object == null) {
            return null;
        }

        if (object instanceof Map) {
            return (Map<?, ?>) object;
        }

        if (object instanceof ConfigurationSection) {
            return ((ConfigurationSection) object).getValues(true);
        }

        throw new IllegalStateException(object.getClass() + " could not be converted to Map");
    }
}
