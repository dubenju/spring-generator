package org.springframework.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SystemDateTime {
    public static String getDateTime() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd");
        return df.format(new Date());
    }
}
