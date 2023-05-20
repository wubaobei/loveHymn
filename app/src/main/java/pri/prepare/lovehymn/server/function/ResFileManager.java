package pri.prepare.lovehymn.server.function;

import java.io.File;
import java.util.ArrayList;

import pri.prepare.lovehymn.R;
import pri.prepare.lovehymn.server.entity.MyFile;

public class ResFileManager {
    public static int[] getMusicFiles() {
        return new int[]{R.raw.b111,R.raw.b112};
    }

    private static String[] jumpList = new String[]{"letter", "字典", "dict", "bible", "三旧一新", "旋律"};

    public static boolean jumpRes(MyFile res) {
        for (String jumpName : jumpList) {
            if (res.getName().contains(jumpName))
                return true;
        }
        return false;
    }
}
