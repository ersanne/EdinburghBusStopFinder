package com.eriksanne.edinburghbus.EdinburghBus.Data;

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * String apikey = "1XPBG8FGFDD4TC9SUJ68TCW78";
 * Created by Erik on 28/02/2018.
 */

public class ApiKey {

    //Get the key hashed for the current time
    public static String getKey() {

        TimeZone tz = TimeZone.getTimeZone("GMT");
        Calendar c = Calendar.getInstance(tz);
        Date date = c.getTime();
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHH");
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        String strDate = df.format(date);

        LocalDateTime timestamp = LocalDateTime.now();
        String currentapikey = "1XPBG8FGFDD4TC9SUJ68TCW78" + strDate;

        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] array = md.digest(currentapikey.getBytes(Charset.forName("UTF-8")));
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            return null;
        }
    }

}
