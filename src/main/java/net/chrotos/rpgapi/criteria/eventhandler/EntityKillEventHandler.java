package net.chrotos.rpgapi.criteria.eventhandler;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.chrotos.rpgapi.criteria.EntityKill;
import net.chrotos.rpgapi.manager.QuestManager;
import net.chrotos.rpgapi.subjects.QuestSubject;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

@RequiredArgsConstructor
public class EntityKillEventHandler implements Listener {
    @NonNull
    private final QuestManager questManager;

    @EventHandler
    public void onEntityDamageByEntity(EntityDeathEvent event) {
        if (event.getEntity().getKiller() == null) {
            return;
        }

        QuestSubject subject = questManager.getQuestSubject(event.getEntity().getKiller().getUniqueId());

        if (subject != null) {
            questManager.checkCompletance(subject, EntityKill.class, event.getEntity());
        }
    }
}
