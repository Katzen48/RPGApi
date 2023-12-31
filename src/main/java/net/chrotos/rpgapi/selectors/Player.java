package net.chrotos.rpgapi.selectors;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import net.chrotos.rpgapi.subjects.QuestSubject;
import org.bukkit.Bukkit;

@Getter
@Builder
public class Player {
    private final String id;
    private final String name;
    private final Location location;

    public boolean applies(@NonNull QuestSubject subject) {
        org.bukkit.entity.Player player = Bukkit.getPlayer(subject.getUniqueId());

        return (id == null || id.equals(player.getUniqueId().toString())) &&
                (name == null || name.equals(player.getName())) &&
                (location == null || location.applies(player.getLocation()));
    }
}
