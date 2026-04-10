package com.akhil.urlShortner.utils;

public class UrlShortenerUtil {

    private static final String BASE62 = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int BASE = 62;

    // Secret key (keep this private!)
    private static final long SECRET_KEY = 0x9E3779B97F4A7C15L;

    // 🔹 Encode: ID → Short Code
   public static String encode(long id) {
    long obfuscated = id ^ SECRET_KEY;
    obfuscated = Math.abs(obfuscated); // ✅ FIX
    return toBase62(obfuscated);
}

    // 🔹 Decode: Short Code → ID
    public static long decode(String shortCode) {
        long num = fromBase62(shortCode);
        return num ^ SECRET_KEY;
    }

    // 🔹 Convert number → Base62
    private static String toBase62(long num) {
        if (num == 0) return String.valueOf(BASE62.charAt(0));

        StringBuilder sb = new StringBuilder();

        while (num > 0) {
            int rem = (int) (num % BASE);
            sb.append(BASE62.charAt(rem));
            num /= BASE;
        }

        return sb.reverse().toString();
    }

    // 🔹 Convert Base62 → number
    private static long fromBase62(String str) {
        long num = 0;

        for (int i = 0; i < str.length(); i++) {
            num = num * BASE + BASE62.indexOf(str.charAt(i));
        }

        return num;
    }
}
