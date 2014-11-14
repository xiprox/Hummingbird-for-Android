package tr.bcxip.hummingbird.api.objects;

import java.util.List;

import tr.bcxip.hummingbird.utils.Utils;

/**
 * Created by Hikari on 10/8/14.
 */
public class AnimeV2 {
    String id;
    Titles titles;
    String slug;
    String synopsis;
    String started_airing_date;
    String finished_airing_date;
    String youtube_trailer_id;
    String age_rating;
    int episode_count;
    int episode_length;
    String poster_image;
    String show_type;
    double community_rating;
    List<String> genres;
    List<GalleryImage> gallery_images;

    private class Titles {
        String canonical;
        String english;
        String romaji;
    }

    public String getId() {
        return id;
    }

    public String getSlug() {
        return slug;
    }

    public String getCanonicalTitle() {
        return titles.canonical;
    }

    public String getEnglishTitle() {
        return titles.english;
    }

    public String getRomajiTitle() {
        return titles.romaji;
    }

    public String getSynopsis() {
        return synopsis;
    }

    public String getCoverImageLink() {
        return poster_image;
    }

    public List<String> getGenres() {
        return genres;
    }

    public String getType() {
        return show_type;
    }

    public long getAiringStartDate() {
        return started_airing_date != null ?
                Utils.getTimestampFromAirDate(started_airing_date) : 0;
    }

    public long getAiringFinishedDate() {
        return finished_airing_date != null ?
                Utils.getTimestampFromAirDate(finished_airing_date) : 0;
    }

    public List<GalleryImage> getGalleryImages() {
        return gallery_images;
    }

    public String getTrailer() {
        return youtube_trailer_id;
    }

    public double getCommunityRating() {
        return community_rating;
    }

    public String getAgeRating() {
        return age_rating;
    }

    public int getEpisodeCount() {
        return episode_count;
    }

    public int getEpisodeLength() {
        return episode_length;
    }
}
