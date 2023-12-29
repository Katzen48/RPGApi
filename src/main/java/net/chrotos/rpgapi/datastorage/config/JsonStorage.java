package net.chrotos.rpgapi.datastorage.config;

import com.google.gson.Gson;
import lombok.NonNull;
import net.chrotos.rpgapi.criteria.Criteria;
import net.chrotos.rpgapi.datastorage.backends.StorageBackend;
import net.chrotos.rpgapi.quests.Quest;
import net.chrotos.rpgapi.quests.QuestStep;
import net.chrotos.rpgapi.serialization.config.CriteriaTypeAdapter;
import net.chrotos.rpgapi.serialization.config.QuestStepTypeAdapter;
import net.chrotos.rpgapi.serialization.config.QuestTypeAdapter;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.LinkedList;

public class JsonStorage implements ConfigStorage {
    private final Gson gson;
    private final StorageBackend storageBackend;

    public JsonStorage(@NonNull StorageBackend storageBackend) {
        this.gson = GsonComponentSerializer.gson().serializer().newBuilder()
                .registerTypeAdapter(Quest.class, new QuestTypeAdapter())
                .registerTypeAdapter(QuestStep.class, new QuestStepTypeAdapter())
                .registerTypeAdapter(Criteria.class, new CriteriaTypeAdapter())
                .create();

        this.storageBackend = storageBackend;
    }

    @Override
    public Iterable<Quest> loadQuests() {
        LinkedList<Quest> quests = new LinkedList<>();
        storageBackend.listQuests().forEach(quest -> {
            try {
                quests.add(gson.fromJson(storageBackend.getQuest(quest), Quest.class));
            } catch (Exception e) {
                JavaPlugin.getProvidingPlugin(getClass()).getLogger().severe("Failed to load quest " + quest + "!");
            }
        });

        return quests;
    }
}
