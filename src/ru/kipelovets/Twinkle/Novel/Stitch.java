package ru.kipelovets.Twinkle.Novel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Stitch {
    private String text;
    private List<Option> options;
    private List<String> flags;
    private Conditions ifConditions;
    private Conditions notIfConditions;
    private String divert;
    private String image;
    private String name;

    public Stitch(String text) {
        options = new ArrayList<>();
        flags = new ArrayList<>();
        ifConditions = new Conditions(false);
        notIfConditions = new Conditions(true);
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addOption(Option option) {
        options.add(option);
    }

    public void addFlag(String flag) {
        flags.add(flag);
    }

    public void setDivert(String divert) {
        this.divert = divert;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Conditions getIfConditions() {
        return ifConditions;
    }

    public Conditions getNotIfConditions() {
        return notIfConditions;
    }

    public List<Option> getOptions() {
        return options;
    }

    public String getDivert() {
        return divert;
    }

    public JSONObject toJSON() {
        JSONArray content = new JSONArray();
        content.put(text);
        options.forEach(o -> content.put(o.toJson()));
        flags.forEach(f -> content.put((new JSONObject())
                .put("flagName", f)
        ));
        if (ifConditions.size() > 0) {
            ifConditions.toJson().forEach(content::put);
        }
        if (notIfConditions.size() > 0) {
            notIfConditions.toJson().forEach(content::put);
        }
        if (divert != null) {
            content.put((new JSONObject()).put("divert", divert));
        }
        if (image != null) {
            content.put((new JSONObject()).put("image", image));
        }

        return (new JSONObject())
                .put("content", content)
            ;
    }
}
