package tr.bcxip.hummingbird.api.objects;

import java.util.List;

import tr.bcxip.hummingbird.utils.Utils;

/**
 * Created by Hikari on 10/8/14.
 */
public class AnimeV2 {
    Data anime;

    private class Data {
        String id;
        String slug;
        String canonical_title;
        String synopsis;
        String started_airing_date;
        String finished_airing_date;
        String youtube_trailer_id;
        String age_rating;
        int episode_count;
        int episode_length;
        String english_title;
        String romaji_title;
        String poster_image;
        String type;
        double community_rating;
        List<String> genres;
        List<String> screencaps;
    }

    public String getId() {
        return anime.id;
    }

    public String getSlug() {
        return anime.slug;
    }

    public String getCanonicalTitle() {
        return anime.canonical_title;
    }

    public String getEnglishTitle() {
        return anime.english_title;
    }

    public String getRomajiTitle() {
        return anime.romaji_title;
    }

    public String getSynopsis() {
        return anime.synopsis;
    }

    public String getCoverImageLink() {
        return anime.poster_image;
    }

    public List<String> getGenres() {
        return anime.genres;
    }

    public String getType() {
        return anime.type;
    }

    public long getAiringStartDate() {
        return anime.started_airing_date != null ?
                Utils.getTimestampFromAirDate(anime.started_airing_date) : 0;
    }

    public long getAiringFinishedDate() {
        return anime.finished_airing_date != null ?
                Utils.getTimestampFromAirDate(anime.finished_airing_date) : 0;
    }

    public List<String> getSreencaps() {
        return anime.screencaps;
    }

    public String getTrailer() {
        return anime.youtube_trailer_id;
    }

    public double getCommunityRating() {
        return anime.community_rating;
    }

    public String getAgeRating() {
        return anime.age_rating;
    }

    public int getEpisodeCount() {
        return anime.episode_count;
    }

    public int getEpisodeLength() {
        return anime.episode_length;
    }
}
