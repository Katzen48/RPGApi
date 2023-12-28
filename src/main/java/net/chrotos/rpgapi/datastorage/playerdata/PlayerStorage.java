package net.chrotos.rpgapi.datastorage.playerdata;

import lombok.NonNull;
import net.chrotos.rpgapi.subjects.QuestProgress;

import java.util.UUID;

public interface PlayerStorage {
    QuestProgress getPlayerData(@NonNull UUID uuid);
    void savePlayerData(@NonNull UUID uuid, @NonNull QuestProgress questProgress);
}
