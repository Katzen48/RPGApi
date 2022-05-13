package net.chrotos.rpgapi.npc;

import com.google.common.io.Files;
import lombok.NonNull;
import net.chrotos.rpgapi.RPGPlugin;
import net.chrotos.rpgapi.npc.citizens.CitizensTrait;
import net.chrotos.rpgapi.npc.citizens.Skin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class NPCLoader {
    private final RPGPlugin plugin;
    private final File npcFolder;

    public NPCLoader(@NonNull RPGPlugin plugin) {
        this.plugin = plugin;

        npcFolder = new File(plugin.getDataFolder(), "npcs");
        prepareNewDir();
    }

    public List<NPC> getNPCs() {
        return Arrays.stream(npcFolder.listFiles(this::isYamlFile)).map(this::map).collect(Collectors.toList());
    }

    private NPC map(@NonNull File file) {
        String id = Files.getNameWithoutExtension(file.getName());

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        NPC.NPCBuilder builder = NPC.builder();

        builder.plugin(plugin);
        builder.id(id);
        builder.displayName(config.getString("displayName"));
        builder.profession(Villager.Profession.valueOf(config.getString("profession")));
        builder.location(mapLocation(config.getConfigurationSection("location")));

        if (config.contains("blockMarkerMaterial")) {
            builder.blockMarkerMaterial(Material.matchMaterial(config.getString("blockMarkerMaterial")));
        }

        if (config.contains("citizens")) {
            builder.citizens(mapCitizens(config.getConfigurationSection("citizens")));
        }

        plugin.getQuestManager().getQuestGraph().getLevels().stream().flatMap(questLevel -> questLevel.getQuests().stream())
                .filter(quest -> id.equals(quest.getNpc())).forEachOrdered(builder::quest);

        return builder.build();
    }

    private CitizensTrait mapCitizens(@NonNull ConfigurationSection section) {
        CitizensTrait.CitizensTraitBuilder builder = CitizensTrait.builder();

        if (section.contains("type")) {
            builder.type(EntityType.valueOf(section.getString("type")));
        }

        if (section.contains("skin")) {
            builder.skin(mapSkin(section.getConfigurationSection("skin")));
        }

        return builder.build();
    }

    private Skin mapSkin(@NonNull ConfigurationSection section) {
        Skin.SkinBuilder builder = Skin.builder();

        if (section.contains("name")) {
            builder.name(section.getString("name"));
        }

        if (section.contains("texture")) {
            builder.texture(section.getString("texture"));
        }

        if (section.contains("signature")) {
            builder.signature(section.getString("signature"));
        }

        return builder.build();
    }

    private Location mapLocation(@NonNull ConfigurationSection section) {
        return new Location(Bukkit.getWorld(section.getString("world")), section.getDouble("x"),
                section.getDouble("y"), section.getDouble("z"), (float) section.getDouble("yaw"),
                (float) section.getDouble("pitch"));
    }

    private boolean isYamlFile(@NonNull File file) {
        String extension = Files.getFileExtension(file.getName());
        return extension.equals("yaml") || extension.equals("yml");
    }

    private void prepareNewDir() {
        if (npcFolder.exists()) {
            return;
        }

        npcFolder.mkdirs();

        try {
            OutputStream out = new FileOutputStream(new File(npcFolder, "example.yml"));
            InputStream in = getClass().getClassLoader().getResourceAsStream("net/chrotos/rpgapi/npcs/example.yml");
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
