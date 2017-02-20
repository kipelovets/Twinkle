package ru.kipelovets.Twinkle.Novel;

import org.json.JSONObject;

public class Option {
    private Conditions ifConditions;
    private Conditions notIfConditions;
    private String option;
    private String linkPath;

    public Option(String option, String linkPath) {
        this.option = option;
        this.linkPath = linkPath;
        ifConditions = new Conditions(false);
        notIfConditions = new Conditions(true);
    }

    public void addIfCondition(String ifCondition) {
        ifConditions.add(ifCondition);
    }

    public void addNotIfCondition(String notIfCondition) {
        notIfConditions.add(notIfCondition);
    }

    public String getOption() {
        return option;
    }

    public void setOption(String option) {
        this.option = option;
    }

    public void setLinkPath(String linkPath) {
        this.linkPath = linkPath;
    }

    public JSONObject toJson() {
        JSONObject builder = new JSONObject();

        if (ifConditions.size() == 0) {
            builder.put("ifConditions", JSONObject.NULL);
        } else {
            builder.put("ifConditions", ifConditions.toJson());
        }

        if (notIfConditions.size() == 0) {
            builder.put("notIfConditions", JSONObject.NULL);
        } else {
            builder.put("notIfConditions", notIfConditions.toJson());
        }

        builder.put("linkPath", linkPath);
        builder.put("option", option);

        return builder;
    }
}
