package net.chrotos.rpgapi.criteria.eventhandler;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.chrotos.rpgapi.criteria.Inventory;
import net.chrotos.rpgapi.manager.QuestManager;
import net.chrotos.rpgapi.subjects.QuestSubject;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.*;
import java.util.*;

@RequiredArgsConstructor
public class InventoryChangeEventHandler implements Listener {
    @NonNull
    @Setter
    private static QuestManager questManager;

    @EventHandler
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        checkCompletance(event.getEntity(), event.getItem().getItemStack());
    }

    private static void checkCompletance(Entity entity, ItemStack itemStack) {
        if (!(entity instanceof Player)) {
            return;
        }

        QuestSubject subject = questManager.getQuestSubject(entity.getUniqueId());

        if (subject != null) {
            questManager.checkCompletance(subject, Inventory.class, itemStack);
        }
    }

    public static class InventoryInvocationHandler implements InvocationHandler {
        private static final Class<?>[] INTERFACES;
        private static final String[] SUPPORTED_VERSIONS = {"v1_18_R1"};
        private static final String[] SUPPORTED_VERSIONS_HUMAN_READABLE = {"1.18.1"};
        private static final Method GET_HANDLE_METHOD;
        private static final Field ENTITY_HUMAN_INVENTORY_FIELD;
        private static final Method ITEMSTACK_AS_CRAFT_MIRROR_METHOD;

        private final Player player;
        private final Object nmsInventory;

        private InventoryInvocationHandler(@NonNull Player player, @NonNull Object nmsInventory) {
            this.player = player;
            this.nmsInventory = nmsInventory;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (!method.getName().equals("c") || method.getParameterCount() != 2 ||
                    method.getParameterTypes()[0] != int.class ||
                    !method.getParameterTypes()[1].getSimpleName().equals("ItemStack")) {
                return method.invoke(nmsInventory);
            }

            try {
                ItemStack itemStack = getBukkitItemStack(args[1]);
                checkCompletance(player, itemStack);
            } catch (Exception e){
                e.printStackTrace();
            }

            return method.invoke(nmsInventory, args);
        }

        private ItemStack getBukkitItemStack(Object itemStack) throws Throwable {
            return (ItemStack) ITEMSTACK_AS_CRAFT_MIRROR_METHOD.invoke(null, itemStack);
        }

        public static void inject(Player player) throws Throwable {
            Object handle = GET_HANDLE_METHOD.invoke(player);
            Object nmsInventory = ENTITY_HUMAN_INVENTORY_FIELD.get(handle);

            if (Proxy.getInvocationHandler(nmsInventory) != null) {
                return;
            }

            Object proxy = Proxy.newProxyInstance(INTERFACES[0].getClassLoader(), INTERFACES,
                                                        new InventoryInvocationHandler(player, nmsInventory));

            ENTITY_HUMAN_INVENTORY_FIELD.set(handle, proxy);
        }

        static {
            String craftBukkitPackage = Bukkit.getServer().getClass().getPackage().getName();
            String[] packageParts = craftBukkitPackage.split("\\.");
            String nmsVersion = packageParts[packageParts.length - 1];

            if (Arrays.stream(SUPPORTED_VERSIONS).noneMatch(nmsVersion::equals)) { // TODO update to newest version
                throw new IllegalStateException("This plugin version only supports "
                        + String.join("/", SUPPORTED_VERSIONS_HUMAN_READABLE));
            }

            try {
                Class<?> entityHumanClass = Class.forName("net.minecraft.world.entity.player.EntityHuman");

                INTERFACES = new Class[] {entityHumanClass};

                GET_HANDLE_METHOD = Class.forName(craftBukkitPackage + ".entity.CraftEntity")
                        .getDeclaredMethod("getHandle");
                GET_HANDLE_METHOD.setAccessible(true);

                ITEMSTACK_AS_CRAFT_MIRROR_METHOD = Class.forName("org.bukkit.craftbukkit.v1_18_R1.inventory.CraftItemStack")
                        .getDeclaredMethod("asCraftMirror", Class.forName("net.minecraft.world.item.ItemStack"));
                ITEMSTACK_AS_CRAFT_MIRROR_METHOD.setAccessible(true);

                ENTITY_HUMAN_INVENTORY_FIELD = entityHumanClass.getDeclaredField("cp"); // TODO update to newest version
                ENTITY_HUMAN_INVENTORY_FIELD.setAccessible(true);
                Field modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(ENTITY_HUMAN_INVENTORY_FIELD,
                        modifiersField.getInt(ENTITY_HUMAN_INVENTORY_FIELD) & ~Modifier.FINAL);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }
}
