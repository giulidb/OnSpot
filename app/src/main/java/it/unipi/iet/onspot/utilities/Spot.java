package it.unipi.iet.onspot.utilities;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

/* Java Object representing the spot in the db */
@IgnoreExtraProperties
public class Spot {

    public String userId;
    public String description;
    public String title;
    public String category;
    public String contentURL;
    public double Lat;
    public double Lng;
    public String time;
    public String Type;
    public int heartCount = 0;
    public Map<String, Boolean> hearts = new HashMap<>();

    public Spot() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Spot(String userId, String description,String title, String category, String contentURL, double Lat,
                double Lng, String time, String Type) {
        this.userId = userId;
        this.description = description;
        this.title = title;
        this.category = category;
        this.contentURL = contentURL;
        this.Lat = Lat;
        this.Lng = Lng;
        this.time = time;
        this.Type = Type;
    }

}