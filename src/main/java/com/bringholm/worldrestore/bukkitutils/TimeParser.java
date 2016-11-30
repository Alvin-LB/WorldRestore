package com.bringholm.worldrestore.bukkitutils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeParser {
    private static final Pattern INPUT_PATTERN = Pattern.compile("([0-9]+(ms|[mshdw]))+");

    public static long parseAsMillis(String time) {
        time = time.toLowerCase();
        Matcher matcher = INPUT_PATTERN.matcher(time);
        if (matcher.matches()) {
            long millisTime = 0;
            char[] chars = time.toCharArray();
            String currentNum = "";
            for (int i = 0; i < chars.length; i++) {
                char c = chars[i];
                if (Character.isDigit(c)) {
                    currentNum += c;
                } else {
                    try {
                        switch (c) {
                            case 'm':
                                if (chars[i + 1] == 's') {
                                    millisTime += Long.parseLong(currentNum);
                                    i++;
                                } else {
                                    millisTime += Long.parseLong(currentNum) * 60000L;
                                }
                                break;
                            case 's':
                                millisTime += Long.parseLong(currentNum) * 1000L;
                                break;
                            case 'h':
                                millisTime += Long.parseLong(currentNum) * 3600000L;
                                break;
                            case 'd':
                                millisTime += Long.parseLong(currentNum) * 86400000L;
                                break;
                            case 'w':
                                millisTime += Long.parseLong(currentNum) * 604800000L;
                                break;
                        }
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Entered number (" + e.toString().substring(e.toString().lastIndexOf(":") + 3, e.toString().length() - 1) + ") was too large!");
                    }
                    currentNum = "";
                }
            }
            return millisTime;
        } else {
            throw new IllegalArgumentException("Invalid time input!");
        }
    }
}
