package partridge.nathan.movienight.fragments;


import android.app.AlertDialog;
import android.app.Dialog;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;

import partridge.nathan.movienight.R;

public class ErrorDialogFragment extends DialogFragment {
    public static final String ARG_MESSAGE = "message";

    private String mErrorMessage;

    public static void showError(String message, FragmentManager manager) {
        ErrorDialogFragment fragment = new ErrorDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MESSAGE,message);
        fragment.setArguments(args);
        fragment.show(manager, "error_dialog");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mErrorMessage = getArguments().getString(ARG_MESSAGE);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.error_dialog_title)
                .setMessage(mErrorMessage)
                .setPositiveButton(R.string.error_dialog_button, null)
                .create();
    }
}
