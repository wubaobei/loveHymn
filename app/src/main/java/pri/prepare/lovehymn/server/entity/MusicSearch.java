package pri.prepare.lovehymn.server.entity;

import android.app.Activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import pri.prepare.lovehymn.server.function.ResFileManager;

public class MusicSearch {
    public static String[] searchMusic(String s, int[] n) {
        ArrayList<String> res = new ArrayList<>();
        for (Map.Entry<String, String> e : _map.entrySet()) {
            if (e.getValue().startsWith(s))
                res.add(e.getKey());
        }
        n[0] = res.size();
        for (Map.Entry<String, String> e : _map.entrySet()) {
            if (e.getValue().contains(s) && !e.getValue().startsWith(s))
                res.add(e.getKey());
        }
        return res.toArray(new String[0]);
    }

    public static void init(Activity activity) {
        if (_map != null)
            return;

        _map = new HashMap<>();

        for (int fn : ResFileManager.getMusicFiles()) {
            for (String s : MyFile.readStream(activity.getResources().openRawResource(fn))) {
                if (s.contains(" ")) {
                    String[] arr = s.split(" ");
                    if (arr.length != 2) {
                        Logger.info("旋律文件数据的长度异常：" + s);
                    } else {
                        _map.put(arr[0], arr[1]);
                    }
                }
            }
        }
    }

    private static HashMap<String, String> _map = null;

    public static boolean contains(String hymnStr) {
        return _map.containsKey(hymnStr);
    }

    public static String[] getSimilar(String hymnStr) {
        if (!_map.containsKey(hymnStr))
            return new String[0];
        String v = _map.get(hymnStr);
        ArrayList<String> res = new ArrayList<>();
        for (Map.Entry<String, String> e : _map.entrySet()) {
            if (e.getKey().equals(hymnStr))
                continue;
            if (isSimilar(e.getValue(), v)) {
                res.add(e.getKey().replace("-", "附"));
            }
        }
        Collections.sort(res);
        return res.toArray(new String[0]);
    }

    private static final int DIF_COUNT = 2;

    private static boolean isSimilar(String s1, String s2) {
        int ed = ed2(s1, s2, DIF_COUNT);
        return ed >= 0;
    }

    private static int ed2(String s1, String s2, int maxV) {
        if (s1 == null || s2 == null)
            return -1;
        if (s1.equals(s2))
            return 0;
        if (Math.abs(s1.length() - s2.length()) > maxV)
            return -1;
        if (s2.length() > s1.length()) {
            String s3 = s2;
            s2 = s1;
            s1 = s3;
        }
        int[][] arr = new int[s1.length() + 1][s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++)
            arr[i][0] = i;
        for (int j = 0; j <= s2.length(); j++)
            arr[0][j] = j;
        for (int i = 2; i <= s1.length() + s2.length(); i++) {
            int min = s1.length() + s2.length();
            int x, y;
            if (i < s1.length() + 1) {
                x = i - 1;
                y = 1;
            } else {
                x = s1.length();
                y = i - s1.length();
            }
            while (x >= 1 && y <= s2.length()) {
                int f = s1.charAt(x - 1) == s2.charAt(y - 1) ? 0 : 1;
                arr[x][y] = min(arr[x - 1][y] + 1, arr[x][y - 1] + 1, arr[x - 1][y - 1] + f);
                min = Math.min(min, arr[x][y]);
                x--;
                y++;
            }
            if (min > maxV + 1)
                return -1;
        }
        int res = arr[s1.length()][s2.length()];
        if (res > maxV)
            return -1;
        return res;
    }

    private static int min(int a, int b, int c) {
        int d = Math.min(a, b);
        return Math.min(c, d);
    }
}
