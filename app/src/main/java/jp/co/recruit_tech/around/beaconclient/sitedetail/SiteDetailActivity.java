package jp.co.recruit_tech.around.beaconclient.sitedetail;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import jp.co.recruit_tech.around.beaconclient.R;
import jp.co.recruit_tech.around.beaconlibrary.metadata.UrlMetadata;

/**
 * Created by MIYAMOTO, Hideaki on 15/02/24.
 */
public class SiteDetailActivity extends Activity {
    private static final String KEY_METADATA = "metadata";
    private static final String TAB_TAG_WEB = "web";
    private static final String TAB_TAG_MAP = "map";

    private ActionBar actionBar;
    private UrlMetadata metadata;

    public static Intent createIntent(Context context, UrlMetadata metadata) {
        Intent intent = new Intent(context, SiteDetailActivity.class);
        intent.putExtra(KEY_METADATA, metadata);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_site_detail);

        metadata = (UrlMetadata)getIntent().getParcelableExtra(KEY_METADATA);

        actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        /* アクションバーに重ねて（アクションバーを隠すように）カスタムビューを描画 */
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);

        actionBar.addTab(actionBar.newTab()
                        .setText("Web")
                        .setTabListener(new MyTabListener(TAB_TAG_WEB))
        );
        actionBar.addTab(actionBar.newTab()
                        .setText("Map")
                        .setTabListener(new MyTabListener(TAB_TAG_MAP))
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public class MyTabListener implements ActionBar.TabListener {
        private String tag;

        public MyTabListener(String tag) {
            this.tag = tag;
        }

        @Override
        public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
            FragmentManager fragmentManager = getFragmentManager();
            Fragment fragment = fragmentManager.findFragmentByTag(tag);

            if (fragment == null) {
                if (tag == TAB_TAG_WEB) {
                    fragment = WebTabFragment.newInstance(metadata);
                } else if (tag == TAB_TAG_MAP) {
                    fragment = MapTabFragment.newInstance(metadata);
                }

                if (fragment != null) {
                    fragmentManager.beginTransaction()
                            .add(R.id.tab_container, fragment, tag)
                            .commit();
                }
            } else {
                fragmentManager.beginTransaction()
                        .show(fragment)
                        .commit();
            }
        }

        @Override
        public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
            FragmentManager fragmentManager = getFragmentManager();
            Fragment fragment = fragmentManager.findFragmentByTag(tag);

            if (fragment != null) {
                fragmentManager.beginTransaction()
                        .hide(fragment)
                        .commit();
            }
        }

        @Override
        public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.right_in, R.anim.right_out);
    }
}
