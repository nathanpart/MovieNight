package partridge.nathan.movienight.fragments;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.Locale;

import partridge.nathan.movienight.fragments.MediaListFragment.OnShowSelectedListener;
import partridge.nathan.movienight.R;
import partridge.nathan.movienight.models.ListModel;
import partridge.nathan.movienight.models.ListItem;
import partridge.nathan.movienight.models.MovieItem;

public class MyMediaListRecyclerViewAdapter extends RecyclerView.Adapter<MyMediaListRecyclerViewAdapter.ViewHolder> {

    private final ListModel mModel;
    private final Context mContext;
    private final OnShowSelectedListener mListener;

    MyMediaListRecyclerViewAdapter(ListModel model, Context context,
                                          OnShowSelectedListener listener) {
        mModel = model;
        mContext = context;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_medialist, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        ListItem item = mModel.getListItem(position);

        // Highlight every other row
        holder.mView.setBackgroundColor(mContext.getResources().getColor(R.color.background_highlight));

        holder.mTitle.setText(item.getTitle());
        holder.mRating.setRating(item.getRating());
        holder.mVoteCount.setText(String.format(Locale.US,"%d", item.getVoteCount()));
        holder.mPopularity.setText(String.format(Locale.US,"%.2f", item.getPopularity()));
        holder.mDateLabel.setText(item instanceof MovieItem ?
                mContext.getString(R.string.release_date) :
                mContext.getString(R.string.airdate));
        holder.mDate.setText(item.getDate());

        if (item instanceof MovieItem) {
            holder.mRevenueLabel.setVisibility(View.VISIBLE);
            holder.mRevenue.setVisibility(View.VISIBLE);
            holder.mRevenue.setText(String.format(Locale.US,"$%,d", ((MovieItem) item).getRevenue()));
        } else {
            holder.mRevenueLabel.setVisibility(View.GONE);
            holder.mRevenue.setVisibility(View.GONE);
        }

        holder.mView.setOnClickListener(v -> {
            if (null != mListener) {
                // Notify the active callbacks interface (the activity, if the
                // fragment is attached to one) that an item has been selected.
                mListener.onShowSelected(holder.getAdapterPosition(), mModel);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mModel.getListSize();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView mTitle;
        final RatingBar mRating;
        final TextView mVoteCount;
        final TextView mPopularity;
        final TextView mDate;
        final TextView mDateLabel;
        final TextView mRevenue;
        final TextView mRevenueLabel;

        ViewHolder(View view) {
            super(view);
            mView = view;
            mTitle = view.findViewById(R.id.title);
            mRating = view.findViewById(R.id.ratingBar);
            mVoteCount = view.findViewById(R.id.vote_count);
            mPopularity = view.findViewById(R.id.popularity);
            mRevenue = view.findViewById(R.id.revenue);
            mDate = view.findViewById(R.id.date);
            mDateLabel = view.findViewById(R.id.date_label);
            mRevenueLabel = view.findViewById(R.id.revenue_label);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mTitle.getText() + "'";
        }
    }
}
