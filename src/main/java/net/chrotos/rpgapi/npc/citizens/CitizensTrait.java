package net.chrotos.rpgapi.npc.citizens;

import lombok.Builder;
import lombok.Getter;
import net.chrotos.rpgapi.npc.NPC;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.trait.LookClose;
import net.citizensnpcs.trait.SkinTrait;
import org.bukkit.entity.EntityType;

@Getter
@Builder
public class CitizensTrait {
    @Builder.Default
    private final EntityType type = EntityType.PLAYER;
    private int citizenId = -1;
    private Skin skin;

    public void spawn(NPC npc) {
        net.citizensnpcs.api.npc.NPC citizen;
        if (citizenId == -1) {
            citizen = CitizensAPI.getNPCRegistry().createNPC(type, npc.getDisplayName());
            citizenId = citizen.getId();
        } else {
            citizen = CitizensAPI.getNPCRegistry().getById(citizenId);
        }
        citizen.spawn(npc.getLocation());

        if (skin != null) {
            SkinTrait skinTrait = citizen.getOrAddTrait(SkinTrait.class);
            skinTrait.setShouldUpdateSkins(skin.isUpdate());

            if (skin.getTexture() != null) {
                skinTrait.setSkinPersistent(skin.getName(), skin.getSignature(), skin.getTexture());
            } else {
                skinTrait.setSkinName(skin.getName());
            }
        }

        LookClose lookClose = citizen.getOrAddTrait(LookClose.class);
        lookClose.lookClose(true);
        lookClose.setRange(5D);
        lookClose.setRealisticLooking(true);
    }

    public net.citizensnpcs.api.npc.NPC getCitizen() {
        if (citizenId == -1) {
            return null;
        }

        return CitizensAPI.getNPCRegistry().getById(citizenId);
    }
}
