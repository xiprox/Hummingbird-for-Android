package tr.bcxip.hummingbird;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.support.v7.graphics.PaletteItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.manuelpeinado.fadingactionbar.FadingActionBarHelper;
import com.squareup.picasso.Picasso;

import tr.bcxip.hummingbird.api.HummingbirdApi;
import tr.bcxip.hummingbird.api.objects.AnimeV2;

/**
 * Created by Hikari on 10/8/14.
 */
public class AnimeDetailsActivity extends Activity {

    public static final String ARG_ID = "arg_id";

    ActionBar mActionBar;
    FadingActionBarHelper mActionBarHelper;

    HummingbirdApi api;

    Palette mPalette;

    Button mViewTrailer;
    Button mAddToList;
    ImageView mHeaderImage;
    TextView mType;
    TextView mGenre;
    TextView mEpisodeCount;
    TextView mEpisodeLength;
    TextView mAgeRating;
    TextView mAired;
    TextView mSynopsis;

    int ANIME_ID;

    AnimeV2 anime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActionBar = getActionBar();
        api = new HummingbirdApi(this);

        ANIME_ID = getIntent().getIntExtra(ARG_ID, 0);
        new LoadTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public boolean onNavigateUp() {
        super.onBackPressed();
        return true;
    }

    protected class LoadTask extends AsyncTask<Void, Void, Boolean> {

        Bitmap coverBitmap = null;

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                if (ANIME_ID != 0) {
                    anime = api.getAnimeById(ANIME_ID);
                    coverBitmap = Picasso.with(AnimeDetailsActivity.this)
                            .load(anime.getCoverImageLink()).get();
                    mPalette = Palette.generate(coverBitmap);
                    return true;
                } else return false;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);

            if (success) {
                PaletteItem vibrantColor = mPalette.getVibrantColor();

                mActionBarHelper = new FadingActionBarHelper()
                        .actionBarBackground(vibrantColor == null ? new ColorDrawable(R.color.neutral)
                                : new ColorDrawable(vibrantColor.getRgb()))
                        .headerLayout(R.layout.header_anime_details)
                        .headerOverlayLayout(R.layout.header_overlay_anime_details)
                        .contentLayout(R.layout.content_anime_details);
                setContentView(mActionBarHelper.createView(AnimeDetailsActivity.this));
                mActionBarHelper.initActionBar(AnimeDetailsActivity.this);

                mViewTrailer = (Button) findViewById(R.id.anime_details_view_trailer_button);
                mAddToList = (Button) findViewById(R.id.anime_details_add_to_list_button);
                mHeaderImage = (ImageView) findViewById(R.id.anime_details_cover_image);
                mType = (TextView) findViewById(R.id.anime_details_type);
                mGenre = (TextView) findViewById(R.id.anime_details_genres);
                mEpisodeCount = (TextView) findViewById(R.id.anime_details_episode_count);
                mEpisodeLength = (TextView) findViewById(R.id.anime_details_episode_duration);
                mAgeRating = (TextView) findViewById(R.id.anime_details_age_rating);
                mAired = (TextView) findViewById(R.id.anime_details_aired);
                mSynopsis = (TextView) findViewById(R.id.anime_details_synopsis);

                mViewTrailer.getBackground().setColorFilter(vibrantColor.getRgb(), PorterDuff.Mode.SRC_ATOP);
                mViewTrailer.setTextColor(vibrantColor.getRgb());
                mAddToList.getBackground().setColorFilter(vibrantColor.getRgb(), PorterDuff.Mode.SRC_ATOP);

                mViewTrailer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(
                                AnimeDetailsActivity.this,
                                "Trailer id on youtube: " + anime.getTrailer(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });

                mAddToList.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(
                                AnimeDetailsActivity.this,
                                "Adding " + anime.getCanonicalTitle() + " to list :P",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });

                Point size = new Point();
                getWindowManager().getDefaultDisplay().getSize(size);
                mHeaderImage.getLayoutParams().height = (size.y / 3) * 2;

                mHeaderImage.setImageDrawable(new BitmapDrawable(coverBitmap));

                mActionBar.setTitle(anime.getCanonicalTitle());

                mType.setText(anime.getType());

                String genres = "";
                for (int i = 0; i < anime.getGenres().size(); i++) {
                    if (i == 0)
                        genres = anime.getGenres().get(i);
                    else
                        genres += ", " + anime.getGenres().get(i);
                }
                mGenre.setText(genres);

                mEpisodeCount.setText(anime.getEpisodeCount() + "");

                mEpisodeLength.setText(anime.getEpisodeLength() + " " + getString(R.string.content_minutes));

                mAgeRating.setText(anime.getAgeRating());

                mAired.setText(anime.getAiringStartDate() + " - " + anime.getAiringFinishedDate());

                mSynopsis.setText(anime.getSynopsis());
            } else {
                Toast.makeText(AnimeDetailsActivity.this, R.string.error_cant_load_data, Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
}
