package it.unipi.iet.onspot.utilities;

import android.net.Uri;

import com.google.firebase.database.IgnoreExtraProperties;



/* Java Object represent the user in the db */
@IgnoreExtraProperties
public class Spot {

    public String userId;
    public String description;
    public double Lat;
    public double Lng;
    public String time;

    public Spot() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Spot(String userId, String description, double Lat, double Lng, String time) {
        this.userId = userId;
        this.description = description;
        this.Lat = Lat;
        this.Lng = Lng;
        this.time = time;
    }

}