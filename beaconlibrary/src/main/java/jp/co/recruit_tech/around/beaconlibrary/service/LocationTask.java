package jp.co.recruit_tech.around.beaconlibrary.service;

import android.app.Service;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

/**
 * Created by Hideaki on 15/03/02.
 */
public class LocationTask extends Task {
    private static final int LOCATION_UPDATE_MIN_TIME = 1000;
    private static final int LOCATION_UPDATE_MIN_DISTANCE = 500;

    private Context context;
    private LocationManager locationManager;
    private static Location lastLocation = null;

    public LocationTask(Context context) {
        this.context = context;
    }

    @Override
    protected void onStart() {
        locationManager = (LocationManager)context.getSystemService(Service.LOCATION_SERVICE);
        requestLocationUpdates();
    }

    @Override
    protected void onStop() {
        locationManager.removeUpdates(locationListener);
    }

    private void requestLocationUpdates() {
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (isNetworkEnabled) {
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    LOCATION_UPDATE_MIN_TIME,
                    LOCATION_UPDATE_MIN_DISTANCE,
                    locationListener);
            lastLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        if (isGPSEnabled) {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    LOCATION_UPDATE_MIN_TIME,
                    LOCATION_UPDATE_MIN_DISTANCE,
                    locationListener);
            lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
    }

    public static Location getLastLocation() {
        return lastLocation;
    }

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            lastLocation = location;
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };
}
