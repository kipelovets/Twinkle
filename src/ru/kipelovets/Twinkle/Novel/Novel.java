package ru.kipelovets.Twinkle.Novel;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Novel {
    private List<Stitch> stitches;
    private Stitch firstStitch;
    private String name;

    public Novel() {
        stitches = new ArrayList<>();
    }

    public List<Stitch> getStitches() {
        return stitches;
    }

    public Stitch getFirstStitch() {
        return firstStitch;
    }

    public void setFirstStitch(Stitch firstStitch) {
        this.firstStitch = firstStitch;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public JSONObject toJSON() {
        JSONObject stitchesList = new JSONObject();
        stitches.forEach(stitch -> stitchesList.put(stitch.getName(), stitch.toJSON()));

        return (new JSONObject())
                .put("name", name)
                .put("data", (new JSONObject())
                        .put("initial", firstStitch.getName())
                        .put("stitches", stitchesList)
                    )
                ;
    }
}
