package net.chrotos.rpgapi.criteria.eventhandler;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.chrotos.rpgapi.criteria.AdvancementDone;
import net.chrotos.rpgapi.manager.QuestManager;
import net.chrotos.rpgapi.quests.Quest;
import net.chrotos.rpgapi.subjects.QuestSubject;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;

@RequiredArgsConstructor
public class AdvancementEventHandler implements Listener {
    @NonNull
    private final QuestManager questManager;

    @EventHandler
    public void onPlayerAdvancementDone(PlayerAdvancementDoneEvent event) {
        QuestSubject subject = questManager.getQuestSubject(event.getPlayer().getUniqueId());

        if (subject != null) {
            questManager.checkCompletance(subject, AdvancementDone.class, event.getAdvancement());
        }
    }
}
