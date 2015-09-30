package jp.co.recruit_tech.around.beaconclient;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.wearable.view.CardFragment;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.support.wearable.view.GridViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import jp.co.recruit_tech.around.beaconlibrary.metadata.UrlMetadata;
import jp.co.recruit_tech.around.beaconlibrary.metadata.UrlMetadataSet;

public class MainActivity extends Activity {

    private static final String DATA_API_PATH = "/DataApiPath";
    private static final String DATA_API_METADATA_KEY = "MetadataKey";
    private static final String MESSAGE_API_START_ACTIVITY_PATH = "/StartActivity";

    private GoogleApiClient googleApiClient;
    private UrlMetadataSet urlMetadataSet;
    private Adapter adapter;
    private String alertMessage;

    /*
    private class Adapter extends WearableListView.Adapter {
        @Override
        public WearableListView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new WearableListView.ViewHolder(getLayoutInflater().inflate(R.layout.main_metadata_item, null));
        }

        @Override
        public void onBindViewHolder(WearableListView.ViewHolder holder, int position) {
            TextView title = (TextView)holder.itemView.findViewById(R.id.title);
            TextView description = (TextView)holder.itemView.findViewById(R.id.description);
            holder.itemView.setTag(position);
            if (position == 0) {
                title.setText(getString(R.string.main_beacon_count_message, urlMetadataSet.size()));
                description.setText(alertMessage);
                return;
            }
            UrlMetadata urlMetadata = urlMetadataSet.get(position - 1);
            title.setText(urlMetadata.getTitle());
            description.setText(urlMetadata.getDescription());
        }

        @Override
        public int getItemCount() {
            return urlMetadataSet.size() + 1;
        }
    }
    */

    public static class CustomCard extends CardFragment {

        private String mTitle;
        private String mDescription;
        @Override
        public View onCreateContentView(LayoutInflater inflater, ViewGroup container,
                                        Bundle savedInstanceState) {
            View root = inflater.inflate(R.layout.custom_card, container, false);
            TextView tv = (TextView)root.findViewById(R.id.card_title);
            if (mTitle != null)
                tv.setText(mTitle);
            TextView dv = (TextView)root.findViewById(R.id.card_description);
            if (mDescription != null)
                dv.setText(mDescription);
            return root;
        }

        public void setData(String title, String description) {
            mTitle = title;
            mDescription = description;
        }
    }

    private class Adapter extends FragmentGridPagerAdapter {
        public Adapter(final FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getFragment(final int row, final int column) {
            UrlMetadata data = urlMetadataSet.get(row);
            //CardFragment fragment = CardFragment.create(data.getTitle(), data.getDescription());
            CustomCard fragment = new CustomCard();
            fragment.setExpansionDirection(CardFragment.EXPAND_UP);
            fragment.setData(data.getTitle(), data.getDescription());
            return fragment;
        }

        @Override
        public int getRowCount() {
            return urlMetadataSet.size();
        }

        @Override
        public int getColumnCount(final int row) {
            return 1;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        urlMetadataSet = new UrlMetadataSet();
        alertMessage = "";
        adapter = new Adapter(getFragmentManager());

        final GridViewPager gridViewPager = (GridViewPager)findViewById(R.id.gridViewPager);
        gridViewPager.setAdapter(adapter);
        /*
        WearableListView listView = (WearableListView)findViewById(R.id.list_view);
        listView.setAdapter(adapter);
        listView.setClickListener(new WearableListView.ClickListener() {
            @Override
            public void onClick(WearableListView.ViewHolder viewHolder) {
                int position = (int)viewHolder.itemView.getTag();
                if (position == 0) {
                    return;
                }
                onItemClick(position - 1);
            }

            @Override
            public void onTopEmptyRegionClick() {
            }
        });
        */

        googleApiClient = new GoogleApiClient.Builder(this)
            .addApi(Wearable.API)
            .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                @Override
                public void onConnected(Bundle bundle) {
                    Wearable.DataApi.addListener(googleApiClient, new DataApi.DataListener() {
                        @Override
                        public void onDataChanged(DataEventBuffer dataEvents) {
                            for (DataEvent event : dataEvents) {
                                DataItem item = event.getDataItem();
                                if (!item.getUri().getPath().equals(DATA_API_PATH)) {
                                    continue;
                                }
                                DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                                MainActivity.this.onDataChanged(dataMap);
                            }
                        }
                    });
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            DataItemBuffer result = Wearable.DataApi.getDataItems(googleApiClient).await();
                            for (int i = 0; i < result.getCount(); i++) {
                                DataItem item = result.get(i);
                                if (!item.getUri().getPath().equals(DATA_API_PATH)) {
                                    continue;
                                }
                                DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                                MainActivity.this.onDataChanged(dataMap);
                            }
                        }
                    }).start();
                }

                @Override
                public void onConnectionSuspended(int i) {
                }
            }).addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                @Override
                public void onConnectionFailed(ConnectionResult connectionResult) {
                    MainActivity.this.onConnectionFailed();
                }
            }).build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }

    private void notifyDataSetChanged() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void onConnectionFailed() {
        alertMessage = getString(R.string.main_pairing_disconnect_message);
        notifyDataSetChanged();
    }

    private void onDataChanged(DataMap dataMap) {
        try {
            urlMetadataSet = UrlMetadataSet.unjsonizer.fromJSONObject(new JSONObject(dataMap.getString(DATA_API_METADATA_KEY)));
        } catch (JSONException e) {
            // ここには来ないはず
            throw new IllegalStateException();
        }
        alertMessage = "";
        notifyDataSetChanged();
    }

    private void onItemClick(int position) {
        final UrlMetadata item = urlMetadataSet.get(position);
        new Thread(new Runnable() {
            @Override
            public void run() {
                NodeApi.GetConnectedNodesResult getNodeListResult = Wearable.NodeApi.getConnectedNodes(googleApiClient).await();
                List<Node> list = getNodeListResult.getNodes();
                if (list.isEmpty()) {
                    return;
                }
                if (list.size() != 1) {
                    // ここには来ないはず
                    throw new IllegalStateException();
                }
                Wearable.MessageApi.sendMessage(googleApiClient, list.get(0).getId(), MESSAGE_API_START_ACTIVITY_PATH, item.getUrl().getBytes());
            }
        }).start();
    }
}
