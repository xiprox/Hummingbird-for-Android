package tr.bcxip.hummingbird;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

/**
 * Created by mhca on 10/11/2014.
 */
public class CatchLink extends Activity {
    String mLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLink = getIntent().getDataString();

        if (mLink.contains("/anime/")) {
            mLink = mLink.replace("http://hummingbird.me/anime/", "");

            String sCheck = mLink.substring(mLink.length() - 1, mLink.length());

            if (sCheck.contains("/")) {
                mLink = mLink.substring(0, mLink.length() - 1);
            }

            Intent intent = new Intent(CatchLink.this, AnimeDetailsActivity.class);
            intent.putExtra(AnimeDetailsActivity.ARG_ID, mLink);
            startActivity(intent);
            finish();

            Toast.makeText(getApplicationContext(), mLink, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), mLink, Toast.LENGTH_SHORT).show();
        }


    }
}
