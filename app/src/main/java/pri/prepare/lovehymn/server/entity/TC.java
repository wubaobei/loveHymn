package pri.prepare.lovehymn.server.entity;

import java.util.HashMap;
import java.util.Map;

/**
 * 时间统计工具类
 */
public class TC {
    private static final HashMap<String, Long> map = new HashMap<>();
    private static final HashMap<String, Integer> mapC = new HashMap<>();

    private static long ct() {
        return System.currentTimeMillis();
    }

    public static void begin(String key) {
        long v1 = map.getOrDefault(key, 0L) - ct();
        map.put(key, v1);

        int v2 = mapC.getOrDefault(key, 0);
        mapC.put(key, v2 + 1);
    }

    public static void end(String key) {
        long v1 = map.get(key) + ct();
        map.put(key, v1);
    }

    public static String log() {
        StringBuilder sb = new StringBuilder();
        if (map.size() == 0)
            return "nothing";
        Logger.info("-----------------------------");
        for (Map.Entry<String, Long> e : map.entrySet()) {
            Logger.info(e.getKey() + "(" + mapC.get(e.getKey()) + ") cost " + e.getValue());
            sb.append(e.getKey()).append("(").append(mapC.get(e.getKey())).append(") cost ").append(e.getValue()).append("\r\n");
        }
        Logger.info("-----------------------------");
        map.clear();
        mapC.clear();
        return sb.toString();
    }
}
