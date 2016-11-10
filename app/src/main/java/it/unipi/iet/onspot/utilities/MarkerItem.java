package it.unipi.iet.onspot.utilities;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Class that Implement ClusterItem to represent a marker on the map.
 * The cluster item returns the position of the marker as a LatLng object.
 */

public class MarkerItem implements ClusterItem {
    private final LatLng mPosition;
    public final Integer icon_id;
    public final Spot spot;
    public final String spot_key;

    public MarkerItem(int icon_id,Spot spot,String spot_key) {
        mPosition = new LatLng(spot.Lat, spot.Lng);
        this.icon_id = icon_id;
        this.spot = spot;
        this.spot_key = spot_key;

    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }
}
