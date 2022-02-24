package net.chrotos.rpgapi.serialization.config;

import lombok.NonNull;
import net.chrotos.rpgapi.actions.*;
import net.chrotos.rpgapi.config.YamlStore;
import net.chrotos.rpgapi.criteria.*;
import net.chrotos.rpgapi.quests.Quest;
import net.chrotos.rpgapi.quests.QuestCriterion;
import net.chrotos.rpgapi.quests.QuestStep;
import net.chrotos.rpgapi.selectors.IntegerRange;
import net.chrotos.rpgapi.selectors.Location;
import net.chrotos.rpgapi.selectors.LocationParameters;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;

import java.util.List;
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
                                            .actions(mapActions(config.getConfigurationSection("actions")));

        if (!config.contains("questSteps")) {
            throw new IllegalStateException(String.format("For %s quest were no questSteps defined.", id));
        }

        ConfigurationSection questSteps = config.getConfigurationSection("questSteps");
        for (String key : questSteps.getKeys(false)) {
            builder.step(mapQuestStep(questSteps.getConfigurationSection(key), id));
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

    private QuestStep mapQuestStep(@NonNull ConfigurationSection section, @NonNull String id) {
        QuestStep.QuestStepBuilder builder = QuestStep.builder()
                                                    .required(section.getBoolean("required", true))
                                                    .actions(mapActions(section.getConfigurationSection("actions")));

        ConfigurationSection criteria = section.getConfigurationSection("criteria");
        for (String key : criteria.getKeys(false)) {
            builder.criterion(mapQuestCriterion(criteria.getConfigurationSection(key), id));
        }

        return builder.build();
    }

    private QuestCriterion mapQuestCriterion(@NonNull ConfigurationSection section, @NonNull String id) {
        QuestCriterion.QuestCriterionBuilder builder = QuestCriterion.builder();

        if (section.getKeys(false).size() < 1) {
            throw new IllegalStateException(
                    String.format("A quest criterion of quest %s does not contain any criteria", id));
        }

        if (section.contains("quest")) {
            ConfigurationSection quest = section.getConfigurationSection("quest");
            builder.quest(new net.chrotos.rpgapi.criteria.Quest(quest.getString("id")));
        }

        if (section.contains("entityKill")) {
            ConfigurationSection entityKill = section.getConfigurationSection("entityKill");
            EntityKill.EntityKillBuilder entityKillBuilder = EntityKill.builder();
            entityKillBuilder.id(entityKill.getString("id"));
            entityKillBuilder.type(entityKill.contains("entityType") ?
                    EntityType.valueOf(entityKill.getString("entityType")) : null);
            entityKillBuilder.displayName(entityKill.getString("displayName"));
            entityKillBuilder.location(mapLocationSelector(entityKill.getConfigurationSection("location")));

            builder.entityKill(entityKillBuilder.build());
        }

        if (section.contains("location")) {
            ConfigurationSection location = section.getConfigurationSection("location");
            builder.location(mapLocationCriterion(location));
        }

        if (section.contains("itemPickup")) {
            ConfigurationSection itemPickup = section.getConfigurationSection("itemPickup");
            ItemPickup.ItemPickupBuilder itemPickupBuilder = ItemPickup.builder();
            for (String displayName : itemPickup.getStringList("displayNames")) {
                itemPickupBuilder.displayName(displayName);
            }
            for (String material : itemPickup.getStringList("materials")) {
                itemPickupBuilder.material(Material.getMaterial(material));
            }

            itemPickupBuilder.count(itemPickup.getInt("count", 1));
            builder.itemPickup(itemPickupBuilder.build());
        }

        if (section.contains("itemUse")) {
            ConfigurationSection itemUse = section.getConfigurationSection("itemUse");
            ItemUse.ItemUseBuilder itemUseBuilder = ItemUse.builder();
            for (String displayName : itemUse.getStringList("displayNames")) {
                itemUseBuilder.displayName(displayName);
            }
            for (String material : itemUse.getStringList("materials")) {
                itemUseBuilder.material(Material.getMaterial(material));
            }

            itemUseBuilder.count(itemUse.getInt("count", 1));
            builder.itemUse(itemUseBuilder.build());
        }

        if (section.contains("blockPlacement")) {
            ConfigurationSection blockPlacement = section.getConfigurationSection("blockPlacement");
            BlockPlacement.BlockPlacementBuilder blockPlacementBuilder = BlockPlacement.builder();
            for (String material : blockPlacement.getStringList("materials")) {
                blockPlacementBuilder.material(Material.getMaterial(material));
            }

            blockPlacementBuilder.count(blockPlacement.getInt("count", 1));
            builder.blockPlacement(blockPlacementBuilder.build());
        }

        if (section.contains("entityDamage")) {
            ConfigurationSection entityDamage = section.getConfigurationSection("entityDamage");
            EntityDamage.EntityDamageBuilder entityDamageBuilder = EntityDamage.builder();
            entityDamageBuilder.id(entityDamage.getString("id"));
            entityDamageBuilder.type(entityDamage.contains("entityType") ?
                    EntityType.valueOf(entityDamage.getString("entityType")) : null);
            entityDamageBuilder.displayName(entityDamage.getString("displayName"));
            entityDamageBuilder.location(mapLocationSelector(entityDamage.getConfigurationSection("location")));

            builder.entityDamage(entityDamageBuilder.build());
        }

        if (section.contains("advancementDone")) {
            ConfigurationSection advancementDone = section.getConfigurationSection("advancementDone");
            AdvancementDone.AdvancementDoneBuilder advancementDoneBuilder = AdvancementDone.builder();
            for (String key : advancementDone.getStringList("keys")) {
                advancementDoneBuilder.key(NamespacedKey.fromString(key));
            }

            builder.advancementDone(advancementDoneBuilder.build());
        }

        return builder.build();
    }

    private net.chrotos.rpgapi.criteria.Location mapLocationCriterion(@NonNull ConfigurationSection section) {
        net.chrotos.rpgapi.criteria.Location.LocationBuilder builder = net.chrotos.rpgapi.criteria.Location.builder();

        builder.world(section.getString("world"));

        if (section.contains("min")) {
            ConfigurationSection min = section.getConfigurationSection("min");
            builder.min(mapLocationParameters(min));
        }

        if (section.contains("max")) {
            ConfigurationSection max = section.getConfigurationSection("max");
            builder.max(mapLocationParameters(max));
        }

        if (section.contains("exact")) {
            ConfigurationSection exact = section.getConfigurationSection("exact");
            builder.exact(mapLocationParameters(exact));
        }

        return builder.build();
    }

    private Location mapLocationSelector(ConfigurationSection section) {
        Location.LocationBuilder builder = Location.builder();

        if (section != null) {
            builder.world(section.getString("world"));

            if (section.contains("min")) {
                ConfigurationSection min = section.getConfigurationSection("min");
                builder.min(mapLocationParameters(min));
            }

            if (section.contains("max")) {
                ConfigurationSection max = section.getConfigurationSection("max");
                builder.max(mapLocationParameters(max));
            }

            if (section.contains("exact")) {
                ConfigurationSection exact = section.getConfigurationSection("exact");
                builder.exact(mapLocationParameters(exact));
            }
        }

        return builder.build();
    }

    private LocationParameters mapLocationParameters(@NonNull ConfigurationSection section) {
        assert section.contains("x");
        assert section.contains("y");
        assert section.contains("z");

        return LocationParameters.builder()
                .x(section.getInt("x"))
                .y(section.getInt("y"))
                .z(section.getInt("z"))
                .build();
    }

    private Actions mapActions(ConfigurationSection section) {
        Actions.ActionsBuilder builder = Actions.builder();

        if (section != null) {
            ConfigurationSection loots = section.getConfigurationSection("loots");
            if (loots != null) {
                for (String key : loots.getKeys(false)) {
                    ConfigurationSection loot = loots.getConfigurationSection(key);
                    builder.loot(mapLoot(loot));
                }
            }

            ConfigurationSection lootTables = section.getConfigurationSection("lootTables");
            if (lootTables != null) {
                for (String key : lootTables.getKeys(false)) {
                    ConfigurationSection lootTable = lootTables.getConfigurationSection(key);
                    builder.lootTable(mapLootTable(lootTable));
                }
            }

            if (section.contains("experience")) {
                IntegerRange range = mapIntegerRange(section.getConfigurationSection("experience"));
                builder.experience(Experience.builder().min(range.getMin()).max(range.getMax()).build());
            }

            ConfigurationSection advancements = section.getConfigurationSection("advancements");
            if (advancements != null) {
                for (String key : advancements.getKeys(false)) {
                    ConfigurationSection advancement = advancements.getConfigurationSection(key);
                    builder.advancement(mapAdvancement(advancement));
                }
            }

            ConfigurationSection title = section.getConfigurationSection("title");
            if (title != null) {
                Title.TitleBuilder titleBuilder = Title.builder();

                if (title.contains("title")) {
                    titleBuilder.title(title.getString("title"));
                }

                if (title.contains("subTitle")) {
                    titleBuilder.subTitle(title.getString("subTitle"));
                }

                builder.title(titleBuilder.build());
            }
        }

        return builder.build();
    }

    private Advancement mapAdvancement(ConfigurationSection advancement) {
        return new Advancement(NamespacedKey.fromString(advancement.getString("key")));
    }

    private LootTable mapLootTable(ConfigurationSection lootTable) {
        LootTable.LootTableBuilder builder = LootTable.builder()
                                                .key(NamespacedKey.fromString(lootTable.getString("key")));

        if (lootTable.contains("lootingModifier")) {
            builder.lootingModifier(lootTable.getInt("lootingModifier"));
        }

        return builder.build();
    }

    private Loot mapLoot(ConfigurationSection loot) {
        Loot.LootBuilder builder = Loot.builder()
                .material(loot.getObject("material", Material.class))
                .count(mapIntegerRange(loot.getConfigurationSection("count")));

        if (loot.contains("displayName")) {
            builder.displayName(loot.getString("displayName"));
        }

        if (loot.contains("durability")) {
            builder.durability((short) loot.getInt("durability"));
        }

        return builder.build();
    }

    private IntegerRange mapIntegerRange(ConfigurationSection section) {
        IntegerRange.IntegerRangeBuilder range = IntegerRange.builder();

        if (section != null) {
            if (section.contains("min")) {
                range.min(section.getInt("min"));
            }

            if (section.contains("max")) {
                range.max(section.getInt("max"));
            }
        }

        return range.build();
    }
}
