package net.chrotos.rpgapi.datastorage.config;

import net.chrotos.rpgapi.quests.Quest;

import java.util.List;

public interface ConfigStorage {
    List<Quest> loadQuests();
}
