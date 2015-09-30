package jp.co.recruit_tech.around.beaconclient.sitedetail;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import jp.co.recruit_tech.around.beaconclient.R;
import jp.co.recruit_tech.around.beaconlibrary.metadata.UrlMetadata;

/**
 * Created by Hideaki on 15/02/24.
 */
public class MapTabFragment extends Fragment {
    private static final String ARG_METADATA = "metadata";
    private UrlMetadata metadata;

    public static MapTabFragment newInstance(UrlMetadata metadata) {
        MapTabFragment fragment = new MapTabFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_METADATA, metadata);
        fragment.setArguments(args);
        return fragment;
    }

    public MapTabFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            metadata = args.getParcelable(ARG_METADATA);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map_tab, container, false);

        MapFragment fragment = (MapFragment)getFragmentManager().findFragmentById(R.id.map);
        GoogleMap map = fragment.getMap();
        if ((metadata != null) && metadata.hasLocation()) {
            LatLng center = new LatLng(metadata.getLatitude(), metadata.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(center, 17.5f);
            map.addMarker(new MarkerOptions().position(center));
            map.moveCamera(cameraUpdate);
        }

        return view;
    }
}
