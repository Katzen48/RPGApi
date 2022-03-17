package net.chrotos.rpgapi.subjects;

import lombok.*;
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

import java.util.*;

@Getter(onMethod = @__(@Synchronized))
public class DefaultQuestSubject implements QuestSubject {
    @NonNull
    private final UUID uniqueId;
    @NonNull
    private final Player player;
    @Setter(onMethod = @__({@Synchronized, @Override}), onParam = @__(@NonNull))
    private QuestLevel level;
    @Setter(onMethod = @__({@Synchronized, @Override}), onParam = @__(@NonNull))
    private List<Quest> completedQuests;
    @Setter(onMethod = @__({@Synchronized, @Override}), onParam = @__(@NonNull))
    private List<Quest> activeQuests;
    @Setter(onMethod = @__({@Synchronized, @Override}), onParam = @__(@NonNull))
    private List<QuestProgress> questProgress;

    protected DefaultQuestSubject(@NonNull UUID uniqueId) {
        this.uniqueId = uniqueId;
        this.player = Objects.requireNonNull(Bukkit.getPlayer(uniqueId));
    }

    @Override
    @NonNull
    public String getName() {
        return player.getName();
    }

    @Override
    @Deprecated
    @NonNull
    public String getDisplayName() {
        return player.getDisplayName();
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

            player.getInventory().addItem(itemStack);
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
                    .populateLoot(new Random(), contextBuilder.build()).forEach(player.getInventory()::addItem);
        }
    }

    @Override
    @Synchronized
    public void award(@NonNull Title title) {
        player.showTitle(net.kyori.adventure.title.Title.title(Component.text(title.getTitle()), // TODO: i18n
                                                                Component.text(title.getSubTitle())));
    }

    @Override
    public void award(@NonNull Command command) {
        if (command.asServer()) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.format(this));
        } else {
            player.performCommand(command.format(this));
        }
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

    @Override
    public void activate(@NonNull Quest quest, @NonNull QuestManager questManager) {
        if (getActiveQuests().contains(quest)) {
            return;
        }

        award(quest.getInitializationActions());
        getActiveQuests().add(quest);

        // TODO check if required quests have already been completed. Refactoring required
        questManager.checkCompletance(this, net.chrotos.rpgapi.criteria.Quest.class, null);
    }

    public static DefaultQuestSubject create(@NonNull UUID uniqueId) {
        return new DefaultQuestSubject(uniqueId);
    }
}
