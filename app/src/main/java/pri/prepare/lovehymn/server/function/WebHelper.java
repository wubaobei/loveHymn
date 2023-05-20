package pri.prepare.lovehymn.server.function;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import pri.prepare.lovehymn.server.entity.Logger;

public class WebHelper {
//    public static boolean isExist(String urlString) {
//        try {
//            URL url = new URL(urlString);
//            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//            int state = connection.getResponseCode();
//            return state == 200;
//        } catch (Exception e) {
//            return false;
//        }
//    }

    public static void OpenByUrl(Activity activity, String url){
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW,uri);
        activity.startActivity(intent);
    }

    public static final String AD_TITLE = "AD_TITLE";
    public static final String AD_CONTENT = "AD_CONTENT";
    public static final String NEW_VERSION = "NEW_VERSION";
    public static final String BD_URL = "BD_URL";
    public static final String BD_PW = "BD_PW";

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
        Logger.info("加密结果：" + sb.toString());
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
        Logger.info("解密结果：" + sb.toString());
        return sb.toString();
    }

    public static HashMap<String, String> getAds() {
        try {
            Logger.info("getAds from " + Constant.ADURL);
            String ct = getContentGzip(Constant.ADURL);
            HashMap<String, String> res = new HashMap<>();
            if (ct == null) {
                Logger.info("return null");
                return res;
            }
            if (!(ct.contains("神秘数字") && ct.contains("结束标记"))) {
                Logger.info("未识别到标识 长度=" + res.size() + " ct:" + ct.length());
                return res;
            }

            ct = ct.substring(0, ct.indexOf("结束标记"));
            ct = ct.substring(ct.lastIndexOf("神秘数字") + 4);
            StringBuilder sb = new StringBuilder();
            for (char c : ct.toCharArray()) {
                if (c >= '1' && c <= '9' || c == '0')
                    sb.append(c);
                else
                    break;
            }
            ct = decode(ct);

            String f1 = "adbegin";
            String f2 = "adend";
            String f3 = "adtitle";
            String f4 = "titleend";
            if (ct.contains(f1) && ct.contains(f2) && ct.contains(f3) && ct.contains(f4)) {
                int ind1 = ct.indexOf(f1) + f1.length();
                int ind2 = ct.indexOf(f2);
                int ind3 = ct.indexOf(f3) + f3.length();
                int ind4 = ct.indexOf(f4);
                res.put(AD_CONTENT, ct.substring(ind1, ind2).trim());
                res.put(AD_TITLE, ct.substring(ind3, ind4).trim());
            }
            String f5 = "versionbegin";
            String f6 = "versionend";
            if (ct.contains(f5) && ct.contains(f6)) {
                int ind5 = ct.indexOf(f5) + f5.length();
                int ind6 = ct.indexOf(f6);
                res.put(NEW_VERSION, ct.substring(ind5, ind6).trim());
            }
            String b1 = "bdurl";
            String b2 = "urlend";
            String b3 = "bdpw";
            String b4 = "pwend";
            if (ct.contains(b1) && ct.contains(b2) && ct.contains(b3) && ct.contains(b4)) {
                int ind1 = ct.indexOf(b1) + b1.length();
                int ind2 = ct.indexOf(b2);
                int ind3 = ct.indexOf(b3) + b3.length();
                int ind4 = ct.indexOf(b4);
                res.put(BD_URL, "https://pan.baidu.com/s/" + ct.substring(ind1, ind2).trim());
                res.put(BD_PW, ct.substring(ind3, ind4).trim());
            }
            Logger.info("ads count:" + res.size());
            if (res.size() == 0) {
                Logger.info(ct);
            }
            return res;
        } catch (Exception e) {
            Logger.exception(e);
            return null;
        }
    }

    public static String getContentGzip(String u) {
        String r = getMethodForNormal(u);
        if (r == null || r.length() == 0)
            r = getMethodForGzip(u);
        return r;
    }

    private static String getMethodForGzip(String u) {
        try {
            URL url = new URL(u);
            List<String> lines = new GzippedByteSource(Resources.asByteSource(url)).asCharSource(Charsets.UTF_8).readLines();
            return String.join("\r\n", lines);
        } catch (Exception e) {
            Logger.info("read url by gzip:" + e.getMessage());
            return null;
        }
    }

    private static String getMethodForNormal(String u) {
        try {
            URL url = new URL(u);
            List<String> lines = Resources.asCharSource(url, Charsets.UTF_8).readLines();
            return String.join("\r\n", lines);
        } catch (Exception e) {
            Logger.info("read url by normal:" + e.getMessage());
            return null;
        }
    }

}
