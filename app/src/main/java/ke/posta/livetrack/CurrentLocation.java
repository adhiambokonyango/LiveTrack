package ke.posta.livetrack;

public class CurrentLocation {
    private double lat;
    private  double lng;


    public CurrentLocation(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public CurrentLocation() {
    }

    /**
     user_101
     accuracy:
     32.15999984741211
     altitude:
     1683.06982421875
     bearing:
     0
     bearingAccuracyDegrees:
     0
     complete:
     true
     elapsedRealtimeNanos:
     571827286110704
     elapsedRealtimeUncertaintyNanos:
     0
     extras
     fromMockProvider:
     false
     latitude:
     -1.2864237
     longitude:
     36.81905197
     provider:
     "gps"
     speed:
     0
     speedAccuracyMetersPerSecond:
     1.7336666584014893
     time:
     1644915401000
     verticalAccuracyMeters:
     16
     **/





    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }
}
