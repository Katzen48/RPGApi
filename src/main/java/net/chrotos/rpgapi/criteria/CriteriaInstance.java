package net.chrotos.rpgapi.criteria;

import com.google.gson.JsonObject;

public interface CriteriaInstance<T, A extends Criteria<T, A>> {
    A getCriteria();
    JsonObject serialize();
}
