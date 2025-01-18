package utils;

import java.util.ArrayList;

public class StringUtils {

    public static String arrayToString(String[] strArray) {
        StringBuilder str = new StringBuilder();
        for (String item : strArray) {
            if (str.length() > 0)
                str.append(", ");
            str.append(item);
        }

        return str.toString();
    }

}
