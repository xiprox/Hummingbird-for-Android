package tr.bcxip.hummingbird.utils;

import org.joda.time.format.ISODateTimeFormat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Hikari on 10/19/14.
 */
public class Utils {

    public static long getTimestampFromISO8601(String iso8601) {
        return ISODateTimeFormat.dateTime().parseMillis(iso8601);
    }

    public static long getTimestampFromAirDate(String airDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date date = sdf.parse(airDate);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
