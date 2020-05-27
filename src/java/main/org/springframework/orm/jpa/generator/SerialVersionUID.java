package org.springframework.orm.jpa.generator;

import java.util.HashMap;

public class SerialVersionUID {
    private static HashMap<String, String> map = null;
    static {
        map = new HashMap<String, String>();
        map.put("", "1L");
    }
    public static String getSerialVersionUID(String name) {
        String res = map.get(name);
        if (res == null || res.length() <= 0) {
            res = "1L";
        }
        return res;
    }
}
