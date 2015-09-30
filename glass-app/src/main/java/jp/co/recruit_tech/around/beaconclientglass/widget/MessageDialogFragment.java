package jp.co.recruit_tech.around.beaconclientglass.widget;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.glass.widget.CardBuilder;

import jp.co.recruit_tech.around.beaconclientglass.R;

/**
 * Created by kusakabe on 15/02/17.
 */
public class MessageDialogFragment extends DialogFragment {

    private static final String TAG = "jp.co.recruit_tech.around.beaconclientglass.widget.MessageDialogFragment";
    private static final String KEY_MESSAGE = "message";
    private static final String KEY_ICON_RES_ID = "iconResId";
    private static final String KEY_DURATION_MILLIS = "durationMillis";

    private String message;
    private Integer iconResId;
    private int durationMillis;

    public MessageDialogFragment() {
        super();
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.ContextualDialogTheme);
    }

    public static MessageDialogFragment newInstance(String message, Integer iconResId, int durationMillis) {
        MessageDialogFragment fragment = new MessageDialogFragment();
        Bundle args = new Bundle();
        args.putString(KEY_MESSAGE, message);
        if (iconResId != null) {
            args.putInt(KEY_ICON_RES_ID, iconResId);
        }
        args.putInt(KEY_DURATION_MILLIS, durationMillis);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        message = args.getString(KEY_MESSAGE);
        if (args.containsKey(KEY_ICON_RES_ID)) {
            iconResId = args.getInt(KEY_ICON_RES_ID);
        }
        durationMillis = args.getInt(KEY_DURATION_MILLIS);
        setCancelable(false);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        CardBuilder cardBuilder = new CardBuilder(getActivity(), CardBuilder.Layout.MENU);
        cardBuilder.setText(message);
        if (iconResId != null) {
            cardBuilder.setIcon(iconResId);
        }
        return cardBuilder.getView();
    }

    @Override
    public void onStart() {
        super.onStart();
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                dismiss();
            }
        }, durationMillis);
    }

    public static void show(FragmentManager manager, String message, Integer iconResId, int durationMillis) {
        if (manager.findFragmentByTag(TAG) != null) {
            return;
        }
        MessageDialogFragment dialog = newInstance(message, iconResId, durationMillis);
        dialog.show(manager, TAG);
    }
}
