package net.chrotos.rpgapi.gui;

import lombok.NonNull;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public interface QuestLogItem {
    ItemStack getGuiItemStack(Locale locale);
    QuestLogGuiItem getGui();
    List<Component> getGuiLore();
    TranslatableComponent getGuiName();

    default Component getGuiDisplayName(@NonNull Locale locale) {
        QuestLogGuiItem gui = getGui();

        if (gui == null) {
            return null;
        }

        if (gui.getDisplayName() != null) {
            Translatable displayName = gui.getDisplayName();

            if (displayName.getText() != null) {
                return Component.text(displayName.getText());
            } else {
                MessageFormat format = GlobalTranslator.translator().translate(displayName.getKey(), locale);
                if (format == null) {
                    return null;
                }

                return Component.text(format.format(null));
            }
        }

        return null;
    }

    @NonNull
    default List<Component> getGuiLores(@NonNull Locale locale) {
        List<Component> components = new ArrayList<>();
        QuestLogGuiItem gui = getGui();

        if (gui != null && gui.getLores() != null && !gui.getLores().isEmpty()) {
            List<Translatable> lores = gui.getLores();

            lores.forEach(lore -> {
                if (lore.getText() != null) {
                    components.add(Component.text(lore.getText()));
                } else {
                    MessageFormat format = GlobalTranslator.translator().translate(lore.getKey(), locale);
                    if (format != null) {
                        components.add(Component.text(format.format(null)));
                    }
                }
            });

        }

        return components;
    }

    default Material getGuiMaterial() {
        QuestLogGuiItem gui = getGui();
        if (gui == null) {
            return null;
        }

        return gui.getMaterial();
    }
}
