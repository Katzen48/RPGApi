package net.chrotos.rpgapi;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.chrotos.rpgapi.config.ConfigStorage;
import net.chrotos.rpgapi.criteria.eventhandler.*;
import net.chrotos.rpgapi.datastorage.SubjectStorage;
import net.chrotos.rpgapi.listener.NPCEventListener;
import net.chrotos.rpgapi.listener.PlayerEventListener;
import net.chrotos.rpgapi.manager.QuestManager;
import net.chrotos.rpgapi.npc.NPC;
import net.chrotos.rpgapi.npc.NPCLoader;
import org.bukkit.plugin.PluginLoadOrder;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.annotation.dependency.SoftDependency;
import org.bukkit.plugin.java.annotation.plugin.ApiVersion;
import org.bukkit.plugin.java.annotation.plugin.LoadOrder;
import org.bukkit.plugin.java.annotation.plugin.Plugin;
import org.bukkit.plugin.java.annotation.plugin.author.Author;

@Plugin(name = "RPGApi", version = "1.18.2")
@Author("Katzen48")
@SoftDependency("ChrotosCloud")
@LoadOrder(PluginLoadOrder.POSTWORLD)
@ApiVersion(ApiVersion.Target.v1_18)
public class RPGPlugin extends JavaPlugin {
    @Getter
    private static RPGPlugin instance;
    @Getter
    private QuestManager questManager;
    @Setter
    @NonNull
    private ConfigStorage configStorage;

    @Override
    public void onLoad() {
        super.onLoad();

        instance = this;

        NPC.setEntityTrackingRange(getServer().spigot().getSpigotConfig()
                .getDouble("world-settings.default.entity-tracking-range.other", 64));
    }

    @Override
    public void onEnable() {
        super.onEnable();

        if (configStorage == null) {
            configStorage = new net.chrotos.rpgapi.config.YamlStore(getDataFolder());
        }

        questManager = new QuestManager(getLogger(), getSubjectStorage(), configStorage, new NPCLoader(this));
        questManager.getQuestGraph();
        questManager.loadNPCs();

        questManager.getNpcs().forEach(NPC::spawn);

        registerEventHandlers();
    }

    private SubjectStorage getSubjectStorage() {
        if (getServer().getPluginManager().getPlugin("ChrotosCloud") != null) {
            return new net.chrotos.rpgapi.datastorage.ChrotosCloudStore();
        }

        return new net.chrotos.rpgapi.datastorage.YamlStore(getDataFolder());
    }

    @Override
    public void onDisable() {
        super.onDisable();

        questManager.getNpcs().forEach(NPC::close);
    }

    private void registerEventHandlers() {
        // NPCs
        getServer().getPluginManager().registerEvents(new NPCEventListener(this), this);

        // Player
        getServer().getPluginManager().registerEvents(new PlayerEventListener(this), this);

        // Criteria
        getServer().getPluginManager().registerEvents(new AdvancementEventHandler(getQuestManager()), this);
        getServer().getPluginManager().registerEvents(new BlockPlacementEventHandler(getQuestManager()), this);
        getServer().getPluginManager().registerEvents(new BlockBreakEventHandler(getQuestManager()), this);
        getServer().getPluginManager().registerEvents(new BlockHarvestEventHandler(getQuestManager()), this);
        getServer().getPluginManager().registerEvents(new EntityDamageEventHandler(getQuestManager()), this);
        getServer().getPluginManager().registerEvents(new EntityKillEventHandler(getQuestManager()), this);
        getServer().getPluginManager().registerEvents(new ItemPickupEventHandler(getQuestManager()), this);
        getServer().getPluginManager().registerEvents(new ItemUseEventHandler(getQuestManager()), this);
        getServer().getPluginManager().registerEvents(new LocationEventHandler(getQuestManager()), this);
        getServer().getPluginManager().registerEvents(new InventoryChangeEventHandler(this), this);
    }
}
