package tr.bcxip.hummingbird;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by Hikari on 12/12/14.
 */
public class FullscreenImageActivity extends ActionBarActivity {
    public static final String ARG_IMAGE_URL = "image_url";

    public static final String TRANSITION_NAME_IMAGE = "image";

    private String mImageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int black = Color.parseColor("#000000");

        getWindow().setBackgroundDrawable(new ColorDrawable(black));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
            Transition sharedElem = TransitionInflater.from(this)
                    .inflateTransition(R.transition.move_transition);
            getWindow().setSharedElementEnterTransition(sharedElem);
            getWindow().setSharedElementExitTransition(sharedElem);
            getWindow().setStatusBarColor(black);
        }

        super.onCreate(savedInstanceState);

        ImageView mImageView = new ImageView(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            mImageView.setTransitionName(TRANSITION_NAME_IMAGE);

        setContentView(mImageView);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.getString(ARG_IMAGE_URL) != null)
            mImageUrl = bundle.getString(ARG_IMAGE_URL);

        if (mImageUrl != null) {
            Picasso.with(this)
                    .load(mImageUrl)
                    .into(mImageView);
        } else {
            Toast.makeText(this, R.string.error_cant_load_image, Toast.LENGTH_SHORT).show();
            finish();
        }

        new PhotoViewAttacher(mImageView);
    }
}
