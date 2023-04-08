package org.bot.ua.utils;

import org.hashids.Hashids;

public class CryptoTool {

    //add my salt
    private final Hashids hashids;

    public CryptoTool(String salt) {
        var minHashLength = 14;
        this.hashids = new Hashids(salt, minHashLength);
    }

    public String hashOff(Long value) {
        return hashids.encode(value);
    }

    public Long idOff(String value) {
        long[] res = hashids.decode(value);
        if (res != null && res.length > 0) {
            return res[0];
        }
        return null;
    }
}
