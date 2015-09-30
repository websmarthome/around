package jp.co.recruit_tech.around.beaconclient.debug;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.DateFormat;

import jp.co.recruit_tech.around.beaconlibrary.debug.ScannedBeacon;
import jp.co.recruit_tech.around.beaconlibrary.debug.ScannedBeaconHistory;

/**
 * Created by MIYAMOTO, Hideaki on 15/01/28.
 */
public class ScannedBeaconListViewAdapter extends BaseAdapter {
    private Context context;
    private ScannedBeaconHistory history;

    public ScannedBeaconListViewAdapter(Context context) {
        this.context = context;
        this.history = ScannedBeaconHistory.getInstance(context);
    }

    @Override
    public int getCount() {
        return history.size();
    }

    @Override
    public ScannedBeacon getItem(int i) {
        return history.getAt(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = ((LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(android.R.layout.simple_list_item_2, viewGroup, false);
        }

        ScannedBeacon scannedBeacon = history.getAt(i);

        TextView textView = (TextView)view.findViewById(android.R.id.text1);
        textView.setText(scannedBeacon.getBeacon().getUriString());

        String datetimeString = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT).format(scannedBeacon.getTimestamp());
        textView = (TextView)view.findViewById(android.R.id.text2);
        textView.setText(datetimeString);

        return view;
    }

    public void clear() {
        history.clear();
        notifyDataSetChanged();
    }
}
