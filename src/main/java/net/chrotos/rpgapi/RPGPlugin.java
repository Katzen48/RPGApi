package net.chrotos.rpgapi;

import lombok.Getter;
import net.chrotos.rpgapi.commands.QuestCommand;
import net.chrotos.rpgapi.datastorage.backends.FileStorageBackend;
import net.chrotos.rpgapi.datastorage.backends.StorageBackend;
import net.chrotos.rpgapi.datastorage.config.ConfigStorage;
import net.chrotos.rpgapi.criteria.eventhandler.*;
import net.chrotos.rpgapi.datastorage.config.JsonStorage;
import net.chrotos.rpgapi.datastorage.playerdata.PlayerStorage;
import net.chrotos.rpgapi.listener.PlayerEventListener;
import net.chrotos.rpgapi.manager.QuestManager;
import net.chrotos.rpgapi.utils.QuestUtil;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationRegistry;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.PluginLoadOrder;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.annotation.command.Command;
import org.bukkit.plugin.java.annotation.command.Commands;
import org.bukkit.plugin.java.annotation.dependency.SoftDependency;
import org.bukkit.plugin.java.annotation.plugin.ApiVersion;
import org.bukkit.plugin.java.annotation.plugin.LoadOrder;
import org.bukkit.plugin.java.annotation.plugin.Plugin;
import org.bukkit.plugin.java.annotation.plugin.author.Author;

import java.io.*;
import java.net.URL;
import java.security.CodeSource;
import java.util.Arrays;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Plugin(name = "RPGApi", version = "1.18.2")
@Author("Katzen48")
@SoftDependency("ChrotosCloud")
@SoftDependency("Citizens")
@LoadOrder(PluginLoadOrder.POSTWORLD)
@ApiVersion(ApiVersion.Target.v1_18)
@Commands(@Command(name = "quest", aliases = {"questlog"}))
public class RPGPlugin extends JavaPlugin {
    public static final String DEFAULT_NAMESPACE = "rpg";
    @Getter
    private static RPGPlugin instance;
    @Getter
    private QuestManager questManager;
    private StorageBackend fileStorageBackend;
    @Getter
    private ConfigStorage configStorage;
    @Getter
    private PlayerStorage playerStorage;
    @Getter
    private TranslationRegistry translationRegistry;

    @Override
    public void onLoad() {
        super.onLoad();

        instance = this;

        fileStorageBackend = new FileStorageBackend(getDataFolder());

        //QuestUtil.QUEST_BOOK_KEY = new NamespacedKey(this, "questbook");

        //NPC.setEntityTrackingRange(getServer().spigot().getSpigotConfig()
        //      .getDouble("world-settings.default.entity-tracking-range.other", 64));

        if (configStorage == null) {
            configStorage = new JsonStorage(fileStorageBackend);
        }

        if (playerStorage == null) {
            playerStorage = new net.chrotos.rpgapi.datastorage.playerdata.JsonStorage(fileStorageBackend);
        }

        questManager = new QuestManager(this);
        questManager.loadQuests();
    }

    @Override
    public void onEnable() {
        super.onEnable();

        initializeTranslations();

        //questManager.loadNPCs();

        //questManager.getNpcs().stream().filter(npc -> npc.getCitizens() == null).forEach(NPC::spawn);

        /*
        if (isCitizensEnabled()) {
            getServer().getScheduler().runTaskLater(this, () ->
                    questManager.getNpcs().stream()
                                        .filter(npc -> npc.getCitizens() != null && npc.getCitizens().getCitizen() == null)
                                        .forEach(NPC::spawn),
                    5L);
        }
         */

        registerEventHandlers();
        registerCommands();
    }

    @Override
    public void onDisable() {
        super.onDisable();

        //questManager.getNpcs().forEach(NPC::close);
        Bukkit.getOnlinePlayers().forEach(questManager::onPlayerQuit);
    }

    private void registerCommands() {
        // TODO ersetzen mit Command API
        //getCommand("quest").setExecutor(new QuestCommand(this));
    }

    private void registerEventHandlers() {
        // NPCs
        //getServer().getPluginManager().registerEvents(new NPCEventListener(this), this);

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

        // Citizens
        //if (getServer().getPluginManager().getPlugin("Citizens") != null) {
        //    getServer().getPluginManager().registerEvents(new CitizensEventListener(this), this);
        //}
    }

    private void initializeTranslations() {
        File translationsDir = new File(getDataFolder(), "translations");
        if (!translationsDir.exists()) {
            translationsDir.mkdirs();
        }

        CodeSource src = getClass().getProtectionDomain().getCodeSource();
        if (src != null) {
            try {
                URL jar = src.getLocation();
                ZipInputStream zip = new ZipInputStream(jar.openStream());
                ZipEntry entry;
                while((entry = zip.getNextEntry()) != null) {
                    if (!entry.getName().startsWith("net/chrotos/rpgapi/translations")) {
                        continue;
                    }

                    File translationFile = new File(translationsDir, entry.getName().replace("net/chrotos/rpgapi/translations/", ""));
                    if (!translationFile.exists()) {
                        translationFile.createNewFile();

                        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(translationFile));
                        byte[] buffer = new byte[1024];

                        int count;
                        while ((count = zip.read(buffer)) != -1) {
                            out.write(buffer, 0, count);
                        }

                        out.close();
                    }
                }
            } catch (Exception e) {
                getLogger().log(Level.SEVERE, "Could not initialize translations: {0}", e.getMessage());
            }
        }

        translationRegistry = TranslationRegistry.create(Key.key("rpgapi"));
        Arrays.stream(translationsDir.listFiles((dir, name) -> name.endsWith(".properties"))).forEach(file -> {
            try {
                String[] fileNameParts = file.getName().split("_", 2);
                Locale locale = fileNameParts.length > 1 ?
                        Locale.forLanguageTag(fileNameParts[1].replace(".properties", "")) : Locale.US;

                ResourceBundle resourceBundle = new PropertyResourceBundle(new FileInputStream(file));

                translationRegistry.registerAll(locale, resourceBundle, false);
            } catch (Exception e) {
                getLogger().log(Level.SEVERE, "Could not load translation file " + file.getName());
            }
        });

        GlobalTranslator.translator().addSource(translationRegistry);
    }

    /*
    private boolean isCitizensEnabled() {
        org.bukkit.plugin.Plugin plugin = getCitizens();

        return plugin != null && plugin.isEnabled();
    }

    private org.bukkit.plugin.Plugin getCitizens() {
        return getServer().getPluginManager().getPlugin("Citizens");
    }
     */
}
