package net.chrotos.rpgapi.datastorage.config;

import net.chrotos.rpgapi.quests.Quest;

public interface ConfigStorage {
    Iterable<Quest> loadQuests();
}
