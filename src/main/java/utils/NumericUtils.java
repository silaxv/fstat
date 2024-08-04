package utils;

import java.math.BigDecimal;

public class NumericUtils {

    public static BigDecimal objectToBigDecimal(Object obj) {
        BigDecimal value;

        if (obj == null) {
            value = null;
        } else if (obj instanceof Integer) {
            value = BigDecimal.valueOf((int) obj);
        } else if (obj instanceof Double) {
            value = BigDecimal.valueOf((double) obj);
        } else {
            throw new ClassCastException("Can't convert object to BigDecimal");
        }

        return value;
    }

}
