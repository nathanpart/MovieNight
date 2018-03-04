package partridge.nathan.movienight.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import partridge.nathan.movienight.R;

/**
 * About dialog
 */
public class AboutDialogFragment extends DialogFragment {
    public AboutDialogFragment() {
        // Required empty public constructor
    }

    /**
     * Show the about dialog
     *
     * @param fm Fragment Manger
     */
    public static void showAboutDialog(FragmentManager fm) {
        AboutDialogFragment fragment = new AboutDialogFragment();
        fragment.show(fm, "about");
    }


    @SuppressLint("InflateParams")
    private View inflateAbout() {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        return inflater.inflate(R.layout.fragment_about_dialog, null);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        return builder.setView(inflateAbout())
                .setPositiveButton("Ok", (dialog, id)->{})
                .create();
    }
}
