package net.chrotos.rpgapi.datastorage;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.NonNull;
import net.chrotos.chrotoscloud.Cloud;
import net.chrotos.chrotoscloud.games.states.CloudGameState;
import net.chrotos.chrotoscloud.games.states.GameState;
import net.chrotos.chrotoscloud.player.Player;
import net.chrotos.rpgapi.quests.QuestGraph;
import net.chrotos.rpgapi.serialization.data.ChrotosCloudSerializer;
import net.chrotos.rpgapi.serialization.data.SubjectSerializer;
import net.chrotos.rpgapi.subjects.QuestSubject;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class ChrotosCloudStore implements SubjectStorage {
    private final Gson gson = new Gson();
    private final SubjectSerializer<ChrotosCloudStore> subjectSerializer = new ChrotosCloudSerializer(this);

    @Override
    public QuestSubject getSubject(@NonNull UUID uniqueId, @NonNull QuestGraph questGraph) {
        return subjectSerializer.getSubject(uniqueId, questGraph);
    }

    @Override
    public void saveSubject(@NonNull QuestSubject questSubject) {
        subjectSerializer.saveSubject(questSubject);
    }

    public JsonObject getRaw(@NonNull UUID uniqueId) {
        AtomicReference<GameState> state = new AtomicReference<>();
        Cloud.getInstance().getPersistence().runInTransaction(databaseTransaction ->
            state.set(getGameState(uniqueId, true))
        );

        if (state.get() == null) {
            return new JsonObject();
        }

        return gson.fromJson(state.get().getState(), JsonObject.class);
    }

    public void save(@NonNull UUID uniqueId, @NonNull JsonObject object) {
        Cloud.getInstance().getPersistence().runInTransaction(databaseTransaction -> {
            GameState state = getGameState(uniqueId, true);
            if (state == null) {
                return;
            }

            state.setState(gson.toJson(object));
        });
    }

    private GameState getGameState(@NonNull UUID uniqueId, boolean orCreate) {
        Player player = Cloud.getInstance().getPlayerManager().getPlayer(uniqueId);

        if (player == null) {
            return null;
        }

        GameState gameState = player.getStates(Cloud.getInstance().getGameMode()).stream()
                .filter(state -> state.getName().equals("quests")).findFirst().orElse(null);

        if (orCreate) {
            if (gameState == null) {
                gameState = new CloudGameState(UUID.randomUUID(), "quests", Cloud.getInstance().getGameMode(), player,
                        "{}");

                player.getStates().add(gameState);
                Cloud.getInstance().getPersistence().save(player);
            }
        }

        return gameState;
    }
}
