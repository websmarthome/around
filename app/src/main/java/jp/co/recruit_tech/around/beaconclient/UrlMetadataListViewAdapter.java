package jp.co.recruit_tech.around.beaconclient;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import jp.co.recruit_tech.around.beaconclient.utils.AppUtils;
import jp.co.recruit_tech.around.beaconclient.utils.BitmapCache;
import jp.co.recruit_tech.around.beaconclient.view.IconImageView;
import jp.co.recruit_tech.around.beaconlibrary.database.dao.Transaction;
import jp.co.recruit_tech.around.beaconlibrary.database.dao.UrlMetadataDAO;
import jp.co.recruit_tech.around.beaconlibrary.metadata.AndroidAppLink;
import jp.co.recruit_tech.around.beaconlibrary.metadata.UrlMetadata;
import jp.co.recruit_tech.around.beaconlibrary.metadata.UrlMetadataSet;

/**
 * Created by Hideaki on 15/01/30.
 */
public class UrlMetadataListViewAdapter extends BaseAdapter implements Filterable {
    private RequestQueue requestQueue;
    private Context context;
    private UrlMetadataSet metadataSet;
    private UrlMetadataSet filteredMetadataSet;
    private ImageLoader imageLoader;

    public UrlMetadataListViewAdapter(Context context, RequestQueue requestQueue) {
        this.context = context;
        this.requestQueue = requestQueue;
        metadataSet = new UrlMetadataSet();
        imageLoader = new ImageLoader(requestQueue, new BitmapCache());
        //
        // 全メタデータ読み込み
        //
        Transaction<Void> transaction = new Transaction<Void>();
        transaction.run(context, new Transaction.Runner<Void>() {
            @Override
            public Void run(SQLiteDatabase db) {
                UrlMetadataDAO dao = new UrlMetadataDAO(db);
                List<UrlMetadata> metadataList = dao.findAll();
                for (UrlMetadata metadata : metadataList) {
                    metadataSet.upsert(metadata);
                }
                return null;
            }
        });
    }

    private UrlMetadataSet getCurrentMetadataSet() {
        return filteredMetadataSet != null ? filteredMetadataSet : metadataSet;
    }

    public void upsertMetadata(UrlMetadata metadata) {
        metadataSet.upsert(metadata);
        metadataSet.sortByScannedAt();
    }

    public void upsertMetadatas(UrlMetadataSet metadataSet) {
        for (int i = 0; i < metadataSet.size(); i++) {
            UrlMetadata metadata = metadataSet.get(i);
            this.metadataSet.upsert(metadata);
        }
        this.metadataSet.sortByScannedAt();
    }

    @Override
    public int getCount() {
        return getCurrentMetadataSet().size();
    }

    @Override
    public UrlMetadata getItem(int i) {
        return getCurrentMetadataSet().get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = ((LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.url_metadata_list_item, viewGroup, false);
            Button openAppButton = (Button)view.findViewById(R.id.open_app_button);
            Button openStoreButton = (Button)view.findViewById(R.id.open_store_button);
            openAppButton.setOnClickListener(openAppButtonClickListener);
            openStoreButton.setOnClickListener(openStoreButtonClickListener);
        }

        final UrlMetadata metadata = getCurrentMetadataSet().get(i);

        TextView titleView = (TextView)view.findViewById(R.id.title_view);
        if (metadata.getTitle().length() > 0) {
            titleView.setText(metadata.getTitle());
        } else {
            titleView.setText(metadata.getUrl());
        }

        TextView descView = (TextView)view.findViewById(R.id.description_view);
        if (metadata.getDescription().length() > 0) {
            descView.setText(metadata.getDescription());
        } else {
            descView.setText(metadata.getUrl());
        }

        String scannedAtString = displayStringFromDate(metadata.getLastScannedAt());
        TextView scannedAtView = (TextView)view.findViewById(R.id.scanned_at_view);
        scannedAtView.setText(scannedAtString);

        IconImageView iconView = (IconImageView)view.findViewById(R.id.icon_view);
        if (metadata.getIconUrl() != null) {
            iconView.loadImageUrl(metadata.getIconUrl(), null, imageLoader);
        } else {
            iconView.setImageBitmap(null);
        }

        if (i % 2 == 0) {
            view.setBackgroundColor(Color.parseColor("#ffffff"));
        } else {
            view.setBackgroundColor(Color.parseColor("#eeeeee"));
        }

        Button openAppButton = (Button)view.findViewById(R.id.open_app_button);
        Button openStoreButton = (Button)view.findViewById(R.id.open_store_button);
        openAppButton.setTag(null);
        openStoreButton.setTag(null);
        if (metadata.getAndroidAppLinks().size() > 0) {
            AndroidAppLink appLink = metadata.getAndroidAppLinks().get(0);
            openAppButton.setTag(appLink);
            openStoreButton.setTag(appLink);
            if (AppUtils.canOpenExternalApp(context, appLink.getPackageName())) {
                openAppButton.setVisibility(View.VISIBLE);
                openStoreButton.setVisibility(View.GONE);
            } else {
                openAppButton.setVisibility(View.GONE);
                openStoreButton.setVisibility(View.VISIBLE);
            }
        } else {
            openAppButton.setVisibility(View.GONE);
            openStoreButton.setVisibility(View.GONE);
        }

        return view;
    }

    private Filter searchFilter = new Filter() {
        @Override
        protected Filter.FilterResults performFiltering(CharSequence constraint) {
            final Filter.FilterResults results = new Filter.FilterResults();
            if (constraint != null && constraint.length() > 0) {
                UrlMetadataSet matched = metadataSet.query(constraint.toString());
                results.values = matched;
            } else {
                results.values = null;
            }
            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint,
                Filter.FilterResults results) {
            filteredMetadataSet = (UrlMetadataSet)results.values;
            notifyDataSetChanged();
        }
    };

    @Override
    public Filter getFilter() {
        return searchFilter;
    }

    public void clear() {
        metadataSet.clear();
        notifyDataSetChanged();
    }

    private String displayStringFromDate(Date date) {
        //DateFormat format = SimpleDateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG);
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        return format.format(date);
    }

    private Button.OnClickListener openAppButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            AndroidAppLink appLink = (AndroidAppLink)view.getTag();
            AppUtils.openExternalApp(context, appLink.getPackageName());
        }
    };

    private Button.OnClickListener openStoreButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            AndroidAppLink appLink = (AndroidAppLink)view.getTag();
            AppUtils.openGooglePlayStore(context, appLink.getPackageName());
        }
    };
}
