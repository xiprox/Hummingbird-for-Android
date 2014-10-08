package tr.bcxip.hummingbird.api;

import android.content.Context;

import retrofit.RestAdapter;
import tr.bcxip.hummingbird.api.objects.Anime;

/**
 * Created by Hikari on 10/8/14.
 */
public class HummingbirdApi {
    private static final String API_HOST_v1 = "http://hummingbird.me/api/v1";
    private static final String API_HOST_v2 = "http://hummingbird.me/api/v2";

    Context context;
    HummingbirdService service;
    HummingbirdServiceV2 serviceV2;

    public HummingbirdApi(Context context) {
        this.context = context;
        setupServices();
    }

    private void setupServices() {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(API_HOST_v1)
                .build();

        RestAdapter restAdapterV2 = new RestAdapter.Builder()
                .setEndpoint(API_HOST_v2)
                .build();

        service = restAdapter.create(HummingbirdService.class);
        serviceV2 = restAdapterV2.create(HummingbirdServiceV2.class);
    }

    public Anime getAnimeById(int id) {
        return serviceV2.getAnime(id);
    }
}
