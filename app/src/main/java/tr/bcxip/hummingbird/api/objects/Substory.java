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

    public static final String SUBSTORY_TYPE_COMMENT = "comment";
    public static final String SUBSTORY_TYPE_REPLY = "reply";
    public static final String SUBSTORY_TYPE_FOLLOWED = "followed";
    public static final String SUBSTORY_TYPE_WATCHED_EPISODE = "watched_episode";
    public static final String SUBSTORY_TYPE_WATCHLIST_STATUS_UPDATE = "watchlist_status_update";

    public static final String WATCHLIST_STATUS_CURRENTLY_WATCHING = "currently_watching";
    public static final String WATCHLIST_STATUS_PLAN_TO_WATCH = "plan_to_watch";
    public static final String WATCHLIST_STATUS_DROPPED = "dropped";
    public static final String WATCHLIST_STATUS_COMPLETED = "completed";
    public static final String WATCHLIST_STATUS_ON_HOLD = "on_hold";

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
