package org.drizzle.drizzle;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ${XYY} on ${2015/11/20}.
 */

public class Util {

    public static String processDate(String date){
        return date.replace('T',' ').replace('Z',' ');
    }

    public static String replaceBlank(String str) {
        String dest = "";
        if (str!=null) {
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            Matcher m = p.matcher(str);
            dest = m.replaceAll("");
        }
        return dest;
    }

}
