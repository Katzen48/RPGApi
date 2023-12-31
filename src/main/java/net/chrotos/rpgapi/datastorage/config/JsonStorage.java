package net.chrotos.rpgapi.datastorage.config;

import com.google.gson.Gson;
import lombok.NonNull;
import net.chrotos.rpgapi.criteria.Criteria;
import net.chrotos.rpgapi.datastorage.backends.StorageBackend;
import net.chrotos.rpgapi.quests.Quest;
import net.chrotos.rpgapi.quests.QuestStep;
import net.chrotos.rpgapi.selectors.Location;
import net.chrotos.rpgapi.selectors.LocationParameters;
import net.chrotos.rpgapi.selectors.Player;
import net.chrotos.rpgapi.serialization.config.BlockDataTypeAdapter;
import net.chrotos.rpgapi.serialization.config.CriteriaTypeAdapter;
import net.chrotos.rpgapi.serialization.config.LocationParametersTypeAdapter;
import net.chrotos.rpgapi.serialization.config.LocationTypeAdapter;
import net.chrotos.rpgapi.serialization.config.NamespacedKeyTypeAdapter;
import net.chrotos.rpgapi.serialization.config.PlayerTypeAdapter;
import net.chrotos.rpgapi.serialization.config.QuestStepTypeAdapter;
import net.chrotos.rpgapi.serialization.config.QuestTypeAdapter;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.NamespacedKey;
import org.bukkit.block.data.BlockData;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.LinkedList;
import java.util.List;

public class JsonStorage implements ConfigStorage {
    private final Gson gson;
    private final StorageBackend storageBackend;

    public JsonStorage(@NonNull StorageBackend storageBackend) {
        this.gson = GsonComponentSerializer.gson().serializer().newBuilder()
                .registerTypeAdapter(Quest.class, new QuestTypeAdapter())
                .registerTypeAdapter(QuestStep.class, new QuestStepTypeAdapter())
                .registerTypeAdapter(Criteria.class, new CriteriaTypeAdapter())
                .registerTypeAdapter(NamespacedKey.class, new NamespacedKeyTypeAdapter())
                .registerTypeAdapter(BlockData.class, new BlockDataTypeAdapter())
                .registerTypeAdapter(Location.class, new LocationTypeAdapter())
                .registerTypeAdapter(LocationParameters.class, new LocationParametersTypeAdapter())
                .registerTypeAdapter(Player.class, new PlayerTypeAdapter())
                .create();

        this.storageBackend = storageBackend;
    }

    @Override
    public List<Quest> loadQuests() {
        LinkedList<Quest> quests = new LinkedList<>();
        storageBackend.listQuests().forEach(questKey -> {
            try {
                Quest quest = gson.fromJson(storageBackend.getQuest(questKey), Quest.class);
                quest.setKey(questKey);

                quests.add(quest);
            } catch (Exception e) {
                JavaPlugin.getProvidingPlugin(getClass()).getLogger().severe("Failed to load quest " + questKey + "!");
            }
        });

        return quests;
    }
}
