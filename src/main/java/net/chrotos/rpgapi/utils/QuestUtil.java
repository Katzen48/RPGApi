package net.chrotos.rpgapi.utils;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.NonNull;
import lombok.SneakyThrows;
import net.chrotos.rpgapi.criteria.Checkable;
import net.chrotos.rpgapi.quests.QuestCriterion;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class QuestUtil {
    public static NamespacedKey QUEST_BOOK_KEY;

    private static final LoadingCache<Class<?>, List<Field>> CRITERIA_FIELDS = CacheBuilder.newBuilder()
            .build(new CacheLoader<>() {
                @Override
                public List<Field> load(@NonNull Class<?> key) {
                    return resolveCriteria(key);
                }
            });

    @SneakyThrows
    public static List<Field> getCriteria(@NonNull QuestCriterion questCriterion) {
        return CRITERIA_FIELDS.get(questCriterion.getClass());
    }

    private static List<Field> resolveCriteria(@NonNull Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> Checkable.class.isAssignableFrom(field.getType())).collect(Collectors.toList());
    }

    public static Material getItemMaterialFromBlockMaterial(@NonNull Material material) {
        return switch (material) {
            case BEETROOTS -> Material.BEETROOT;
            case CARROTS -> Material.CARROT;
            case POTATOES -> Material.POTATO;
            default -> material;
        };
    }

    public static boolean isQuestBook(ItemStack item) {
        if (item == null || item.getType() != Material.BOOK || !item.hasItemMeta()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();

        if (!meta.getPersistentDataContainer().has(QUEST_BOOK_KEY)) {
            return false;
        }

        return meta.getPersistentDataContainer().get(QUEST_BOOK_KEY, PersistentDataType.BYTE) == ((byte) 1);
    }
}
