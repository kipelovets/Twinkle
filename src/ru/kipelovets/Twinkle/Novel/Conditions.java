package ru.kipelovets.Twinkle.Novel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Conditions {
    private List<String> conditions;
    private Boolean notIfConditions;

    public Conditions(Boolean notIfConditions) {
        this.conditions = new ArrayList<>();
        this.notIfConditions = notIfConditions;
    }

    public void add(String condition) {
        conditions.add(condition);
    }

    public String get(int index) {
        return conditions.get(index);
    }

    public Integer size() {
        return conditions.size();
    }

    public JSONArray toJson() {
        JSONArray ifConditionsBuilder = new JSONArray();
        conditions.forEach(s -> ifConditionsBuilder.put(
                (new JSONObject())
                        .put(notIfConditions ? "notIfCondition" : "ifCondition", s)
        ));

        return ifConditionsBuilder;
    }
}
