package tr.bcxip.hummingbird.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import tr.bcxip.hummingbird.AnimeDetailsActivity;
import tr.bcxip.hummingbird.R;
import tr.bcxip.hummingbird.api.objects.Anime;

/**
 * Created by ix on 11/7/14.
 */
public class AnimeSearchAdapter extends RecyclerView.Adapter<AnimeSearchAdapter.ViewHolder> {

    private Context context;
    private List<Anime> mDataset;
    private RecyclerView mRecyclerView;

    private final View.OnClickListener mOnClickListener = new MyOnClickListener();

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView mCover;
        public TextView mTitle;
        public TextView mEpisodes;
        public TextView mType;

        public ViewHolder(View v) {
            super(v);
            mCover = (ImageView) v.findViewById(R.id.cover);
            mTitle = (TextView) v.findViewById(R.id.title);
            mEpisodes = (TextView) v.findViewById(R.id.episodes);
            mType = (TextView) v.findViewById(R.id.type);
        }
    }

    public AnimeSearchAdapter(Context context, RecyclerView recyclerView, List<Anime> dataset) {
        this.context = context;
        mDataset = dataset;
        mRecyclerView = recyclerView;
    }

    @Override
    public AnimeSearchAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_anime, parent, false);
        v.setOnClickListener(mOnClickListener);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Anime item = mDataset.get(position);

        Picasso.with(context)
                .load(item.getCoverImage())
                .into(holder.mCover);
        holder.mTitle.setText(item.getTitle());
        holder.mEpisodes.setText(item.getEpisodeCount() + "");
        holder.mType.setText(item.getShowType());
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    private class MyOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            int position = mRecyclerView.getChildPosition(view);
            Intent intent = new Intent(context, AnimeDetailsActivity.class);
            intent.putExtra(AnimeDetailsActivity.ARG_ID, mDataset.get(position).getId());
            intent.putExtra(AnimeDetailsActivity.ARG_ANIME_OBJ, mDataset.get(position));
            context.startActivity(intent);
        }
    }
}