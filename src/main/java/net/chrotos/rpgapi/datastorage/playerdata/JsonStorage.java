package net.chrotos.rpgapi.datastorage.playerdata;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.NonNull;
import net.chrotos.rpgapi.criteria.CriteriaInstance;
import net.chrotos.rpgapi.datastorage.backends.StorageBackend;
import net.chrotos.rpgapi.serialization.data.CriteriaInstanceTypeAdapter;
import net.chrotos.rpgapi.subjects.QuestProgress;

import java.util.UUID;

public class JsonStorage implements PlayerStorage {
    private final Gson gson;
    private final StorageBackend storageBackend;

    public JsonStorage(@NonNull StorageBackend storageBackend) {
        this.gson = new GsonBuilder()
                //.registerTypeAdapter(QuestProgress.class, new QuestProgressTypeAdapter())
                .registerTypeAdapter(CriteriaInstance.class, new CriteriaInstanceTypeAdapter())
                .create();

        this.storageBackend = storageBackend;
    }

    @Override
    public QuestProgress getPlayerData(@NonNull UUID uuid) {
        return gson.fromJson(storageBackend.getPlayerData(uuid), QuestProgress.class);
    }

    @Override
    public void savePlayerData(@NonNull UUID uuid, @NonNull QuestProgress questProgress) {
        storageBackend.savePlayerData(uuid, gson.toJson(questProgress));
    }
}
