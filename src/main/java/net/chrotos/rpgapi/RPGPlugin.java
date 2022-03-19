package net.chrotos.rpgapi;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.chrotos.rpgapi.config.ConfigStorage;
import net.chrotos.rpgapi.criteria.eventhandler.*;
import net.chrotos.rpgapi.datastorage.SubjectStorage;
import net.chrotos.rpgapi.listener.PlayerEventListener;
import net.chrotos.rpgapi.manager.QuestManager;
import org.bukkit.plugin.PluginLoadOrder;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.annotation.plugin.ApiVersion;
import org.bukkit.plugin.java.annotation.plugin.LoadOrder;
import org.bukkit.plugin.java.annotation.plugin.Plugin;
import org.bukkit.plugin.java.annotation.plugin.author.Author;

@Plugin(name = "RPGApi", version = "1.18.2")
@Author("Katzen48")
@LoadOrder(PluginLoadOrder.STARTUP)
@ApiVersion(ApiVersion.Target.v1_18)
public class RPGPlugin extends JavaPlugin {
    @Getter
    private QuestManager questManager;
    @Setter
    @NonNull
    private SubjectStorage subjectStorage;
    @Setter
    @NonNull
    private ConfigStorage configStorage;

    @Override
    public void onLoad() {
        super.onLoad();
    }

    @Override
    public void onEnable() {
        super.onEnable();

        if (subjectStorage == null) {
            subjectStorage = new net.chrotos.rpgapi.datastorage.YamlStore(getDataFolder());
        }

        if (configStorage == null) {
            configStorage = new net.chrotos.rpgapi.config.YamlStore(getDataFolder());
        }

        questManager = new QuestManager(getLogger(), subjectStorage, configStorage);
        questManager.getQuestGraph();

        registerEventHandlers();
    }

    private void registerEventHandlers() {
        getServer().getPluginManager().registerEvents(new PlayerEventListener(this), this);

        getServer().getPluginManager().registerEvents(new AdvancementEventHandler(getQuestManager()), this);
        getServer().getPluginManager().registerEvents(new BlockPlacementEventHandler(getQuestManager()), this);
        getServer().getPluginManager().registerEvents(new EntityDamageEventHandler(getQuestManager()), this);
        getServer().getPluginManager().registerEvents(new EntityKillEventHandler(getQuestManager()), this);
        getServer().getPluginManager().registerEvents(new ItemPickupEventHandler(getQuestManager()), this);
        getServer().getPluginManager().registerEvents(new ItemUseEventHandler(getQuestManager()), this);
        getServer().getPluginManager().registerEvents(new LocationEventHandler(getQuestManager()), this);

        getServer().getPluginManager().registerEvents(new InventoryChangeEventHandler(this), this);
    }
}
