package pri.prepare.lovehymn.server.function;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import pri.prepare.lovehymn.server.entity.Book;
import pri.prepare.lovehymn.server.entity.Hymn;
import pri.prepare.lovehymn.server.entity.LabelType;
import pri.prepare.lovehymn.server.entity.MyFile;
import pri.prepare.lovehymn.server.entity.Setting;

public class CollectTool {
    public static MyFile[] getCollects() {
        String s = Setting.getValueS(Setting.COLLECT);
        String[] arr = s.split(";");
        ArrayList<MyFile> res = new ArrayList<>();
        for (String a : arr) {
            if (new File(a).exists())
                res.add(MyFile.from(a));
        }
        return sort(res).toArray(new MyFile[0]);
    }

    private static ArrayList<MyFile> sort(ArrayList<MyFile> fs) {
        ArrayList<MyFile> res = new ArrayList<>();
        for (Book bk : Book.getAll()) {
            ArrayList<MyFile> temp = new ArrayList<>();
            ArrayList<MyFile> fu = new ArrayList<>();
            for (MyFile f : fs) {
                if (f.getAbsolutePath().contains(bk.FullName)) {
                    temp.add(f);
                    if (f.getName().contains(Constant.SUBJOIN_DIR_NAME))
                        fu.add(f);
                }
            }
            Collections.sort(temp);
            Collections.sort(fu);
            temp.removeAll(fu);
            temp.addAll(fu);

            res.addAll(temp);
        }
        Collections.reverse(res);
        return res;
    }

    public static void modCollect(MyFile f) {
        String s = Setting.getValueS(Setting.COLLECT);
        if (s.contains(f.getAbsolutePath())) {
            s = s.replace(f.getAbsolutePath(), "").replace(";;", ";");
        } else {
            s += ";" + f.getAbsolutePath();
            if (s.startsWith(";"))
                s = s.substring(1);
        }
        Setting.updateSetting(Setting.COLLECT, s);
    }

    public static boolean hasFile(MyFile f) {
        String s = Setting.getValueS(Setting.COLLECT);
        return s.contains(f.getAbsolutePath());
    }

    /**
     * 清空收藏夹
     */
    public static void clearAll() {
        Setting.updateSetting(Setting.COLLECT, "");
    }
}
