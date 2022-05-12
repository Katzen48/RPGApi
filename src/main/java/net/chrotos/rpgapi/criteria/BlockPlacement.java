package net.chrotos.rpgapi.criteria;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
import lombok.experimental.SuperBuilder;
import net.chrotos.rpgapi.subjects.QuestSubject;
import net.chrotos.rpgapi.utils.QuestUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Getter
@SuperBuilder
public class BlockPlacement extends Criterion implements Checkable<Block> {
    /**
     * The materials, of the block to be placed. All are substitutes.
     */
    @Singular("material")
    private final List<Material> materials;
    /**
     * The count, of blocks to be placed. If not set, min will be 1
     */
    @Builder.Default
    private final Integer count = 1;

    public boolean check(@NonNull QuestSubject subject, @NonNull Block block) {
        if (!materials.contains(block.getBlockData().getMaterial())) {
            return false;
        }

        return checkIntegerProgress(subject, count);
    }

    @Override
    public ItemStack getGuiItemStack(Locale locale) {
        ItemStack itemStack = new ItemStack(getGuiMaterial(), count);
        ItemMeta meta = itemStack.getItemMeta();

        meta.displayName(orFetch(getGuiDisplayName(locale),
                () -> Component.text(GlobalTranslator.translator().translate(getGuiName().key(), locale).format(null))));
        meta.lore(orFetch(getGuiLores(locale), this::getGuiLore));
        itemStack.setItemMeta(meta);

        return itemStack;
    }

    public Material getGuiMaterial() {
        Material material = materials.size() == 1 ? materials.get(0) : Material.GRASS_BLOCK;
        return QuestUtil.getItemMaterialFromBlockMaterial(material);
    }

    public List<Component> getGuiLore() {
        return materials.stream().map(Component::translatable)
                .collect(Collectors.toList());
    }

    public TranslatableComponent getGuiName() {
        return Component.translatable("quest.criteria.block_place");
    }
}
