package tr.bcxip.hummingbird.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import ca.weixiao.widget.InfiniteScrollListAdapter;
import tr.bcxip.hummingbird.AnimeDetailsActivity;
import tr.bcxip.hummingbird.ProfileActivity;
import tr.bcxip.hummingbird.R;
import tr.bcxip.hummingbird.api.objects.Story;
import tr.bcxip.hummingbird.api.objects.Substory;
import tr.bcxip.hummingbird.utils.CircleTransformation;
import tr.bcxip.hummingbird.utils.Utils;
import tr.bcxip.hummingbird.widget.RelativeTimeTextView;

/**
 * Created by ix on 11/11/14.
 */
public class FeedTimelineAdapter extends InfiniteScrollListAdapter {

    Context context;
    List<Story> mItems;

    String storyType;

    boolean[] expanded;

    NewPageListener newPageListener;

    public FeedTimelineAdapter(Context context, List<Story> items, NewPageListener listener) {
        this.context = context;
        mItems = items;
        newPageListener = listener;
        expanded = new boolean[items.size()];
    }

    private String getString(int resource) {
        return context.getString(resource);
    }


    @Override
    protected void onScrollNext() {
        if (newPageListener != null) newPageListener.onScrollNext();
    }

    @Override
    public View getInfiniteScrollListView(final int position, View convertView, ViewGroup parent) {
        if (getCount() != 0) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View rootView = new View(context);

            final Story item = mItems.get(position);
            storyType = item.getStoryType();

            if (storyType.equals(Story.STORY_TYPE_COMMENT))
                rootView = inflater.inflate(R.layout.item_story_comment, null);
            else if (storyType.equals(Story.STORY_TYPE_MEDIA))
                rootView = inflater.inflate(R.layout.item_story_media, null);

            if (storyType.equals(Story.STORY_TYPE_COMMENT)) {
                FrameLayout mAvatarHolder = (FrameLayout) rootView.findViewById(R.id.item_story_comment_avatar_holder);
                ImageView mAvatar = (ImageView) rootView.findViewById(R.id.item_story_comment_avatar);
                TextView mUsername = (TextView) rootView.findViewById(R.id.item_story_comment_username);
                RelativeTimeTextView mTime = (RelativeTimeTextView) rootView.findViewById(R.id.item_story_comment_time);
                TextView mText = (TextView) rootView.findViewById(R.id.item_story_comment_text);

                mAvatarHolder.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(context, ProfileActivity.class);
                        intent.putExtra(ProfileActivity.ARG_USERNAME, item.getPoster().getName());
                        context.startActivity(intent);
                    }
                });

            /* The following 2 won't be used until we find out how to obtain the necessary data */
                View mDivider = rootView.findViewById(R.id.item_story_comment_divider);
                LinearLayout mComments = (LinearLayout) rootView.findViewById(R.id.item_story_comment_comments);

                Picasso.with(context)
                        .load(item.getPoster().getAvatar())
                        .transform(new CircleTransformation())
                        .into(mAvatar);

                mUsername.setText(item.getPoster().getName());

                for (Substory substory : item.getSubstories()) {
                    if (substory.getSubstoryType().equals(Substory.SUBSTORY_TYPE_COMMENT)) {
                        mTime.setReferenceTime(substory.getCreatedAt());
                        mText.setText(substory.getComment());
                    }
                }
            }

            if (storyType.equals(Story.STORY_TYPE_MEDIA)) {
                final CardView mCard = (CardView) rootView.findViewById(R.id.item_story_media_card);
                FrameLayout mCoverHolder = (FrameLayout) rootView.findViewById(R.id.item_story_media_cover_holder);
                final ImageView mCover = (ImageView) rootView.findViewById(R.id.item_story_media_cover);
                final TextView mTitle = (TextView) rootView.findViewById(R.id.item_story_media_title);
                final LinearLayout mSubstories = (LinearLayout) rootView.findViewById(R.id.item_story_media_substories);
                final TextView mMoreIndicator = (TextView) rootView.findViewById(R.id.item_story_media_more);

                AbsListView.LayoutParams params = new AbsListView.LayoutParams(
                        AbsListView.LayoutParams.MATCH_PARENT,
                        Utils.dpToPx(context, 160));
                rootView.setLayoutParams(params);
                expanded[position] = false;

                mCoverHolder.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(context, AnimeDetailsActivity.class);
                        intent.putExtra(AnimeDetailsActivity.ARG_ID, item.getMedia().getId());
                        intent.putExtra(AnimeDetailsActivity.ARG_ANIME_OBJ, item.getMedia());

                        ActivityOptionsCompat transition =
                                ActivityOptionsCompat.makeSceneTransitionAnimation(
                                        (Activity) context, mCover, "anime_cover");

                        Utils.startActivityWithTransition(context, intent, transition);
                    }
                });

            /* Hide the VIEW MORE button if there are no more than a single substory */
                if (item.getSubstoriesCount() == 1)
                    mMoreIndicator.setVisibility(View.GONE);

                if (item.getSubstoriesCount() > 1) {
                    rootView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (!expanded[position]) {
                                for (int i = 0; i < mSubstories.getChildCount(); i++) {
                                    View v = mSubstories.getChildAt(i);
                                    v.setVisibility(View.VISIBLE);
                                }

                                AbsListView.LayoutParams params = new AbsListView.LayoutParams(
                                        AbsListView.LayoutParams.MATCH_PARENT,
                                        AbsListView.LayoutParams.WRAP_CONTENT);
                                mCard.setLayoutParams(params);

                                expanded[position] = true;
                            } else {
                                for (int i = 0; i < mSubstories.getChildCount(); i++) {
                                    if (i > 0) {
                                        View v = mSubstories.getChildAt(i);
                                        v.setVisibility(View.GONE);
                                    }
                                }

                                AbsListView.LayoutParams params = new AbsListView.LayoutParams(
                                        AbsListView.LayoutParams.MATCH_PARENT,
                                        Utils.dpToPx(context, 160));
                                mCard.setLayoutParams(params);

                                expanded[position] = false;
                                notifyDataSetChanged();
                            }
                        }
                    });
                }

                Picasso.with(context)
                        .load(item.getMedia().getCoverImage())
                        .into(mCover);

                mTitle.setText(item.getMedia().getTitle());

                List<Substory> sortedSubstories = Utils.sortSubstoriesByDate(item.getSubstories());

                mSubstories.removeAllViews();
                for (Substory substory : sortedSubstories) {
                    View view = inflater.inflate(R.layout.item_substory, null);
                    FrameLayout avatarHolder = (FrameLayout) view.findViewById(R.id.item_substory_avatar_holder);
                    ImageView avatar = (ImageView) view.findViewById(R.id.item_substory_avatar);
                    TextView username = (TextView) view.findViewById(R.id.item_substory_username);
                    RelativeTimeTextView time = (RelativeTimeTextView) view.findViewById(R.id.item_substory_time);

                    Picasso.with(context)
                            .load(item.getUser().getAvatar())
                            .transform(new CircleTransformation())
                            .into(avatar);

                    avatarHolder.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(context, ProfileActivity.class);
                            intent.putExtra(ProfileActivity.ARG_USERNAME, item.getUser().getName());
                            context.startActivity(intent);
                        }
                    });

                    username.setText(item.getUser().getName());
                    time.setReferenceTime(substory.getCreatedAt());

                    if (substory.getSubstoryType().equals(Substory.SUBSTORY_TYPE_WATCHED_EPISODE)) {
                        String textToSet = getString(R.string.content_watched_episode)
                                + " " + substory.getEpisodeNumber();
                        ((TextView) view.findViewById(R.id.item_substory_text)).setText(textToSet);
                    }

                    if (substory.getSubstoryType().equals(Substory.SUBSTORY_TYPE_WATCHLIST_STATUS_UPDATE)) {
                        String textToSet = "";

                        String watchlistStatusUpdate = substory.getNewStatus();

                        if (watchlistStatusUpdate.equals(Substory.WATCHLIST_STATUS_CURRENTLY_WATCHING))
                            textToSet = getString(R.string.content_is_currently_watching);

                        if (watchlistStatusUpdate.equals(Substory.WATCHLIST_STATUS_COMPLETED))
                            textToSet = getString(R.string.content_has_completed);

                        if (watchlistStatusUpdate.equals(Substory.WATCHLIST_STATUS_DROPPED))
                            textToSet = getString(R.string.content_has_dropped);

                        if (watchlistStatusUpdate.equals(Substory.WATCHLIST_STATUS_ON_HOLD))
                            textToSet = getString(R.string.content_has_placed_on_hold);

                        if (watchlistStatusUpdate.equals(Substory.WATCHLIST_STATUS_PLAN_TO_WATCH))
                            textToSet = getString(R.string.content_is_planning_to_watch);

                        ((TextView) view.findViewById(R.id.item_substory_text)).setText(textToSet);
                    }

                    if (mSubstories.getChildCount() >= 1)
                        view.setVisibility(View.GONE);

                    mSubstories.addView(view);
                }
            }

            return rootView;
        } else return getInfiniteScrollListView(position, convertView, parent);
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void addEntries(List<Story> entries) {
        mItems.addAll(entries);

        List<Story> temp = new ArrayList<Story>(mItems);
        mItems.clear();
        mItems = Utils.sortStoriesByDate(temp);

        expanded = new boolean[getCount()];
        notifyDataSetChanged();
    }

    public interface NewPageListener {
        public void onScrollNext();
    }
}
