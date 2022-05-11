package net.chrotos.rpgapi.gui;

import org.bukkit.inventory.ItemStack;

import java.util.Locale;

public interface QuestLogItem {
    ItemStack getGuiItemStack(Locale locale);
}
