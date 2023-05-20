package pri.prepare.lovehymn.client.tool;

import android.content.Context;

import pri.prepare.lovehymn.server.function.SdCardTool;

/**
 * 加载进度
 */
public class LoadProcess {

    public static void init(Context ct) {
        FILE_SUM = SdCardTool.getAssetsFileCount(ct);
        process = LOAD_ENUM.COPY_FILE;
    }

    public static int FILE_COUNT = 0;
    public static int FILE_SUM = 0;
    public static int RES_COUNT = 0;
    public static int RES_SUM = 0;
    public static int UNZIP_SUM = 0;
    public static LOAD_ENUM process;

    public static String getText() {
        if (process == LOAD_ENUM.COPY_FILE)
            return "正在复制资源文件:" + FILE_COUNT + (FILE_SUM > 0 ? ("/" + FILE_SUM) : "");

        if (process == LOAD_ENUM.SCAN_ADD)
            return "正在扫描附加包...";

        if (process == LOAD_ENUM.PRE_DEAL_FILE) {
            if (UNZIP_SUM > 0)
                return "正在解压第" + UNZIP_SUM + "个附加包";
            return "正在预处理数据...";
        }

        if (process == LOAD_ENUM.LOAD_RES)
            return "正在加载资源文件:" + RES_COUNT + "/" + RES_SUM;

        return "加载中...";
    }
}

