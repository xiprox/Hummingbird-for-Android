package tr.bcxip.hummingbird.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.squareup.picasso.Picasso;

import java.util.List;

import tr.bcxip.hummingbird.R;
import tr.bcxip.hummingbird.api.HummingbirdApi;
import tr.bcxip.hummingbird.api.objects.AnimeV2;
import tr.bcxip.hummingbird.api.objects.Favorite;

/**
 * Created by Hikari on 10/12/14.
 */
public class FavoritesAdapter extends ArrayAdapter<Favorite> {

    Context context;

    List<Favorite> mItems;

    HummingbirdApi api;

    public FavoritesAdapter(Context context, int resource, List<Favorite> list) {
        super(context, resource, list);
        this.context = context;
        mItems = list;
        api = new HummingbirdApi(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rootView = inflater.inflate(R.layout.item_favorite_grid, null);

        Favorite fav = mItems.get(position);

        ImageView mCover = (ImageView) rootView.findViewById(R.id.item_favorite_cover);
        TextView mTitle = (TextView) rootView.findViewById(R.id.item_favorite_title);
        ViewFlipper mFlipper = (ViewFlipper) rootView.findViewById(R.id.item_favorite_flipper);

        AnimeTask animeTask = new AnimeTask();
        animeTask.putViews(mCover, mTitle, mFlipper);
        animeTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                fav.getItemId());

        return rootView;
    }

    protected class AnimeTask extends AsyncTask<String, AnimeV2, AnimeV2> {

        int darkVibrantColor;

        ImageView mCover;
        TextView mTitle;
        ViewFlipper mFlipper;

        @Override
        protected AnimeV2 doInBackground(String... strings) {
            try {
                AnimeV2 anime = api.getAnime(strings[0]);
                Bitmap bitmap = Picasso.with(context)
                        .load(anime.getCoverImageLink())
                        .get();

                darkVibrantColor = Palette.generate(bitmap)
                        .getDarkMutedColor().getRgb();

                return anime;
            } catch (Exception e) {
                e.printStackTrace();
                return new AnimeV2();
            }
        }

        public void putViews(ImageView cover, TextView title, ViewFlipper flipper) {
            this.mCover = cover;
            this.mTitle = title;
            this.mFlipper = flipper;
        }

        @Override
        protected void onPostExecute(AnimeV2 anime) {
            super.onPostExecute(anime);

            Picasso.with(context)
                    .load(anime.getCoverImageLink())
                    .into(mCover);

            mTitle.setText(anime.getCanonicalTitle());
            mTitle.setBackgroundDrawable(new ColorDrawable(darkVibrantColor));

            if (mFlipper.getDisplayedChild() == 0) mFlipper.showNext();
        }
    }
}
