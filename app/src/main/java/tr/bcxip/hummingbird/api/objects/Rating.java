package tr.bcxip.hummingbird.api.objects;

/**
 * Created by Hikari on 10/9/14.
 */
public class Rating {
    String type;
    String value;

    final String TYPE_SIMPLE = "simple";
    final String TYPE_ADVANCED = "advanced";

    public static final String RATING_SIMPLE_NEGATIVE = "negative";
    public static final String RATING_SIMPLE_NEUTRAL = "neutral";
    public static final String RATING_SIMPLE_POSITIVE = "positive";

    public String getSimpleRating() {
        if (type.equals(TYPE_SIMPLE) && value != null) {
            Double dvalue = Double.parseDouble(value);
            if (dvalue > 0 && dvalue <= 2.4)
                return RATING_SIMPLE_NEGATIVE;
            if (dvalue > 2.4 && dvalue <= 3.6)
                return RATING_SIMPLE_NEUTRAL;
            if (dvalue > 3.6 && dvalue <= 5)
                return RATING_SIMPLE_POSITIVE;
            return "";
        } else {
            return null;
        }
    }

    public String getAdvancedRating() {
        return value;
    }

    public boolean isAdvanced() {
        return type.equals(TYPE_ADVANCED);
    }

    public boolean isSimple() {
        return type.equals(TYPE_SIMPLE);
    }
}
