package net.chrotos.rpgapi.utils;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.NonNull;
import lombok.SneakyThrows;
import net.chrotos.rpgapi.criteria.Checkable;
import net.chrotos.rpgapi.quests.QuestCriterion;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class QuestUtil {
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
}
