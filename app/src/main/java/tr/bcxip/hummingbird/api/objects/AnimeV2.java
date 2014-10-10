package tr.bcxip.hummingbird.api.objects;

import java.util.List;

/**
 * Created by Hikari on 10/8/14.
 */
public class AnimeV2 {
    int id;
    String slug;
    String canonical_title;
    String english_title;
    String romaji_title;
    String synopsis;
    String poster_image;
    List<String> genres;
    String type;
    String started_airing;
    String finished_airing;
    List<String> screencaps;
    String youtube_trailer_id;
    double community_rating;
    String age_rating;
    int episode_count;
    int episode_length;

    public int getId() {
        return id;
    }

    public String getSlug() {
        return slug;
    }

    public String getCanonicalTitle() {
        return canonical_title;
    }

    public String getEnglishTitle() {
        return english_title;
    }

    public String getRomajiTitle() {
        return romaji_title;
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
        return type;
    }

    public String getAiringStartDate() {
        return started_airing; // TODO - parse into a timestamp
    }

    public String getAiringFinishedDate() {
        return finished_airing; // TODO - parse into a timestamp
    }

    public List<String> getSreencaps() {
        return screencaps;
    }

    public String getTrailer() {
        return youtube_trailer_id; // TODO - return full link or something
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
