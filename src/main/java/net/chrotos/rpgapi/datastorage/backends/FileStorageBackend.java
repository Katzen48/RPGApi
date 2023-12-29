package net.chrotos.rpgapi.datastorage.backends;

import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class FileStorageBackend implements StorageBackend {
    private static final String DEFAULT_NAMESPACE = "rpg";
    private static final String PLAYER_DATA_FOLDER_NAME = "players";
    private static final String QUEST_DATA_FOLDER_NAME = "quests";

    @NonNull
    private final File dataFolder;

    @Override
    public void savePlayerData(@NonNull UUID uuid, @NonNull String data) {
        File file = new File(getPlayerDataFolder(), uuid.toString());

        try {
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
            JavaPlugin.getProvidingPlugin(getClass()).getLogger().severe("Failed to create player data file for " + uuid + "!");
            throw new RuntimeException(e);
        }

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(data);
            writer.flush();
        } catch (Exception e) {
            JavaPlugin.getProvidingPlugin(getClass()).getLogger().severe("Failed to write player data file for " + uuid + "!");
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getPlayerData(@NonNull UUID uuid) {
        File file = new File(getPlayerDataFolder(), uuid.toString());

        if (!file.exists()) {
            return null;
        }

        try {
            return new String(Files.readAllBytes(file.toPath()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getQuest(@NonNull NamespacedKey key) {
        File file = new File(getQuestDataFolder(), getPathByKey(key));

        if (!file.exists()) {
            return null;
        }

        try {
            return new String(Files.readAllBytes(file.toPath()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NonNull Iterable<NamespacedKey> listQuests() {
        ObjectArraySet<NamespacedKey> questFiles = new ObjectArraySet<>();

        try (Stream<Path> paths = Files.walk(getQuestDataFolder().toPath())) {
            paths.forEach(path -> {
                if (path.toFile().isDirectory()) {
                    return;
                }

                questFiles.add(getKeyFromFile(path.toFile()));
            });
        } catch (IOException e) {
            JavaPlugin.getProvidingPlugin(getClass()).getLogger().severe(e.toString());
        }

        return questFiles;
    }

    private File getPlayerDataFolder() {
        return getOrCreateFolder(PLAYER_DATA_FOLDER_NAME);
    }

    private File getQuestDataFolder() {
        return getOrCreateFolder(QUEST_DATA_FOLDER_NAME);
    }

    private File getOrCreateFolder(@NonNull String subFolder) {
        File folder = new File(dataFolder, subFolder);

        if (!folder.exists()) {
            folder.mkdirs();
        }

        return folder;
    }

    private String getPathByKey(@NonNull NamespacedKey key) {
        return key.getNamespace() + File.separator + key.getKey();
    }

    private NamespacedKey getKeyFromFile(@NonNull File file) {
        String relative = getQuestDataFolder().toURI().relativize(file.toURI()).getPath();
        String[] parts = relative.split(Pattern.quote(File.separator), 2);
        String namespace = parts.length > 1 ? parts[0] : DEFAULT_NAMESPACE;

        return new NamespacedKey(namespace, parts[1]);
    }
}
