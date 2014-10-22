package tr.bcxip.hummingbird.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import tr.bcxip.hummingbird.AnimeDetailsActivity;
import tr.bcxip.hummingbird.FeedAnimeDetails;
import tr.bcxip.hummingbird.R;
import tr.bcxip.hummingbird.api.objects.Story;
import tr.bcxip.hummingbird.api.objects.Substory;
import tr.bcxip.hummingbird.utils.CircleTransformation;
import tr.bcxip.hummingbird.utils.Utils;
import tr.bcxip.hummingbird.widget.RelativeTimeTextView;

/**
 * Created by Hikari on 10/11/14.
 */
public class FeedAdapter extends ArrayAdapter<Story> {

    Context context;
    List<Story> mItems;

    String storyType;

    String username;

    public FeedAdapter(Context context, int resource, List<Story> items, String username) {
        super(context, resource, items);
        this.context = context;
        mItems = items;
        this.username = username;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rootView = new View(context);

        final Story item = mItems.get(position);
        storyType = item.getStoryType();

        if (storyType.equals(Story.STORY_TYPE_COMMENT))
            rootView = inflater.inflate(R.layout.item_story_comment, null);
        else if (storyType.equals(Story.STORY_TYPE_MEDIA))
            rootView = inflater.inflate(R.layout.item_story_media, null);

        if (storyType.equals(Story.STORY_TYPE_COMMENT)) {
            ImageView mAvatar = (ImageView) rootView.findViewById(R.id.item_story_comment_avatar);
            TextView mUsername = (TextView) rootView.findViewById(R.id.item_story_comment_username);
            RelativeTimeTextView mTime = (RelativeTimeTextView) rootView.findViewById(R.id.item_story_comment_time);
            TextView mText = (TextView) rootView.findViewById(R.id.item_story_comment_text);

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
            FrameLayout mCoverHolder = (FrameLayout) rootView.findViewById(R.id.item_story_media_cover_holder);
            ImageView mCover = (ImageView) rootView.findViewById(R.id.item_story_media_cover);
            TextView mTitle = (TextView) rootView.findViewById(R.id.item_story_media_title);
            LinearLayout mSubstories = (LinearLayout) rootView.findViewById(R.id.item_story_media_substories);
            Button mViewMore = (Button) rootView.findViewById(R.id.item_story_media_view_more);

            mCoverHolder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, AnimeDetailsActivity.class);
                    intent.putExtra(AnimeDetailsActivity.ARG_ID, item.getMedia().getId());
                    context.startActivity(intent);
                }
            });

            /* Hide the VIEW MORE button if there are no more than a single substory */
            if (item.getSubstoriesCount() == 1)
                mViewMore.setVisibility(View.GONE);

            mViewMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, FeedAnimeDetails.class);
                    intent.putExtra(FeedAnimeDetails.ARG_STORY, item.getId());
                    intent.putExtra(FeedAnimeDetails.ARG_USERNAME, username);
                    context.startActivity(intent);
                }
            });

            Picasso.with(context)
                    .load(item.getMedia().getCoverImage())
                    .into(mCover);

            mTitle.setText(item.getMedia().getTitle());

            List<Substory> sortedSubstories = Utils.sortSubstoriesByDate(item.getSubstories());

            Substory substory = sortedSubstories.get(0);

            mSubstories.removeAllViews();

            View view = inflater.inflate(R.layout.item_substory, null);

            Picasso.with(context)
                    .load(item.getUser().getAvatar())
                    .transform(new CircleTransformation())
                    .into((ImageView) view.findViewById(R.id.item_substory_avatar));

            ((TextView) view.findViewById(R.id.item_substory_username))
                    .setText(item.getUser().getName());

            ((RelativeTimeTextView) view.findViewById(R.id.item_substory_time))
                    .setReferenceTime(substory.getCreatedAt());

            if (substory.getSubstoryType().equals(Substory.SUBSTORY_TYPE_WATCHED_EPISODE)) {
                String textToSet = getString(R.string.content_watched_episode)
                        + " " + substory.getEpisodeNumber();
                ((TextView) view.findViewById(R.id.item_substory_text)).setText(textToSet);
            }

            if (substory.getSubstoryType().equals(Substory.SUBSTORY_TYPE_WATCHLIST_STATUS_UPDATE)) {
                String textToSet = "";

                String watchlistStatusUpdate = substory.getNewStatus();

                if (watchlistStatusUpdate.equals(substory.WATCHLIST_STATUS_CURRENTLY_WATCHING))
                    textToSet = getString(R.string.content_is_currently_watching);

                if (watchlistStatusUpdate.equals(substory.WATCHLIST_STATUS_COMPLETED))
                    textToSet = getString(R.string.content_has_completed);

                if (watchlistStatusUpdate.equals(substory.WATCHLIST_STATUS_DROPPED))
                    textToSet = getString(R.string.content_has_dropped);

                if (watchlistStatusUpdate.equals(substory.WATCHLIST_STATUS_ON_HOLD))
                    textToSet = getString(R.string.content_has_placed_on_hold);

                if (watchlistStatusUpdate.equals(substory.WATCHLIST_STATUS_PLAN_TO_WATCH))
                    textToSet = getString(R.string.content_is_planning_to_watch);

                ((TextView) view.findViewById(R.id.item_substory_text)).setText(textToSet);
            }

            mSubstories.addView(view);
        }

        return rootView;
    }

    private String getString(int resource) {
        return context.getString(resource);
    }
}
