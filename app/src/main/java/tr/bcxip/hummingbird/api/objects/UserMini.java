package tr.bcxip.hummingbird.api.objects;

/**
 * Created by Hikari on 10/10/14.
 */
public class UserMini {
    String name;
    String url;
    String avatar;
    String avatar_small;
    boolean nb; // Should be ignored according to the API docs

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getAvatarSmall() {
        return avatar_small;
    }
}
