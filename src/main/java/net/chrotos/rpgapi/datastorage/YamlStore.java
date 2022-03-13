package net.chrotos.rpgapi.datastorage;

import lombok.NonNull;
import net.chrotos.rpgapi.quests.QuestGraph;
import net.chrotos.rpgapi.serialization.data.SubjectSerializer;
import net.chrotos.rpgapi.serialization.data.YamlSerializer;
import net.chrotos.rpgapi.subjects.QuestSubject;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.function.Function;

public class YamlStore implements SubjectStorage {
    @NonNull
    private final File subjectsFolder;
    private SubjectSerializer<YamlStore> subjectSerializer;

    public YamlStore(@NonNull File dataFolder) {
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        this.subjectsFolder = new File(dataFolder.getAbsolutePath(), "subjects");
    }

    @Override
    public void initialize(@NonNull Function<UUID, ? extends QuestSubject> subjectFunction) {
        this.subjectSerializer = new YamlSerializer();
        subjectSerializer.initialize(this, subjectFunction);
    }

    public FileConfiguration getRaw(@NonNull String id, boolean create) {
        if (!subjectsFolder.exists()) {
            subjectsFolder.mkdirs();
        }

        File file = new File(subjectsFolder, id + ".yml");

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return YamlConfiguration.loadConfiguration(file);
    }

    @Override
    public QuestSubject getSubject(@NonNull UUID uniqueId, @NonNull QuestGraph questGraph) {
        return subjectSerializer.getSubject(uniqueId, questGraph);
    }

    @Override
    public void saveSubject(@NonNull QuestSubject questSubject) {
        subjectSerializer.saveSubject(questSubject);
    }
}
