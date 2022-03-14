package net.chrotos.rpgapi.criteria;

import lombok.NonNull;
import net.chrotos.rpgapi.subjects.QuestSubject;

public interface Checkable<T> {
    boolean check(@NonNull QuestSubject subject, T object);
}
