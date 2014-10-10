package tr.bcxip.hummingbird.api.objects;

import java.util.ArrayList;

/**
 * Created by Hikari on 10/10/14.
 */
public class Anime {
    String id;
    String slug;
    String status;
    String url;
    String title;
    String alternate_title;
    int episode_count;
    String cover_image;
    String synopsis;
    String show_type;
    ArrayList<String> genres;

    public String getId() {
        return id;
    }

    public String getSlug() {
        return slug;
    }

    public String getStatus() {
        return status;
    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public String getAlternateTitle() {
        return alternate_title;
    }

    public int getEpisodeCount() {
        return episode_count;
    }

    public String getCoverImage() {
        return cover_image;
    }

    public String getSynopsis() {
        return synopsis;
    }

    public String getShowType() {
        return show_type;
    }

    public ArrayList<String> getGenres() {
        return genres;
    }
}
