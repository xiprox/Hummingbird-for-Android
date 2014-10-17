package tr.bcxip.hummingbird.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.graphics.Palette;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

import tr.bcxip.hummingbird.R;
import tr.bcxip.hummingbird.api.HummingbirdApi;
import tr.bcxip.hummingbird.api.objects.FavoriteAnime;

/**
 * Created by Hikari on 10/12/14.
 */
public class FavoriteAnimeAdapter extends ArrayAdapter<FavoriteAnime> {

    Context context;

    List<FavoriteAnime> mItems;

    HummingbirdApi api;

    public FavoriteAnimeAdapter(Context context, int resource, List<FavoriteAnime> list) {
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

        FavoriteAnime fav = mItems.get(position);

        final ImageView mCover = (ImageView) rootView.findViewById(R.id.item_favorite_cover);
        final TextView mTitle = (TextView) rootView.findViewById(R.id.item_favorite_title);
        ViewFlipper mFlipper = (ViewFlipper) rootView.findViewById(R.id.item_favorite_flipper);

        Picasso.with(context)
                .load(fav.getCoverImage())
                .into(mCover, new Callback() {
                    @Override
                    public void onSuccess() {
                        int darkMutedColor;
                        Bitmap bitmap = ((BitmapDrawable) mCover.getDrawable()).getBitmap();
                        darkMutedColor = Palette.generate(bitmap)
                                .getDarkMutedColor().getRgb();
                        mTitle.setBackgroundDrawable(new ColorDrawable(darkMutedColor));

                    }

                    @Override
                    public void onError() {

                    }
                });

        mTitle.setText(fav.getTitle());

        if (mFlipper.getDisplayedChild() == 0) mFlipper.showNext();

        return rootView;
    }
}
