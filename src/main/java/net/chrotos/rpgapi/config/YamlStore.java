package net.chrotos.rpgapi.config;

import com.google.common.io.Files;
import lombok.NonNull;
import net.chrotos.rpgapi.quests.Quest;
import net.chrotos.rpgapi.serialization.config.YamlSerializer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class YamlStore implements ConfigStorage {
    @NonNull
    private final File questsFolder;
    private YamlSerializer questSerializer = new YamlSerializer(this);

    public YamlStore(@NonNull File dataFolder) {
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        this.questsFolder = new File(dataFolder, "quests");
    }

    public FileConfiguration getRaw(@NonNull String id) {
        prepareNewDir();

        return YamlConfiguration.loadConfiguration(new File(questsFolder, id + ".yml"));
    }

    @Override
    public @NonNull List<String> getQuestIds() {
        prepareNewDir();

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

    private void prepareNewDir() {
        if (questsFolder.exists()) {
            return;
        }

        questsFolder.mkdirs();

        try {
            OutputStream out = new FileOutputStream(new File(questsFolder, "example.yml"));
            InputStream in = getClass().getClassLoader().getResourceAsStream("net/chrotos/rpgapi/quests/example.yml");
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
