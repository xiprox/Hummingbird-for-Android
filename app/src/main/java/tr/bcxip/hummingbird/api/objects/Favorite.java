package tr.bcxip.hummingbird.api.objects;

import tr.bcxip.hummingbird.utils.Utils;

/**
 * Created by Hikari on 10/9/14.
 */
public class Favorite {
    String id;
    String user_id;
    String item_id;
    String item_type;
    String created_at;
    String updated_at;

    public String getId() {
        return id;
    }

    public String getUserId() {
        return user_id;
    }

    public String getItemId() {
        return item_id;
    }

    public String getItemType() {
        return item_type;
    }

    public long getCreatedAt() {
        return Utils.getTimestampFromISO8601(created_at);
    }

    public long getUpdatedAt() {
        return Utils.getTimestampFromISO8601(updated_at);
    }
}
