package fr.arnaud.siege.util;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Utils {

    public static <T> T pickRandom(List<T> list) {
        if (list == null || list.isEmpty()) return null;
        return list.get(ThreadLocalRandom.current().nextInt(list.size()));
    }
}
