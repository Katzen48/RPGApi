package net.chrotos.rpgapi.datastorage.backends;

import lombok.NonNull;
import org.bukkit.NamespacedKey;

import java.util.UUID;

public interface StorageBackend {
    void savePlayerData(@NonNull UUID uuid, @NonNull String data);
    String getPlayerData(@NonNull UUID uuid);
    String getQuest(@NonNull NamespacedKey key);
    @NonNull Iterable<NamespacedKey> listQuests();
}
