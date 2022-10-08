package net.chrotos.rpgapi.listener;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.chrotos.rpgapi.RPGPlugin;
import net.chrotos.rpgapi.npc.NPC;
import net.chrotos.rpgapi.quests.Quest;
import net.chrotos.rpgapi.subjects.QuestSubject;
import net.citizensnpcs.api.event.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

@RequiredArgsConstructor
public class CitizensEventListener implements Listener {
    private final RPGPlugin plugin;

    @EventHandler
    public void onNPCRightClick(NPCRightClickEvent event) {
        NPC npc = getNPC(event);
        if (npc == null) {
            return;
        }

        event.setCancelled(true);
        onNPCClick(event, npc);
    }

    @EventHandler
    public void onNPCLeftClick(NPCLeftClickEvent event) {
        NPC npc = getNPC(event);
        if (npc == null) {
            return;
        }

        event.setCancelled(true);
        onNPCClick(event, npc);
    }

    private void onNPCClick(@NonNull NPCClickEvent event, @NonNull NPC npc) {
        QuestSubject subject = plugin.getQuestManager().getQuestSubject(event.getClicker().getUniqueId());
        if(subject == null) {
            return;
        }

        Quest nextQuest = npc.getNextQuest(subject);
        if (nextQuest == null) {
            return;
        }

        subject.activate(nextQuest, plugin.getQuestManager());
        plugin.getQuestManager().saveQuestSubject(subject.getUniqueId());
    }

    private NPC getNPC(@NonNull NPCEvent event) {
        return plugin.getQuestManager().getNpcs().stream().
                filter(npc -> npc.getCitizens().getCitizen() == event.getNPC()).findFirst().orElse(null);
    }
}
