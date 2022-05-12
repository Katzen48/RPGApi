package net.chrotos.rpgapi.gui;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Translatable {
    private final String text;
    private final String key;
}
