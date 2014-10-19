package tr.bcxip.hummingbird;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.graphics.Palette;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.manuelpeinado.fadingactionbar.extras.actionbarcompat.FadingActionBarHelper;
import com.squareup.picasso.Picasso;

import java.util.List;

import tr.bcxip.hummingbird.api.HummingbirdApi;
import tr.bcxip.hummingbird.api.objects.Story;
import tr.bcxip.hummingbird.api.objects.Substory;
import tr.bcxip.hummingbird.utils.CircleTransformation;
import tr.bcxip.hummingbird.widget.RelativeTimeTextView;

/**
 * Created by Hikari on 10/11/14.
 */
public class FeedAnimeDetails extends ActionBarActivity {

    public static final String ARG_STORY = "mStory";
    public static final String ARG_USERNAME = "username";

    HummingbirdApi api;

    ActionBar mActionBar;
    FadingActionBarHelper mActionBarHelper;

    Story mStory;
    String storyId;
    String username;

    Palette mPalette;

    Bitmap coverBitmap;

    ImageView mHeaderImage;
    LinearLayout mSubstories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        api = new HummingbirdApi(this);

        storyId = getIntent().getStringExtra(ARG_STORY);
        username = getIntent().getStringExtra(ARG_USERNAME);

        mActionBar = getSupportActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);

        new LoadTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    protected class LoadTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                List<Story> feed = api.getFeed(username);

                for (Story story : feed) {
                    if (story.getId().equals(storyId))
                        mStory = story;
                }

                if (mStory == null)
                    return false;

                coverBitmap = Picasso.with(FeedAnimeDetails.this)
                        .load(mStory.getMedia().getCoverImage())
                        .get();
                mPalette = Palette.generate(coverBitmap);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);

            if (success) {
                Palette.Swatch vibrantSwatch = mPalette.getVibrantSwatch();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    getWindow().setStatusBarColor(vibrantSwatch != null ?
                            vibrantSwatch.getRgb() : getResources().getColor(R.color.apptheme_primary));

                mActionBarHelper = new FadingActionBarHelper()
                        .actionBarBackground(vibrantSwatch == null ? new ColorDrawable(R.color.neutral)
                                : new ColorDrawable(vibrantSwatch.getRgb()))
                        .headerLayout(R.layout.header_anime_details)
                        .headerOverlayLayout(R.layout.header_overlay_anime_details)
                        .contentLayout(R.layout.content_feed_item_details);
                setContentView(mActionBarHelper.createView(FeedAnimeDetails.this));
                mActionBarHelper.initActionBar(FeedAnimeDetails.this);

                mHeaderImage = (ImageView) findViewById(R.id.anime_details_cover_image);
                mSubstories = (LinearLayout) findViewById(R.id.activity_feed_item_details_substories);

                mActionBar.setTitle(mStory.getMedia().getTitle());

                mHeaderImage.setImageBitmap(coverBitmap);

                mSubstories.removeAllViews();

                for (Substory substory : mStory.getSubstories()) {
                    View view = getLayoutInflater().inflate(R.layout.item_substory, null);

                    ImageView mAvatar = (ImageView) view.findViewById(R.id.item_substory_avatar);
                    TextView mUsername = (TextView) view.findViewById(R.id.item_substory_username);
                    TextView mText = (TextView) view.findViewById(R.id.item_substory_text);
                    RelativeTimeTextView mTime = (RelativeTimeTextView) view.findViewById(R.id.item_substory_time);

                    Picasso.with(FeedAnimeDetails.this)
                            .load(mStory.getUser().getAvatar())
                            .transform(new CircleTransformation())
                            .into(mAvatar);

                    mUsername.setText(mStory.getUser().getName());

                    if (substory.getSubstoryType().equals(Substory.SUBSTORY_TYPE_WATCHED_EPISODE)) {
                        String textToSet = getString(R.string.content_watched_episode)
                                + " " + substory.getEpisodeNumber();
                        mText.setText(textToSet);
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

                        mText.setText(textToSet);
                    }

                    mTime.setReferenceTime(substory.getCreatedAt());

                    mSubstories.addView(view);
                }

            } else {
                Toast.makeText(FeedAnimeDetails.this, R.string.error_cant_load_data, Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        super.onBackPressed();
        return true;
    }
}
