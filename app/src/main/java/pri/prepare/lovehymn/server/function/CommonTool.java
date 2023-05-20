package pri.prepare.lovehymn.server.function;

import java.util.ArrayList;

import pri.prepare.lovehymn.server.entity.Logger;
import pri.prepare.lovehymn.server.entity.MyFile;
import pri.prepare.lovehymn.server.entity.Setting;

public class CommonTool {
    public static CommonTool c = null;

    public static CommonTool getC() {
        if (c == null)
            c = new CommonTool();
        return c;
    }

    /**
     * string换行调整
     */
    public String lineDeal(String s) {
        if (s == null)
            return null;

        String[] arr = s.split("\n");
        ArrayList<String> res = new ArrayList<>();
        for (String a : arr) {
            if (a.trim().length() == 0)
                continue;
            res.add(a.trim());
        }
        boolean b = Setting.line_show_default;
        return String.join(b ? "\r\n\r\n" : "\r\n", res);
    }

    public int hymnIndexCompare(String n1, String n2) {
        if (n1.contains("-") == n2.contains("-"))
            return n1.compareTo(n2);
        if (n1.contains("."))
            n1 = n1.substring(0, n1.indexOf("."));
        if (n2.contains("."))
            n2 = n2.substring(0, n2.indexOf("."));
        return n1.compareTo(n2);
    }

    public int folderFileCompare(MyFile o1, MyFile o2) {
        int v1 = o1.compareValue() - o2.compareValue();
        if (v1 != 0)
            return v1;
        return o1.getName().compareTo(o2.getName());
    }

    public void ArraySort(MyFile[] arr, int type) {
        try {
            if (type == HYMN_INDEX_COMPARE) {
                for (int i = 0; i < arr.length; i++)
                    for (int j = i + 1; j < arr.length; j++) {
                        if (hymnIndexCompare(arr[i].getName(), arr[j].getName()) < 0) {
                            MyFile temp = arr[i];
                            arr[i] = arr[j];
                            arr[j] = temp;
                        }
                    }
            } else if (type == FOLDER_FILE_COMPARE) {
                for (int i = 0; i < arr.length; i++)
                    for (int j = i + 1; j < arr.length; j++) {
                        if (folderFileCompare(arr[i], arr[j]) > 0) {
                            MyFile temp = arr[i];
                            arr[i] = arr[j];
                            arr[j] = temp;
                        }
                    }
            }
        } catch (Exception e) {
            Logger.info("排序异常");
            Logger.exception(e);
        }
    }

    public static final int HYMN_INDEX_COMPARE = 123;
    public static final int FOLDER_FILE_COMPARE = 126;
}
