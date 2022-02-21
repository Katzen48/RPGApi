package net.chrotos.rpgapi.config;

import com.google.common.io.Files;
import lombok.NonNull;
import net.chrotos.rpgapi.quests.Quest;
import net.chrotos.rpgapi.serialization.config.YamlSerializer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class YamlStore implements ConfigStorage {
    @NonNull
    private final File questsFolder;
    private YamlSerializer questSerializer;

    public YamlStore(@NonNull File dataFolder) {
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        this.questsFolder = new File(dataFolder.getAbsolutePath(), "quests");
    }

    @Override
    public void initialize() {
        this.questSerializer = new YamlSerializer();
        questSerializer.initialize(this);
    }

    public FileConfiguration getRaw(@NonNull String id) {
        if (!questsFolder.exists()) {
            questsFolder.mkdirs();
        }

        return YamlConfiguration.loadConfiguration(new File(questsFolder, id + ".yml"));
    }

    @Override
    public @NonNull List<String> getQuestIds() {
        if (!questsFolder.exists()) {
            questsFolder.mkdirs();
        }

        return Arrays.stream(Objects.requireNonNull(questsFolder.listFiles())).map(this::getIdFromFile)
                .collect(Collectors.toList());
    }

    @Override
    public Quest getQuest(@NonNull String id) {
        return questSerializer.getQuest(id);
    }

    @Override
    public @NonNull List<Quest> getQuests() {
        return questSerializer.getQuests();
    }

    private String getIdFromFile(@NonNull File file) {
        return Files.getNameWithoutExtension(file.getName());
    }
}
