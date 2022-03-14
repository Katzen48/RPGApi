package net.chrotos.rpgapi.serialization.config;

import lombok.NonNull;
import net.chrotos.rpgapi.actions.*;
import net.chrotos.rpgapi.actions.initialization.InitializationActions;
import net.chrotos.rpgapi.config.YamlStore;
import net.chrotos.rpgapi.criteria.*;
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
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class YamlSerializer implements QuestSerializer<YamlStore> {
    private YamlStore configStorage;

    @Override
    public void initialize(@NonNull YamlStore configStorage) {
        this.configStorage = configStorage;
    }

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
                            .level(config.getInt("level"))
                            .actions(mapActions(config.getObject("actions", Map.class)))
                            .initializationActions(mapInitializationActions(
                                    config.getObject("initializationActions", Map.class)));


        List<Map<?, ?>> questSteps;
        if (!config.contains("questSteps") || (questSteps = config.getMapList("questSteps")).size() < 1) {
            throw new IllegalStateException(String.format("For quest %s were no questSteps defined.", id));
        }
        for (Map<?, ?> step : questSteps) {
            builder.step(mapQuestStep(step, id));
        }

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
                                .required(!section.containsKey("required") || (boolean) section.get("required"))
                                .actions(mapActions((Map<?, ?>) section.get("actions")));

        List<Map<?, ?>> criteria = (List<Map<?, ?>>) section.get("criteria");

        if (criteria.size() < 1) {
            throw new IllegalStateException(String.format("At least one quest step of quest %s comes with a quest criterion", id));
        }

        for (Map<?, ?> criterion : criteria) {
            builder.criterion(mapQuestCriterion(criterion, id));
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
            Map<?, ?> quest = (Map<?, ?>) section.get("quest");
            builder.quest(new net.chrotos.rpgapi.criteria.Quest((String) quest.get("id")));
        }

        if (section.containsKey("entityKill")) {
            Map<?, ?> entityKill = (Map<?, ?>) section.get("entityKill");
            EntityKill.EntityKillBuilder<?, ?> entityKillBuilder = EntityKill.builder();
            entityKillBuilder.id((String) entityKill.get("id"));
            entityKillBuilder.type(entityKill.containsKey("entityType") ?
                    EntityType.valueOf((String) entityKill.get("entityType")) : null);
            entityKillBuilder.displayName((String) entityKill.get("displayName"));
            entityKillBuilder.location(mapLocationSelector((Map<?, ?>) entityKill.get("location")));

            builder.entityKill(entityKillBuilder.build());
        }

        if (section.containsKey("location")) {
            Map<?, ?> location = (Map<?, ?>) section.get("location");
            builder.location(mapLocationCriterion((Map<?, ?>) location));
        }

        if (section.containsKey("itemPickup")) {
            Map<?, ?> itemPickup = (Map<?, ?>) section.get("itemPickup");
            ItemPickup.ItemPickupBuilder<?, ?> itemPickupBuilder = ItemPickup.builder();
            for (String displayName : (List<String>) itemPickup.get("displayNames")) {
                itemPickupBuilder.displayName(displayName);
            }
            for (String material : (List<String>) itemPickup.get("materials")) {
                itemPickupBuilder.material(Material.getMaterial(material));
            }

            itemPickupBuilder.count(itemPickup.containsKey("count") ? (int) itemPickup.get("count") : 1);
            builder.itemPickup(itemPickupBuilder.build());
        }

        if (section.containsKey("itemUse")) {
            Map<?, ?> itemUse = (Map<?, ?>) section.get("itemUse");
            ItemUse.ItemUseBuilder<?, ?> itemUseBuilder = ItemUse.builder();
            for (String displayName : (List<String>) itemUse.get("displayNames")) {
                itemUseBuilder.displayName(displayName);
            }
            for (String material : (List<String>) itemUse.get("materials")) {
                itemUseBuilder.material(Material.getMaterial(material));
            }

            itemUseBuilder.count(itemUse.containsKey("count") ? (int) itemUse.get("count") : 1);
            builder.itemUse(itemUseBuilder.build());
        }

        if (section.containsKey("blockPlacement")) {
            Map<?, ?> blockPlacement = (Map<?, ?>) section.get("blockPlacement");
            BlockPlacement.BlockPlacementBuilder<?, ?> blockPlacementBuilder = BlockPlacement.builder();
            for (String material : (List<String>) blockPlacement.get("materials")) {
                blockPlacementBuilder.material(Material.getMaterial(material));
            }

            blockPlacementBuilder.count(blockPlacement.containsKey("count") ? (int) blockPlacement.get("count") : 1);
            builder.blockPlacement(blockPlacementBuilder.build());
        }

        if (section.containsKey("entityDamage")) {
            Map<?, ?> entityDamage = (Map<?, ?>) section.get("entityDamage");
            EntityDamage.EntityDamageBuilder<?, ?> entityDamageBuilder = EntityDamage.builder();
            entityDamageBuilder.id((String) entityDamage.get("id"));
            entityDamageBuilder.type(entityDamage.containsKey("entityType") ?
                    EntityType.valueOf((String) entityDamage.get("entityType")) : null);
            entityDamageBuilder.displayName((String) entityDamage.get("displayName"));
            entityDamageBuilder.location(mapLocationSelector((Map<?, ?>) entityDamage.get("location")));

            builder.entityDamage(entityDamageBuilder.build());
        }

        if (section.containsKey("advancementDone")) {
            Map<?, ?> advancementDone = (Map<?, ?>) section.get("advancementDone");
            AdvancementDone.AdvancementDoneBuilder<?, ?> advancementDoneBuilder = AdvancementDone.builder();
            for (String key : (List<String>) advancementDone.get("keys")) {
                NamespacedKey advancementKey = NamespacedKey.fromString(key);
                assert Bukkit.getAdvancement(advancementKey) != null;
                advancementDoneBuilder.key(NamespacedKey.fromString(key));
            }

            builder.advancementDone(advancementDoneBuilder.build());
        }

        if (section.containsKey("inventory")) {
            Map<?, ?> inventory = (Map<?, ?>) section.get("inventory");
            Inventory.InventoryBuilder<?, ?> inventoryBuilder = Inventory.builder();
            for (String displayName : (List<String>) inventory.get("displayNames")) {
                inventoryBuilder.displayName(displayName);
            }
            for (String material : (List<String>) inventory.get("materials")) {
                inventoryBuilder.material(Material.getMaterial(material));
            }
            inventoryBuilder.count(inventory.containsKey("count") ? (int) inventory.get("count") : 1);

            if (inventory.containsKey("player")) {
                inventoryBuilder.player(mapPlayerSelector((Map<?, ?>) inventory.get("player")));
            }

            builder.inventory(inventoryBuilder.build());
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
            builder.location(mapLocationSelector((Map<?, ?>) section.get("location")));
        }

        return builder.build();
    }

    private net.chrotos.rpgapi.criteria.Location mapLocationCriterion(@NonNull Map<?, ?> section) {
        net.chrotos.rpgapi.criteria.Location.LocationBuilder<?, ?> builder = net.chrotos.rpgapi.criteria.Location.builder();

        builder.world(section.containsKey("world") ? (String) section.get("world") : "world");

        if (section.containsKey("min")) {
            Map<?, ?> min = (Map<?, ?>) section.get("min");
            builder.min(mapLocationParameters(min));
        }

        if (section.containsKey("max")) {
            Map<?, ?> max = (Map<?, ?>)section.get("max");
            builder.max(mapLocationParameters(max));
        }

        if (section.containsKey("exact")) {
            Map<?, ?> exact = (Map<?, ?>) section.get("exact");
            builder.exact(mapLocationParameters(exact));
        }

        return builder.build();
    }

    private Location mapLocationSelector(Map<?, ?> section) {
        Location.LocationBuilder builder = Location.builder();

        if (section != null) {
            builder.world(section.containsKey("world") ? (String) section.get("world") : "world");

            if (section.containsKey("min")) {
                Map<?, ?>  min = (Map<?, ?>) section.get("min");
                builder.min(mapLocationParameters(min));
            }

            if (section.containsKey("max")) {
                Map<?, ?>  max = (Map<?, ?>) section.get("max");
                builder.max(mapLocationParameters(max));
            }

            if (section.containsKey("exact")) {
                Map<?, ?>  exact = (Map<?, ?>) section.get("exact");
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
                                    .commands(actions.getCommands());

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
            List<Map<?, ?>> loots = (List<Map<?, ?>>) section.get("loots");
            if (loots != null) {
                for (Map<?, ?> loot : loots) {
                    builder.loot(mapLoot(loot));
                }
            }

            List<Map<?, ?>> lootTables = (List<Map<?, ?>>) section.get("lootTables");
            if (lootTables != null) {
                for (Map<?, ?> lootTable : lootTables) {
                    builder.lootTable(mapLootTable(lootTable));
                }
            }

            if (section.containsKey("experience")) {
                IntegerRange range = mapIntegerRange((Map<?, ?>) section.get("experience"));
                builder.experience(Experience.builder().min(range.getMin()).max(range.getMax()).build());
            }

            List<Map<?, ?>> advancements = (List<Map<?, ?>>) section.get("advancements");
            if (advancements != null) {
                for (Map<?, ?> advancement : advancements) {
                    builder.advancement(mapAdvancement(advancement));
                }
            }

            Map<?, ?> title = (Map<?, ?>) section.get("title");
            if (title != null) {
                Title.TitleBuilder titleBuilder = Title.builder();

                if (title.containsKey("title")) {
                    titleBuilder.title((String) title.get("title"));
                }

                if (title.containsKey("subTitle")) {
                    titleBuilder.subTitle((String) title.get("subTitle"));
                }

                builder.title(titleBuilder.build());
            }

            List<Map<?, ?>> commands = (List<Map<?,?>>) section.get("commands");
            if (commands != null) {
                for (Map<?, ?> command : commands) {
                    builder.command(mapCommand(command));
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
                .count(mapIntegerRange((Map<?, ?>) loot.get("count")));

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
}
