package pri.prepare.lovehymn.server.function;

import pri.prepare.lovehymn.server.entity.Logger;

public class WebHelper {
    public static String encode(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            int i = (int) c;
            if (i < 256)
                sb.append(String.format("%03d", (i + 500)));
            else {
                sb.append(String.format("%03d", i / 256));
                sb.append(String.format("%03d", i % 256));
            }
        }
        Logger.info("加密结果：" + sb);
        return sb.toString();
    }

    public static String decode(String s) {
        StringBuilder sb = new StringBuilder();
        int bf = -1;
        while (s.length() >= 3) {
            String st = s.substring(0, 3);
            s = s.substring(3);
            int i = Integer.parseInt(st);
            if (i < 256) {
                if (bf != -1) {
                    sb.append((char) (bf * 256 + i));
                    bf = -1;
                } else
                    bf = i;
            } else
                sb.append((char) (i - 500));
        }
        Logger.info("解密结果：" + sb);
        return sb.toString();
    }
}
