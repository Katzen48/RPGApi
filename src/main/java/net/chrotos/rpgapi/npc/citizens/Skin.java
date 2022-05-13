package net.chrotos.rpgapi.npc.citizens;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Skin {
    private String name;
    private String signature;
    private String texture;

    @Builder.Default
    private boolean update = false;
}
