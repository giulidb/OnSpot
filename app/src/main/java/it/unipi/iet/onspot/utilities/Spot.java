package it.unipi.iet.onspot.utilities;

import com.google.firebase.database.IgnoreExtraProperties;

/* Java Object representing the spot in the db */
@IgnoreExtraProperties
public class Spot {

    public String userId;
    public String description;
    public String category;
    public String contentURL;
    public double Lat;
    public double Lng;
    public String time;

    public Spot() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Spot(String userId, String description, String category, String contentURL, double Lat,
                double Lng, String time) {
        this.userId = userId;
        this.description = description;
        this.category = category;
        this.contentURL = contentURL;
        this.Lat = Lat;
        this.Lng = Lng;
        this.time = time;
    }

}