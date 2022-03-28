package net.chrotos.rpgapi.npc;

import lombok.*;
import net.chrotos.rpgapi.RPGPlugin;
import net.chrotos.rpgapi.quests.Quest;
import net.chrotos.rpgapi.subjects.QuestSubject;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

@Getter
public class NPC implements AutoCloseable {
    @NonNull
    private final RPGPlugin plugin;

    /**
     * The id of this NPC (the file name with extension).
     */
    @NonNull
    private final String id;
    /**
     * The display name for this NPC.
     */
    private final String displayName;
    /**
     * The profession of the villager.
     */
    @NonNull
    private final Villager.Profession profession;
    /**
     * The location, this NPC will be spawned.
     */
    @NonNull
    private final Location location;
    /**
     * The material, that should be used for the quest marker particle, if this NPC has a block marker.
     */
    private final Material blockMarkerMaterial;
    /**
     * The quests, this npc should start.
     */
    private final List<Quest> quests;
    /**
     * The entity instance after spawning.
     */
    private Villager entity;
    @Getter(AccessLevel.NONE)
    private BukkitTask task;
    @Setter
    private static double entityTrackingRange = 32;

    @Builder
    public NPC(@NonNull RPGPlugin plugin, @NonNull String id, String displayName, @NonNull Villager.Profession profession,
               @NonNull Location location, Material blockMarkerMaterial, @NonNull @Singular("quest") List<Quest> quests) {
        this.plugin = plugin;
        this.id = id;
        this.displayName = displayName;
        this.profession = profession;
        this.location = location;
        this.blockMarkerMaterial = blockMarkerMaterial;
        this.quests = quests;
    }

    public void spawn() {
        if (entity != null && !entity.isDead() && entity.isValid()) {
            return;
        }

        if (entity != null) {
            cancelParticle();
            despawnEntity();
        }

        location.getWorld().addPluginChunkTicket(location.getChunk().getX(), location.getChunk().getZ(), plugin);

        entity = location.getWorld().spawn(location, Villager.class);
        entity.setInvulnerable(true);
        entity.setAgeLock(true);
        entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0);
        entity.setCollidable(false);
        entity.setPersistent(false);
        entity.setRemoveWhenFarAway(false);
        entity.setProfession(profession);
        entity.setSilent(true);
        entity.clearLootTable();

        if (displayName != null) {
            entity.customName(LegacyComponentSerializer.builder().build().deserialize(displayName));
            entity.setCustomNameVisible(true);
        }

        if (blockMarkerMaterial == null) {
            return;
        }

        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (entity == null || entity.isDead()) {
                return;
            }

            BlockData blockData = Bukkit.createBlockData(blockMarkerMaterial);
            Location villagerLocation = entity.getLocation();
            Location blockMarkerLocation = villagerLocation.clone().add(0, 3, 0);

            villagerLocation.getWorld().getNearbyEntitiesByType(Player.class, villagerLocation, entityTrackingRange).forEach(player -> {
                QuestSubject subject = plugin.getQuestManager().getQuestSubject(player.getUniqueId());

                if (subject == null) {
                    return;
                }

                if (!hasQuest(subject)) {
                    return;
                }

                player.spawnParticle(Particle.BLOCK_MARKER, blockMarkerLocation, 1, blockData);
            });
        }, 20L, 4 * 20L);
    }

    public boolean hasQuest(@NonNull QuestSubject subject) {
        return getNextQuest(subject) != null;
    }

    public Quest getNextQuest(@NonNull QuestSubject subject) {
        if (subject.getLevel() == null || subject.getActiveQuests().size() > 0 ||
                subject.getLevel().getQuests().size() < 1) {
            return null;
        }

        return subject.getLevel().getQuests().stream().filter(quest -> !subject.getCompletedQuests().contains(quest))
                .filter(quests::contains).findFirst().orElse(null);
    }

    public void cancelParticle() {
        if (task == null || task.isCancelled()) {
            return;
        }

        task.cancel();
    }

    private void despawnEntity() {
        if (entity == null) {
            return;
        }

        try {
            entity.setHealth(0);
            entity.remove();
        } catch (Exception ignored) {}
    }

    @Override
    public void close() {
        cancelParticle();
        despawnEntity();
        location.getWorld().removePluginChunkTicket(location.getChunk().getX(), location.getChunk().getZ(), plugin);
    }
}