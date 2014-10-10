package tr.bcxip.hummingbird.api.objects;

import java.util.List;

/**
 * Created by Hikari on 10/10/14.
 */
public class Substory {
    String id;
    String substory_type;
    String created_at;
    String comment;
    int episode_number;
    UserMini followed_user;
    String new_status;
    String service; // Should be ignored according to the API docs
    List<Permission> permissions; // Should be ignored according to the API docs

    public String getId() {
        return id;
    }

    public String getSubstoryType() {
        return substory_type;
    }

    public String getCreatedAt() {
        return created_at;
    }

    public String getComment() {
        return comment;
    }

    public int getEpisodeNumber() {
        return episode_number;
    }

    public UserMini getFollowedUser() {
        return followed_user;
    }

    public String getNewStatus() {
        return new_status;
    }
}
