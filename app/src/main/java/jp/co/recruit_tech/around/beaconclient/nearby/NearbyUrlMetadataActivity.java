package jp.co.recruit_tech.around.beaconclient.nearby;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jp.co.recruit_tech.around.beaconclient.R;
import jp.co.recruit_tech.around.beaconclient.sitedetail.SiteDetailActivity;
import jp.co.recruit_tech.around.beaconlibrary.database.dao.Transaction;
import jp.co.recruit_tech.around.beaconlibrary.database.dao.UrlMetadataDAO;
import jp.co.recruit_tech.around.beaconlibrary.metadata.UrlMetadata;
import jp.co.recruit_tech.around.beaconlibrary.service.LocationTask;

/**
 * 現在地周辺のMetadataを表示
 *
 * Created by Hideaki on 15/03/20.
 */
public class NearbyUrlMetadataActivity extends Activity {
    private static final String KEY_METADATA = "metadata";
    private static int METADATA_LIMIT = 50;

    private Location location;
    private List<UrlMetadata> metadatas = new ArrayList<UrlMetadata>();
    private HashMap<String, UrlMetadata> markerIdMetadataMap = new HashMap<String, UrlMetadata>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_url_metadata);

        GoogleMap map = getMap();
        map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                updateMetadataMarkers();
            }
        });

        location = LocationTask.getLastLocation();
        didUpdateLocation();
    }

    /**
     * Fragmentからの取得なので、安全のためこのメソッドで毎回取得する
     */
    //
    private GoogleMap getMap() {
        MapFragment fragment = (MapFragment)getFragmentManager().findFragmentById(R.id.map);
        return fragment.getMap();
    }

    private void didUpdateLocation() {
        if (location == null) return;

        updateCameraWithLocation();
    }

    private void updateCameraWithLocation() {
        if (location == null) return;

        GoogleMap map = getMap();
        LatLng center = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(center, 17.5f);
        map.addMarker(new MarkerOptions().position(center));
        map.moveCamera(cameraUpdate);
    }

    private void updateMetadataMarkers() {
        if (location == null) return;

        GoogleMap map = getMap();
        map.clear();
        markerIdMetadataMap.clear();
        final VisibleRegion region = map.getProjection().getVisibleRegion();

        Transaction<List<UrlMetadata>> transaction = new Transaction<List<UrlMetadata>>();
        metadatas = transaction.run(this, new Transaction.Runner<List<UrlMetadata>>() {
            @Override
            public List<UrlMetadata> run(SQLiteDatabase db) {
               // TODO: Geohashで検索できるようにしたい
                UrlMetadataDAO urlMetadataDAO = new UrlMetadataDAO(db);
                double left = region.latLngBounds.southwest.longitude;
                double top = region.latLngBounds.southwest.latitude;
                double right = region.latLngBounds.northeast.longitude;
                double bottom = region.latLngBounds.northeast.latitude;
                List<UrlMetadata> metadatas = urlMetadataDAO.findMetadatasInRange(top, left, bottom, right, METADATA_LIMIT);
                return metadatas;
            }
        });

        for (UrlMetadata metadata : metadatas) {
            LatLng location = new LatLng(metadata.getLatitude(), metadata.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions().position(location);
            markerOptions.title(metadata.getTitle());
            Marker marker = map.addMarker(markerOptions);
            markerIdMetadataMap.put(marker.getId(), metadata);
            map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    marker.showInfoWindow();
                    return true;
                }
            });
            map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
                    UrlMetadata metadata = markerIdMetadataMap.get(marker.getId());
                    if (metadata != null) {
                        Intent intent = SiteDetailActivity.createIntent(NearbyUrlMetadataActivity.this, metadata);
                        startActivity(intent);
                    }
                }
            });
        }
    }
}
