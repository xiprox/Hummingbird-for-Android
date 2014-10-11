package tr.bcxip.hummingbird.api;

import android.content.Context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit.RestAdapter;
import tr.bcxip.hummingbird.api.objects.AnimeV2;
import tr.bcxip.hummingbird.api.objects.Story;
import tr.bcxip.hummingbird.api.objects.User;

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

        // TODO - Silcen Mi ? :D
        if (nameOrEmail.contains("@"))
            params.put("email", nameOrEmail);
        else
            params.put("username", nameOrEmail);

        params.put("password", password);

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

    public User getUser(String username) {
        return service.getUser(username);
    }

    public AnimeV2 getAnime(String idOrSlug) {
        return serviceV2.getAnime(idOrSlug);
    }

    public List<Story> getFeed(String username) {
        return service.getFeed(username);
    }

}
