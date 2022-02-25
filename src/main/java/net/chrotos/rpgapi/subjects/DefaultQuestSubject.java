package net.chrotos.rpgapi.subjects;

import lombok.Getter;
import lombok.NonNull;
import lombok.Synchronized;
import net.chrotos.rpgapi.actions.*;
import net.chrotos.rpgapi.manager.QuestManager;
import net.chrotos.rpgapi.quests.Quest;
import net.chrotos.rpgapi.quests.QuestLevel;
import net.chrotos.rpgapi.quests.QuestStep;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.loot.LootContext;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

@Getter(onMethod = @__(@Synchronized))
public class DefaultQuestSubject implements QuestSubject {
    @NonNull
    private final UUID uniqueId;
    @NonNull
    private final Player player;
    private QuestLevel level;
    private List<Quest> completedQuests;
    private List<Quest> activeQuests;
    private List<QuestProgress> questProgress;

    protected DefaultQuestSubject(@NonNull UUID uniqueId) {
        this.uniqueId = uniqueId;
        this.player = Objects.requireNonNull(Bukkit.getPlayer(uniqueId));
    }

    @Override
    @Synchronized
    public void award(@NonNull Advancement... advancements) {
        for (Advancement advancement : advancements) {
            AdvancementProgress progress = player.getAdvancementProgress(Bukkit.getAdvancement(advancement.getKey()));
            for (String criteria : progress.getRemainingCriteria()) {
                progress.awardCriteria(criteria);
            }
        }
    }

    @Override
    @Synchronized
    public void award(@NonNull Experience experience) {
        player.giveExp(experience.getNext());
    }

    @Override
    @Synchronized
    public void award(@NonNull Loot... loots) throws IllegalStateException {
        for (Loot loot : loots) {
            ItemStack itemStack = new ItemStack(loot.getMaterial(), loot.getCount().getNext());

            if (loot.getDurability() != null) {
                itemStack.setDurability(loot.getDurability());
            }
            if (loot.getDisplayName() != null) {
                ItemMeta meta = itemStack.getItemMeta();
                meta.displayName(Component.text(loot.getDisplayName())); // TODO: i18n
                itemStack.setItemMeta(meta);
            }

            if (!player.getInventory().addItem(itemStack).isEmpty()) {
                throw new IllegalStateException("Inventory is full");
            }
        }
    }

    @Override
    @Synchronized
    public void award(@NonNull LootTable... lootTables) {
        for (LootTable lootTable : lootTables) {
            LootContext.Builder contextBuilder = new LootContext.Builder(player.getLocation()).killer(player);
            if (lootTable.getLootingModifier() != null) {
                contextBuilder.lootingModifier(lootTable.getLootingModifier());
            }

            Bukkit.getLootTable(lootTable.getKey())
                    .fillInventory(player.getInventory(), new Random(), contextBuilder.build());
        }
    }

    @Override
    @Synchronized
    public void award(@NonNull Title title) {
        player.showTitle(net.kyori.adventure.title.Title.title(Component.text(title.getTitle()), // TODO: i18n
                                                                Component.text(title.getSubTitle())));
    }

    @Override
    @Synchronized
    public void complete(@NonNull Quest quest) {
        award(quest.getActions());
    }

    @Override
    @Synchronized
    public void complete(@NonNull QuestStep questStep) {
        award(questStep.getActions());
    }

    @Synchronized
    private void award(Actions actions) {
        if (actions == null) {
            return;
        }

        Loot[] loots = new Loot[actions.getLoots().size()];
        actions.getLoots().toArray(loots);
        award(loots);

        LootTable[] lootTables = new LootTable[actions.getLootTables().size()];
        actions.getLootTables().toArray(lootTables);
        award(lootTables);

        if (actions.getExperience() != null) {
            award(actions.getExperience());
        }

        Advancement[] advancements = new Advancement[actions.getLootTables().size()];
        actions.getAdvancements().toArray(advancements);
        award(advancements);

        if (actions.getTitle() != null) {
            award(actions.getTitle());
        }
    }

    public static DefaultQuestSubject create(@NonNull UUID uniqueId) {
        return new DefaultQuestSubject(uniqueId);
    }

    static {
        QuestManager.setSubjectProvider(DefaultQuestSubject::create);
    }
}
