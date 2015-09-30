package jp.co.recruit_tech.around.beaconlibrary.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Hideaki on 15/03/17.
 */
public class DateUtils {
    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss Z");

    public static String simpleFormat(Date date) {
        return simpleDateFormat.format(date);
    }

    public static Date parseSimpleFormat(String dateString) throws ParseException {
        return simpleDateFormat.parse(dateString);
    }
}
