package jp.co.recruit_tech.around.beaconclient.sitedetail;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import jp.co.recruit_tech.around.beaconclient.R;
import jp.co.recruit_tech.around.beaconlibrary.dialog.ProgressDialogFragment;
import jp.co.recruit_tech.around.beaconlibrary.metadata.UrlMetadata;

public class WebTabFragment extends Fragment {
    private static final String ARG_METADATA = "metadata";
    private static final String TAG_PROGRESS_DIALOG = "progressDialog";

    private WebView webView;
    private boolean isLoadedUrl;
    private UrlMetadata metadata;
    private ProgressDialogFragment dialogFragment;

    public static WebTabFragment newInstance(UrlMetadata metadata) {
        WebTabFragment fragment = new WebTabFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_METADATA, metadata);
        fragment.setArguments(args);
        return fragment;
    }

    public WebTabFragment() {
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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_web_tab, container, false);

        webView = (WebView)view.findViewById(R.id.web_view);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                FragmentManager fragmentManager = getFragmentManager();
                if (fragmentManager == null) return;

                ProgressDialogFragment dialog = (ProgressDialogFragment)fragmentManager.findFragmentByTag(TAG_PROGRESS_DIALOG);
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (! isLoadedUrl) {
            ProgressDialogFragment dialog = ProgressDialogFragment.newInstance("", "Loading", ProgressDialog.STYLE_SPINNER, false, true);
            dialog.show(getFragmentManager(), TAG_PROGRESS_DIALOG);

            webView.loadUrl(metadata.getUrl());
            isLoadedUrl = true;
        }
    }
}
