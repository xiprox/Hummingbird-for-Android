package tr.bcxip.hummingbird.api;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

import retrofit.RestAdapter;
import tr.bcxip.hummingbird.api.objects.AnimeV2;

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

    public String authenticate(String nameOrEmail, String password) {
        Map<String, String> params = new HashMap<String, String>();

        if (nameOrEmail.contains("@"))
            params.put("email", password);
        else
            params.put("username", password);

        return service.authenticate(params);
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

    public AnimeV2 getAnimeById(int id) {
        return serviceV2.getAnime(id);
    }


}
