package utils;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class NumericUtils {

    private final static NumberFormat numberFormatCurrency;
    private final static NumberFormat numberFormatQuantity;

    static {
        //Initialize a currency number formatter
        numberFormatCurrency = NumberFormat.getNumberInstance(Locale.forLanguageTag("ru"));
        numberFormatCurrency.setMinimumFractionDigits(2);
        numberFormatCurrency.setGroupingUsed(true);

        //Initialize a currency number formatter
        numberFormatQuantity = NumberFormat.getNumberInstance(Locale.forLanguageTag("ru"));
        numberFormatQuantity.setMinimumFractionDigits(0);
        numberFormatQuantity.setMaximumFractionDigits(3);
        numberFormatQuantity.setGroupingUsed(true);
    }

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

    public static String numberToCurrencyString(BigDecimal number) {
        return numberFormatCurrency.format(number);
    }

    public static String numberToQuantityString(BigDecimal number) {
        return numberFormatQuantity.format(number);
    }

}
