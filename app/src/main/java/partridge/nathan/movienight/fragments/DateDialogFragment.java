package partridge.nathan.movienight.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.widget.DatePicker;

import java.util.Calendar;

import partridge.nathan.movienight.R;

import static partridge.nathan.movienight.models.ModelManager.getMovies;
import static partridge.nathan.movienight.models.ModelManager.getTvShows;


public class DateDialogFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    private static final String TAG = DateDialogFragment.class.getSimpleName();
    public static final int RELEASE_RANGE_START_DATE = 0;
    public static final int RELEASE_RANGE_END_DATE = 1;
    public static final int AIRDATE_RANGE_START_DATE = 2;
    public static final int AIRDATE_RANGE_END_DATE = 3;
    public static final String ARG_WHICH_DATE = "which_date";
    private int mWhichDate;


    public static void pickDate(int whichDate, FragmentManager manager) {
        DateDialogFragment fragment = new DateDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_WHICH_DATE, whichDate);
        fragment.setArguments(args);
        fragment.show(manager, "select_date");
    }


    private Calendar getCurrentDate() {
        switch (mWhichDate) {
            case RELEASE_RANGE_START_DATE:
                return getMovies().getFilter().getStartDate();

            case RELEASE_RANGE_END_DATE:
                return getMovies().getFilter().getEndDate();

            case AIRDATE_RANGE_START_DATE:
                return getTvShows().getFilter().getStartDate();

            case AIRDATE_RANGE_END_DATE:
                return getTvShows().getFilter().getEndDate();

            default:
                return Calendar.getInstance();
        }
    }

    private int getTitleString() {
        switch (mWhichDate) {
            case RELEASE_RANGE_START_DATE:
                return R.string.release_range_start;

            case RELEASE_RANGE_END_DATE:
                return R.string.release_range_end;

            case AIRDATE_RANGE_START_DATE:
                return R.string.airdate_range_start;

            case AIRDATE_RANGE_END_DATE:
                return R.string.airdate_range_end;

            default:
                return R.string.select_date;
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        mWhichDate = args.getInt(ARG_WHICH_DATE, 0);
        Log.w("DATE USED:", String.format("%d", mWhichDate));

        final Calendar c = getCurrentDate();   // default: current date
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        final Dialog dpd = new DatePickerDialog(getActivity(), this, year, month, day);
        dpd.setTitle(getTitleString());
        return dpd;
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        Calendar c = Calendar.getInstance();
        c.set(year, month, day);


        switch (mWhichDate) {
            case RELEASE_RANGE_START_DATE:
                getMovies().getFilter().setStartDate(c);
                break;

            case RELEASE_RANGE_END_DATE:
                getMovies().getFilter().setEndDate(c);
                break;

            case AIRDATE_RANGE_START_DATE:
                getTvShows().getFilter().setStartDate(c);
                break;

            case AIRDATE_RANGE_END_DATE:
                getTvShows().getFilter().setEndDate(c);
                break;

            default:
                Log.e(TAG, "This should never be reached. Invalid date selection.");
        }

    }
}
