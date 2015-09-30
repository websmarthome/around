package jp.co.recruit_tech.around.beaconclient.debug;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

import jp.co.recruit_tech.around.beaconclient.R;

/**
 * Created by MIYAMOTO, Hideaki on 15/02/16.
 */
public class ScannedBeaconHistoryActivity extends Activity {
    private ListView listView;
    private ScannedBeaconListViewAdapter listViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_scanned_beacon_history);

        listView = (ListView)findViewById(R.id.list);
        listView.setEmptyView(findViewById(R.id.empty));
        listViewAdapter = new ScannedBeaconListViewAdapter(this);
        listView.setAdapter(listViewAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        listViewAdapter.notifyDataSetChanged();
    }
}
