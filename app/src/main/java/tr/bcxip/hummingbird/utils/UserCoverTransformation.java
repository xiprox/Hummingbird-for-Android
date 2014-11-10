package tr.bcxip.hummingbird.utils;

import android.graphics.Bitmap;

import com.squareup.picasso.Transformation;
/**
 * Created by ix on 11/10/14.
 */
public class UserCoverTransformation implements Transformation {

    @Override
    public Bitmap transform(Bitmap source) {
        Bitmap newCover;

        int MAX_WIDTH = 2048;
        int width = source.getWidth();
        int height = source.getHeight();

        if (width > MAX_WIDTH) {
            float ratio = (float) width / MAX_WIDTH;
            width = MAX_WIDTH;
            height = (int) (height / ratio);

            newCover = Bitmap.createScaledBitmap(source, width, height, true);
            source.recycle();
            return newCover;
        } else {
            return source;
        }
    }

    @Override
    public String key() {
        return null;
    }
}
