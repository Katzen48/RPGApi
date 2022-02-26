package net.chrotos.rpgapi.actions;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import net.chrotos.rpgapi.subjects.QuestSubject;

@Getter
@Builder
public class Command {
    /**
     * The command, that should be executed.
     *
     * Available placeholders:
     * - %player%      - the player name
     * - %displayname% - the display name of the player
     */
    @NonNull
    private final String command;
    /**
     * If this command should be executed as the server. Defaults to true
     */
    @Builder.Default
    @Accessors(fluent = true)
    private final boolean asServer = true;

    public String format(@NonNull QuestSubject questSubject) {
        return command.replace("%player%", questSubject.getName())
                        .replace("%displayname%", questSubject.getDisplayName());
    }
}
