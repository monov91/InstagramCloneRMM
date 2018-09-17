package com.projects.radomonov.instagramclone.Utils;

public class StringManipulation {
    public static String expandUsername(String username) {
        return username.replace(".", " ");
    }

    public static String condenseUsername(String username) {
        return username.replace(" ", ".");
    }

    public static String getTags(String string) {
        if (string.indexOf("#") > 0) {
            StringBuilder sb = new StringBuilder();
            char[] charArray = string.toCharArray();
            boolean foundTag = false;
            for (char c : charArray) {
                if (c == '#') {
                    foundTag = true;
                    sb.append(c);
                } else {
                    if (foundTag) {
                        sb.append(c);
                    }
                }
                if (c == ' ') {
                    foundTag = false;
                }
            }
            String s = sb.toString().replace(" ","").replace("#",",#");
            return s.substring(1,s.length());
        }
        return string;
    }

}
