package pri.prepare.lovehymn.server.entity;

import android.app.Activity;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import pri.prepare.lovehymn.R;
import pri.prepare.lovehymn.server.function.SdCardTool;

public class Dict {
    private static HashMap<String, String> dict = null;

    private static void loadDict(Activity activity) {
        if (dict != null) return;
        dict = new HashMap<>();
        for (String s : MyFile.readStream(activity.getResources().openRawResource(R.raw.dict))) {
            if (s.length() <= 1)
                continue;
            if (!s.contains(" "))
                continue;
            String[] ss = s.split(" ");
            if (dict.containsKey(ss[0].trim()))
                throw new RuntimeException("字典文件错误，请修改");
            dict.put(ss[0].trim(), ss[0].trim() + "[" + ss[1].trim() + "]");
        }
        Logger.info("dict:" + dict.size());
    }

    public static String updateContent(String s, Activity activity) {
        loadDict(activity);
        boolean show = Setting.getValueB(Setting.SHOW_DICT);
        if (!show)
            return s;
        for (Map.Entry<String, String> e : dict.entrySet())
            s = s.replaceFirst(e.getKey(), e.getValue());
        return s;
    }
}
