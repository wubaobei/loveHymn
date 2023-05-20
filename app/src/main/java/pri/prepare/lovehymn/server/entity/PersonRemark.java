package pri.prepare.lovehymn.server.entity;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import pri.prepare.lovehymn.server.function.SdCardTool;

public class PersonRemark {
    private static HashMap<String, String> map = null;

    public static void init() {
        if (map != null)
            return;
        map = new HashMap<>();
        MyFile file = getFile();
        if (file != null) {
            String[] cts = file.getContent();
            for (String ct : cts) {
                String[] arr = ct.split(" ");
                if (arr.length == 2) {
                    map.put(arr[0], arr[1]);
                }
            }
        }
    }

    public static String getRemark(Hymn hymn) {
        init();
        if (hymn != null)
            return map.getOrDefault(hymn.toString(), "").replace(SPACE_R," ");
        return "";
    }

    public  static final String NO_REMARK="无";
    private static final String SPACE_R = "@#";

    public static void updateAndSave(Hymn hymn, String content) {
        content = content.replace(" ", SPACE_R);
        map.put(hymn.toString(), content);

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> e : map.entrySet()) {
            sb.append(e.getKey() + " " + e.getValue()).append("\r\n");
        }
        SdCardTool.writeToFile(SdCardTool.getResPath() + "/" + PATH, sb.toString().trim(), SdCardTool.FILE_OVERWRITE);
    }

    public static final String PATH = "150.remark.txt";

    public static MyFile getFile() {
        String path = SdCardTool.getResPath() + "/" + PATH;
        if (new File(path).exists())
            return MyFile.from(path);
        return null;
    }


}
