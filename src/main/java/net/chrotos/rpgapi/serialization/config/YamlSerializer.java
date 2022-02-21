package net.chrotos.rpgapi.serialization.config;

import lombok.NonNull;
import net.chrotos.rpgapi.actions.*;
import net.chrotos.rpgapi.config.YamlStore;
import net.chrotos.rpgapi.quests.Quest;
import net.chrotos.rpgapi.quests.QuestStep;
import net.chrotos.rpgapi.selectors.IntegerRange;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

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
                                            .actions(mapActions(config));

        // TODO: Quest Steps

        return builder.build();
    }

    @Override
    public @NonNull List<Quest> getQuests() {
        return configStorage.getQuestIds().stream().map(this::getQuest).collect(Collectors.toList());
    }

    private Actions mapActions(FileConfiguration configuration) {
        ConfigurationSection section = configuration.getConfigurationSection("actions");

        Actions.ActionsBuilder builder = Actions.builder();

        if (section != null) {
            ConfigurationSection loots = configuration.getConfigurationSection("loots");
            if (loots != null) {
                for (String key : loots.getKeys(false)) {
                    ConfigurationSection loot = loots.getConfigurationSection(key);
                    builder.loot(mapLoot(loot));
                }
            }

            ConfigurationSection lootTables = configuration.getConfigurationSection("lootTables");
            if (lootTables != null) {
                for (String key : lootTables.getKeys(false)) {
                    ConfigurationSection lootTable = lootTables.getConfigurationSection(key);
                    builder.lootTable(mapLootTable(lootTable));
                }
            }

            if (configuration.contains("experience")) {
                IntegerRange range = mapIntegerRange(configuration.getConfigurationSection("experience"));
                builder.experience(Experience.builder().min(range.getMin()).max(range.getMax()).build());
            }

            ConfigurationSection advancements = configuration.getConfigurationSection("advancements");
            if (advancements != null) {
                for (String key : advancements.getKeys(false)) {
                    ConfigurationSection advancement = advancements.getConfigurationSection(key);
                    builder.advancement(mapAdvancement(advancement));
                }
            }

            ConfigurationSection title = configuration.getConfigurationSection("title");
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
