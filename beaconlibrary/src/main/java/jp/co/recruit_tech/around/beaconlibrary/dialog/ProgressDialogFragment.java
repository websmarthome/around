package jp.co.recruit_tech.around.beaconlibrary.dialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.os.Bundle;

import java.io.Serializable;

/**
 * Created by Hideaki on 15/03/04.
 */
public class ProgressDialogFragment extends DialogFragment {
    private static final String ARG_PARAMS = "params";

    private static class Params implements Serializable {
        public String title;
        public String message;
        public int style;
        public boolean isCancelable;
        public boolean isIndeterminate;

        public Params() {}
    }

    public static ProgressDialogFragment newInstance(String title, String message, int style, boolean isCancelable, boolean isIndeterminate) {
        Params params = new Params();
        params.title = title;
        params.message = message;
        params.style = style;
        params.isCancelable = isCancelable;
        params.isIndeterminate = isIndeterminate;

        ProgressDialogFragment fragment = new ProgressDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(ARG_PARAMS, params);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        Params params = (Params)args.getSerializable(ARG_PARAMS);

        ProgressDialog dialog = new ProgressDialog(getActivity());
        dialog.setTitle(params.title);
        dialog.setMessage(params.message);
        dialog.setIndeterminate(params.isIndeterminate);
        dialog.setProgressStyle(params.style);
        if (params.isCancelable) {
            dialog.setCancelable(params.isCancelable);
        }

        return dialog;
    }
}
