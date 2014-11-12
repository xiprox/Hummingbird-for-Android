package tr.bcxip.hummingbird;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.Toast;

/**
 * Created by mhca on 10/11/2014.
 */
public class CatchLink extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String url = getIntent().getDataString();

        if (url.contains("/anime/")) {
            String slug = url
                    .replace("http://hummingbird.me/anime/", "")
                    .replace("https://hummingbird.me/anime/", "")
                    .replace("/", "");

            Intent intent = new Intent(this, AnimeDetailsActivity.class);
            intent.putExtra(AnimeDetailsActivity.ARG_ID, slug);
            startActivity(intent);
            finish();
        }
    }
}
