package com.ding.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yanou
 * @date 2022年10月28日 4:56 下午
 */
public class ListUtil {
    public static <T> List<List<T>> averageAssign(List<T> source, int n) {
        List<List<T>> result = new ArrayList<>();
        int remaider = source.size() % n;
        int number = source.size() / n;
        int offset = 0;
        for (int i = 0; i < n; i++) {
            List<T> value;
            if (remaider > 0) {
                value = source.subList(i * number + offset, (i + 1) * number + offset + 1);
                remaider--;
                offset++;
            } else {
                value = source.subList(i * number + offset, (i + 1) * number + offset);
            }
            result.add(value);
        }
        return result;
    }

    public static String toStringWithSeparator(List<String> users, String separator) {
        if (users.isEmpty()) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        for (String user : users) {
            result.append(user).append(separator);
        }
        result.deleteCharAt(result.length() - 1);
        return result.toString();
    }
}
