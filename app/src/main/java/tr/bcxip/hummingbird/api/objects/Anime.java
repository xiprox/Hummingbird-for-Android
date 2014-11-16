package tr.bcxip.hummingbird.api.objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import tr.bcxip.hummingbird.utils.Utils;

/**
 * Created by Hikari on 10/10/14.
 */
public class Anime implements Serializable {
    String id;
    String slug;
    String status;
    String url;
    String title;
    String alternate_title;
    int episode_count;
    int episode_length;
    String cover_image;
    String synopsis;
    String show_type;
    String started_airing;
    String finished_airing;
    double community_rating;
    String age_rating;
    List<Genre> genres;

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

    public int getEpisodeLength() {
        return episode_length;
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

    public long getAiringStartDate() {
        return started_airing != null ?
                Utils.getTimestampFromAirDate(started_airing) : 0;
    }

    public long getAiringFinishedDate() {
        return finished_airing != null ?
                Utils.getTimestampFromAirDate(finished_airing) : 0;
    }

    public double getCommunityRating() {
        return community_rating;
    }

    public String getAgeRating() {
        return age_rating;
    }

    public List<Genre> getGenres() {
        return genres;
    }
}
