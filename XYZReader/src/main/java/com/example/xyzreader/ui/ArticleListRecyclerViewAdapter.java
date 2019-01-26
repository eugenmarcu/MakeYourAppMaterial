package com.example.xyzreader.ui;

import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

class ArticleListRecyclerViewAdapter extends RecyclerView.Adapter<ArticleListActivity.ViewHolder> {
    private static final String TAG = ArticleListRecyclerViewAdapter.class.getName();
    private ArticleListActivity articleListActivity;
    private Cursor mCursor;
    private int mPosition;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");
    // Use default locale format
    private SimpleDateFormat outputFormat = new SimpleDateFormat();
    // Most time functions can only handle 1902 - 2037
    private GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2, 1, 1);

    public ArticleListRecyclerViewAdapter(ArticleListActivity articleListActivity, Cursor cursor) {
        this.articleListActivity = articleListActivity;
        mCursor = cursor;
    }

    @Override
    public long getItemId(int position) {
        mCursor.moveToPosition(position);
        return mCursor.getLong(ArticleLoader.Query._ID);
    }

    @Override
    public ArticleListActivity.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = articleListActivity.getLayoutInflater().inflate(R.layout.list_item_article, parent, false);
        final ArticleListActivity.ViewHolder vh = new ArticleListActivity.ViewHolder(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPosition = vh.getAdapterPosition();
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        ItemsContract.Items.buildItemUri(getItemId(vh.getAdapterPosition())));
                intent.putExtra(ArticleListActivity.TRANSITION_POSITION_START, mPosition);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    Bundle bundle = ActivityOptionsCompat
                            .makeSceneTransitionAnimation(
                                    articleListActivity,
                                    vh.thumbnailView,
                                    vh.thumbnailView.getTransitionName()).toBundle();

                    Log.d(TAG, "GET transition NameId: " + mPosition);
                    articleListActivity.startActivity(intent, bundle);
                } else {
                    articleListActivity.startActivity(intent);
                }
            }
        });
        return vh;
    }

    private Date parsePublishedDate() {
        try {
            String date = mCursor.getString(ArticleLoader.Query.PUBLISHED_DATE);
            return dateFormat.parse(date);
        } catch (ParseException ex) {
            Log.e(TAG, ex.getMessage());
            Log.i(TAG, "passing today's date");
            return new Date();
        }
    }

    @Override
    public void onBindViewHolder(ArticleListActivity.ViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        mPosition = position;
        holder.titleView.setText(mCursor.getString(ArticleLoader.Query.TITLE));
        Date publishedDate = parsePublishedDate();
        if (!publishedDate.before(START_OF_EPOCH.getTime())) {

            holder.subtitleView.setText(Html.fromHtml(
                    DateUtils.getRelativeTimeSpanString(
                            publishedDate.getTime(),
                            System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                            DateUtils.FORMAT_ABBREV_ALL).toString()
                            + "<br/>" + " by "
                            + mCursor.getString(ArticleLoader.Query.AUTHOR)));
        } else {
            holder.subtitleView.setText(Html.fromHtml(
                    outputFormat.format(publishedDate)
                            + "<br/>" + " by "
                            + mCursor.getString(ArticleLoader.Query.AUTHOR)));
        }
        holder.thumbnailView.setImageUrl(
                mCursor.getString(ArticleLoader.Query.THUMB_URL),
                ImageLoaderHelper.getInstance(articleListActivity).getImageLoader());
        holder.thumbnailView.setAspectRatio(mCursor.getFloat(ArticleLoader.Query.ASPECT_RATIO));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            holder.thumbnailView.setTransitionName(articleListActivity.getString(R.string.transition_picture) + mPosition);
            holder.thumbnailView.setTag(articleListActivity.getString(R.string.transition_picture) + mPosition);
        }
    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }
}
