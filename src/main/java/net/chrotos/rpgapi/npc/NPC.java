package net.chrotos.rpgapi.npc;

import lombok.*;
import net.chrotos.rpgapi.RPGPlugin;
import net.chrotos.rpgapi.npc.citizens.CitizensTrait;
import net.chrotos.rpgapi.quests.Quest;
import net.chrotos.rpgapi.quests.QuestCriterion;
import net.chrotos.rpgapi.selectors.LocationParameters;
import net.chrotos.rpgapi.subjects.QuestProgress;
import net.chrotos.rpgapi.subjects.QuestSubject;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.function.Function;

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
     * Configuration of the "Citizens2" npc. Requires the Citizens2 plugin.
     */
    private final CitizensTrait citizens;
    /**
     * The profession of the villager.
     */
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
    /***
     * The material, that should be used for the completion quest marker particle, if this NPCS has one
     */
    private final Material completionMarkerMaterial;
    /**
     * The quests, this npc should start.
     */
    private final List<Quest> quests;
    /**
     * The entity instance after spawning.
     */
    private Villager entity;
    @Getter(AccessLevel.NONE)
    private BukkitTask markerTask;
    @Getter(AccessLevel.NONE)
    private BukkitTask completionMarkerTask;
    @Setter
    private static double entityTrackingRange = 32;

    @Builder
    public NPC(@NonNull RPGPlugin plugin, @NonNull String id, String displayName, Villager.Profession profession,
               @NonNull Location location, Material blockMarkerMaterial, Material completionMarkerMaterial,
               @NonNull @Singular("quest") List<Quest> quests,
               CitizensTrait citizens) {
        this.plugin = plugin;
        this.id = id;
        this.displayName = displayName;
        this.profession = profession;
        this.location = location;
        this.blockMarkerMaterial = blockMarkerMaterial;
        this.completionMarkerMaterial = completionMarkerMaterial;
        this.quests = quests;
        this.citizens = citizens;
    }

    public void spawn() {
        if (citizens == null) {
            spawnVillager();
        } else {
            if (citizens.getCitizen() != null) {
                reset(citizens.getCitizen().getEntity());
            }
            citizens.spawn(this);
        }

        if (blockMarkerMaterial != null) {
            markerTask = createMarkerTask(blockMarkerMaterial, this::hasQuest, plugin);
        }

        if (completionMarkerMaterial != null) {
            completionMarkerTask = createMarkerTask(completionMarkerMaterial, this::hasToReturn, plugin);
        }
    }

    private BukkitTask createMarkerTask(@NonNull Material material, @NonNull Function<QuestSubject, Boolean> checkFunction,
                                        @NonNull RPGPlugin plugin) {
        return Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if ((citizens == null || citizens.getCitizen() == null || !citizens.getCitizen().isSpawned())
                    && (entity == null || entity.isDead())) {
                return;
            }

            BlockData blockData = Bukkit.createBlockData(material);
            Location villagerLocation = location;
            Location blockMarkerLocation = villagerLocation.clone().add(0, 2.5, 0);

            villagerLocation.getWorld().getNearbyEntitiesByType(Player.class, villagerLocation, entityTrackingRange).forEach(player -> {
                QuestSubject subject = plugin.getQuestManager().getQuestSubject(player.getUniqueId());

                if (subject == null) {
                    return;
                }

                if (!checkFunction.apply(subject)) {
                    return;
                }

                player.spawnParticle(Particle.BLOCK_MARKER, blockMarkerLocation, 1, blockData);
            });
        }, 20L, 4 * 20L);
    }

    private void spawnVillager() {
        if (entity != null && !entity.isDead() && entity.isValid()) {
            return;
        }

        reset(entity);

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
    }

    private void reset(Entity entity) {
        if (entity != null) {
            cancelParticle();
            despawnEntity();
        }
    }

    public boolean hasQuest(@NonNull QuestSubject subject) {
        return getNextQuest(subject) != null;
    }

    public boolean hasToReturn(@NonNull QuestSubject subject) {
        if (subject.getActiveQuests().size() < 1) {
            return false;
        }

        return subject.getActiveQuests().stream()
                .anyMatch(quest -> {
                    if (!quests.contains(quest)) {
                        return false;
                    }

                    QuestProgress progress = subject.getQuestProgress().stream()
                            .filter(questProgress -> questProgress.getQuest() == quest).findFirst().orElse(null);

                    if (progress == null || progress.getCompletedSteps().size() != (quest.getSteps().size() - 1)) {
                        return false;
                    }

                    List<QuestCriterion> lastCriteria = progress.getActiveQuestSteps().get(0).getCriteria();

                    if (progress.getCompletedCriteria().size() != (lastCriteria.size() - 1)) {
                        return false;
                    }

                    QuestCriterion lastCriterion = lastCriteria.stream()
                            .filter(criterion -> !progress.getCompletedQuestCriteria().contains(criterion)).findFirst()
                            .orElse(null);

                    if (lastCriterion == null || lastCriterion.getLocation() == null) {
                        return false;
                    }

                    net.chrotos.rpgapi.criteria.Location criterionLocation = lastCriterion.getLocation();

                    if (!criterionLocation.getWorld().equals(location.getWorld().getName())) {
                        return false;
                    }

                    if (criterionLocation.getExact() != null) {
                        return criterionLocation.getExact().equal(location.getBlockX(), location.getBlockY(), location.getBlockZ());
                    } else {
                        return LocationParameters.between(criterionLocation.getMin(), criterionLocation.getMax(),
                                location.getBlockX(), location.getBlockY(), location.getBlockZ());
                    }
                });
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
        if (markerTask != null && !markerTask.isCancelled()) {
            markerTask.cancel();
        }

        if (completionMarkerTask != null && ! completionMarkerTask.isCancelled()) {
            completionMarkerTask.cancel();
        }
    }

    private void despawnEntity() {
        try {
            if (citizens == null) {
                if (entity == null) {
                    return;
                }

                entity.setHealth(0);
                entity.remove();
            } else {
                if (citizens.getCitizen() == null) {
                    return;
                }

                citizens.getCitizen().despawn();
            }
        } catch (Exception ignored) {}
    }

    @Override
    public void close() {
        cancelParticle();
        despawnEntity();
        location.getWorld().removePluginChunkTicket(location.getChunk().getX(), location.getChunk().getZ(), plugin);
    }
}
