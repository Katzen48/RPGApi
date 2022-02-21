package net.chrotos.rpgapi.datastorage;

import lombok.NonNull;
import net.chrotos.rpgapi.serialization.data.SubjectSerializer;
import net.chrotos.rpgapi.subjects.QuestSubject;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class YamlStore implements SubjectStorage {
    @NonNull
    private final File subjectsFolder;
    private SubjectSerializer subjectSerializer;

    public YamlStore(@NonNull File dataFolder) {
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        this.subjectsFolder = new File(dataFolder.getAbsolutePath(), "subjects");
    }

    @Override
    public void initialize() {
        //this.subjectSerializer = new YamlSerializer(); //TODO: implement YamlSerializer
        subjectSerializer.initialize(this);
    }

    public FileConfiguration getRaw(@NonNull String id) {
        if (!subjectsFolder.exists()) {
            subjectsFolder.mkdirs();
        }

        return YamlConfiguration.loadConfiguration(new File(subjectsFolder, id + ".yml"));
    }

    @Override
    public QuestSubject getSubject(@NonNull UUID uniqueId) {
        return subjectSerializer.getSubject(uniqueId);
    }

    @Override
    public void saveSubject(@NonNull QuestSubject questSubject) {
        subjectSerializer.saveSubject(questSubject);
    }
}
