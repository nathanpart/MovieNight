package partridge.nathan.movienight.fragments;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import partridge.nathan.movienight.activities.ShowError;
import partridge.nathan.movienight.R;
import partridge.nathan.movienight.models.ListModel;
import partridge.nathan.movienight.models.MovieItem;

import static partridge.nathan.movienight.models.ModelManager.findModelByName;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnShowSelectedListener}
 * interface.
 */
public class MediaListFragment extends Fragment
        implements AdapterView.OnItemSelectedListener, ListModel.DataChangeNotification {

    private static final String MODEL_ID = "model-string";
    private OnShowSelectedListener mListener;
    private ProgressBar mProgressBar;
    private ListModel mModel;
    private MyMediaListRecyclerViewAdapter mRecyclerViewAdapter;
    private TextView mPagingText;

    private RelativeLayout mPaging;
    private LinearLayout mSortOrder;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MediaListFragment() {
    }


    // Factory to create this fragment
    public static MediaListFragment newInstance(String modelName) {
        MediaListFragment fragment = new MediaListFragment();
        Bundle args = new Bundle();
        args.putString(MODEL_ID, modelName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String modelName = null;
        mModel = null;

        if (getArguments() != null) {
            modelName = getArguments().getString(MODEL_ID);
        }

        if (modelName != null) {
            mModel = findModelByName(modelName);
        }

        if (mModel == null) {
            throw new RuntimeException("Containing Activity/Fragment must pass model id name as a" +
                    " string argument.");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_medialist_list, container, false);

        // Setup the progress bar
        mProgressBar = view.findViewById(R.id.list_progress);
        mProgressBar.setVisibility(View.GONE);

        mPaging = view.findViewById(R.id.paging_layout);
        mSortOrder = view.findViewById(R.id.sort_order_selection);

        // Setup Sort order spinner
        Spinner sortOrder = view.findViewById(R.id.sort_by);
        sortOrder.getBackground().setColorFilter(getResources().getColor(R.color.background_highlight), PorterDuff.Mode.DST_OVER);
        sortOrder.getBackground().setColorFilter(getResources().getColor(R.color.background_highlight), PorterDuff.Mode.SRC_ATOP);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(view.getContext(),
                mModel.getSortOrderList(), android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortOrder.setAdapter(adapter);
        sortOrder.setSelection(mModel.getListOrder().getCurrentSortBy());
        sortOrder.setOnItemSelectedListener(this);

        // Setup sort direction button
        int button_image = mModel.getListOrder().isAscendingSort() ? R.drawable.sort_ascending : R.drawable.sort_descending;
        ImageButton imageButton = view.findViewById(R.id.sort_direction);
        imageButton.setImageDrawable(view.getContext().getResources().getDrawable(button_image));
        imageButton.setOnClickListener((v)->{
            mModel.getListOrder().toggleSortDirection();
            int i = mModel.getListOrder().isAscendingSort() ? R.drawable.sort_ascending : R.drawable.sort_descending;
            ((ImageButton) v).setImageDrawable(v.getContext().getResources().getDrawable(i));
        });

        // Set page navigation
        mPagingText = view.findViewById(R.id.paging_text);
        updatePaging();

        imageButton = view.findViewById(R.id.fisrt_page);
        imageButton.setOnClickListener((v)->mModel.getPaging().gotoFirstPage());
        imageButton = view.findViewById(R.id.prev_page);
        imageButton.setOnClickListener((v)->mModel.getPaging().gotoPrevPage());
        imageButton = view.findViewById(R.id.next_page);
        imageButton.setOnClickListener((v)->mModel.getPaging().gotoNextPage());
        imageButton = view.findViewById(R.id.imageButton4);
        imageButton.setOnClickListener((v)->mModel.getPaging().gotoLastPage());


        // Set list main background color
        int backgroundColor;
        if (mModel.getModelType().equals(MovieItem.class.getSimpleName())) {
            backgroundColor = getResources().getColor(R.color.movie_background);
        } else {
            backgroundColor = getResources().getColor(R.color.tv_background);
        }
        view.setBackgroundColor(backgroundColor);

        // Set the adapter
        RecyclerView recyclerView = view.findViewById(R.id.list);
//        recyclerView.setBackgroundColor(backgroundColor);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        mRecyclerViewAdapter = new MyMediaListRecyclerViewAdapter(mModel, view.getContext(), mListener);
        recyclerView.setAdapter(mRecyclerViewAdapter);

        // Attach ourselves to our data model
        mModel.attachViewer(mModel.getClass().getSimpleName(), this);

        return view;
    }

    private void updatePaging() {
        mPagingText.setText(String.format(getString(R.string.paging_text),
                mModel.getPaging().getCurrentPage(),
                mModel.getPaging().getNumberOfPages()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mModel.detachView(mModel.getClass().getSimpleName());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnShowSelectedListener) {
            mListener = (OnShowSelectedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        mModel.getListOrder().setCurrentSortBy(position);

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        // If this ever gets called we select the first order option by default
        adapterView.setSelection(0);
        mModel.getListOrder().setCurrentSortBy(0);
    }

    @Override
    public void onDataChange() {
        mSortOrder.setVisibility(View.VISIBLE);
        mPaging.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
        updatePaging();
        mRecyclerViewAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDataError(String message) {
        mSortOrder.setVisibility(View.VISIBLE);
        mPaging.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
        ((ShowError) getActivity()).showErrorDialog(message);
    }

    @Override
    public void onUpdateStarted() {
        mSortOrder.setVisibility(View.GONE);
        mPaging.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    /**
     * This interface must be implemented by the activities/fragment that contain this
     * fragment.  It is used to inform of a selected show and the containing activity/fragment
     * can use the passed item index to show dialog showing the details of the selected show.
     */
    public interface OnShowSelectedListener {
        void onShowSelected(int item, ListModel model);
    }
}
