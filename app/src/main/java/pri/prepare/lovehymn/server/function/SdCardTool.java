package pri.prepare.lovehymn.server.function;

import android.content.Context;
import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import pri.prepare.lovehymn.client.tool.LoadProcess;
import pri.prepare.lovehymn.server.Service;
import pri.prepare.lovehymn.server.UpdateHistory;
import pri.prepare.lovehymn.server.entity.Book;
import pri.prepare.lovehymn.server.entity.Hymn;
import pri.prepare.lovehymn.server.entity.Logger;
import pri.prepare.lovehymn.server.entity.MusicSearch;
import pri.prepare.lovehymn.server.entity.MyFile;
import pri.prepare.lovehymn.server.entity.SearchIndex;
import pri.prepare.lovehymn.server.result.ShowResult;

public class SdCardTool {
    private static String mkdirs(String path) {
        File f = new File(path);
        if (!f.exists()) {
            f.mkdirs();
        }
        return path;
    }

    public static String getRoot() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    public static String getLbPath() {
        return getRoot() + File.separator + Constant.LB_DIR_NAME;
    }

    public static String getD001() {
        return getRoot() + File.separator + Constant.LB_DIR_NAME + File.separator + "大本" + File.separator + "0" + File.separator + "001.pdf";
    }

    public static String getB001() {
        return getRoot() + File.separator + Constant.LB_DIR_NAME + File.separator + "补充本" + File.separator + "00" + File.separator + "001.pdf";
    }

    public static String getE001() {
        return getRoot() + File.separator + Constant.LB_DIR_NAME + File.separator + "儿童诗歌" + File.separator + "00" + File.separator + "001.pdf";
    }

    public static String getWd() {
        return getRoot() + File.separator + Constant.LB_DIR_NAME + File.separator + "white" + File.separator + "b.pdf";
    }

    public static String getResPath() {
        return mkdirs(SdCardTool.getLbPath() + File.separator + Constant.RES_NAME);
    }

    public static String getStepPath() {
        return getResPath() + File.separator + STEP_FILE_NAME;
    }

    public static final String STEP_FILE_NAME = "足迹.txt";

    public static String getSharePath() {
        return getRoot() + File.separator + "诗歌蓝版分享";
    }

    public static String getLogPath() {
        return getSharePath() + File.separator + "日志";
    }

    /**
     * 返回资源文件夹
     */
    public static String copyIfNotExist(Context context, boolean forceUpdate) {
        String path = getLbPath();
        if (!new File(path).exists() || forceUpdate) {
            try {
                Logger.info("复制蓝版资源，是否覆盖:" + forceUpdate);
                FileStorageHelper.copyFilesFromAssets(context, "诗歌蓝版", path, forceUpdate);
                Logger.info("复制蓝版资源完成");
                LoadProcess.FILE_COUNT = -1;
            } catch (Exception e) {
                Logger.exception(e);
            }
        }
        return path;
    }

    public static int getAssetsFileCount(Context context) {
        FileStorageHelper.assetsCount = 0;
        try {
            FileStorageHelper.getAssetsCount(context, "诗歌蓝版");
        } catch (IOException e) {
            Logger.exception(e);
        }
        return FileStorageHelper.assetsCount;
    }

    /**
     * 获取可加载的附加包
     */
    public static String searchAddedFile() {
        //优先搜索蓝版
        String s0 = dfsDir(MyFile.from(getLbPath()), 1);
        if (s0.length() > 0) {
            return s0;
        }
        //然后搜索根目录
        return dfsDir(MyFile.from(getRoot()), 1);
    }

    private static String dfsDir(MyFile f, int deep) {
        if (deep < 0) {
            return "";
        }

        if (f.isDirectory()) {
            if (f.getName().startsWith(".")) {
                //跳过隐藏文件夹
                return "";
            }
            for (MyFile fn : f.listFiles()) {
                String p = dfsDir(fn, deep - 1);
                if (p.length() > 0) {
                    return p;
                }
            }
        } else if (f.getName().contains(Constant.ADD_FILE_NAME) && f.getName().endsWith(".zip")) {
            int type = getFjbType(f);
            if (type == 0) {
                return "";
            }

            if (type == 1 && DbHasLoad()) {
                Logger.info("已经加载大本补充本附加包");
                return "";
            }
            if (type == 2 && OwHasLoad()) {
                Logger.info("已经加载其他诗歌和白板附加包");
                return "";
            }
            return f.getAbsolutePath();

        }

        return "";
    }

    public static int getFjbType(File f) {
        int type = 0;
        if (f.getName().contains("大本") || f.getName().contains("补充本")) {
            type = 1;
        } else if (f.getName().contains("其他") || f.getName().contains("白板")) {
            type = 2;
        }
        return type;
    }

    public static boolean OwHasLoad() {
        return new File(getE001()).exists() || new File(getWd()).exists();
    }

    public static boolean DbHasLoad() {
        return new File(getD001()).exists() || new File(getB001()).exists();
    }


    private static boolean isInteger(String str) {
        Pattern pattern = Pattern.compile("^[-+]?\\d*$");
        return pattern.matcher(str).matches();
    }

    private static ArrayList<String> searchTemp0;
    private static Hymn[] searchTemp1;
    private static int tempInd;
    private static boolean tooMuch;

    /**
     * 是旋律
     */
    private static boolean isMusicStr(String ss) {
        if (ss == null || ss.length() < 5)
            return false;
        String nm = "1234567";
        for (char c : ss.toCharArray())
            if (!nm.contains(c + ""))
                return false;
        return true;
    }

    public static ShowResult[] search(String ss, int page, int bookId) throws Exception {
        long t1 = System.currentTimeMillis();
        ShowResult[] res = search0(ss, page, bookId);
        t1 = System.currentTimeMillis() - t1;
        if (t1 > 100) {
            String s = "搜索'" + ss + "'";
            if (page > 0)
                s += "第" + page + "页";
            if (bookId > 0)
                s += "书id=" + bookId;
            s += "耗时" + t1 + "ms";
            Logger.info(s);
        }

        return res;
    }

    public static String searchType = "";

    private static ShowResult[] searchMusic(String ss) {
        List<ShowResult> res = new ArrayList<>();
        int[] n = new int[1];
        int ind = 0;
        for (String s : MusicSearch.searchMusic(ss, n)) {
            ind++;
            try {
                Hymn h = Hymn.search(s);
                MyFile f = h.getFile();
                if (f != null) {
                    if (res.size() > 10) {
                        res.add(new ShowResult("结果太多了"));
                        break;
                    }
                    res.add(new ShowResult(f, ind <= n[0]));
                }
            } catch (Exception e) {
                Logger.exception(e);
            }
        }
        return res.toArray(new ShowResult[0]);
    }

    private static ShowResult[] searchCorrect(String ss) {
        List<ShowResult> res = new ArrayList<>();
        for (String a : Service.getC().correctOrders(ss)) {
            try {
                Hymn h = Hymn.search(a);
                MyFile f = h.getFile();
                if (f != null)
                    res.add(new ShowResult(f));
            } catch (Exception e) {
                Logger.exception(e);
            }
        }
        return res.toArray(new ShowResult[0]);
    }

    private static ShowResult[] search0(String ss, int page, int bookId) throws Exception {
        if (isMusicStr(ss)) {
            //搜索旋律
            searchType = "isMusicStr";
            if (page == 0) {
                return searchMusic(ss);
            }
            return new ShowResult[0];
        } else if (Service.getC().correctOrders(ss) != null) {
            //D001;D002 一般是同谱诗歌链接
            searchType = "correctOrders";
            if (page == 0) {
                return searchCorrect(ss);
            }
            return new ShowResult[0];
        } else if (isInteger(ss)) {
            List<ShowResult> res = new ArrayList<>();
            //搜索序号
            searchType = "isInteger";
            if (page == 0) {
                tempInd = 0;
                SearchIndex[] t = SearchIndex.search(ss, Constant.SEARCH_RESULT_SHOW_MAX_COUNT + 1);
                Book bk = null;
                if (bookId >= 0)
                    bk = Book.getById(bookId);
                int n = 0;
                searchTemp0 = new ArrayList<>();

                for (int i = tempInd; i < t.length; i++) {
                    SearchIndex sc = t[i];
                    for (String p : sc.getPathArr()) {
                        if (bk != null) {
                            if (!p.contains(bk.fullName))
                                continue;
                        }
                        if (searchTemp0.contains(p)) {
                            continue;
                        }
                        searchTemp0.add(p);
                        n++;
                        if (n == Constant.SEARCH_RESULT_SHOW_MAX_COUNT + 1)
                            break;
                    }
                    if (n == Constant.SEARCH_RESULT_SHOW_MAX_COUNT + 1)
                        break;
                }

                tooMuch = searchTemp0.size() == Constant.SEARCH_RESULT_SHOW_MAX_COUNT + 1;
            }

            if (tempInd >= searchTemp0.size()) {
                if (page == 0 && searchTemp0.size() == 0) {
                    res.add(new ShowResult(Constant.NO_RESULT));
                    return res.toArray(new ShowResult[0]);
                }
                return new ShowResult[0];
            }

            int n = 0;
            for (int i = tempInd; i < searchTemp0.size(); i++) {
                String p = searchTemp0.get(i);
                if (n < Constant.SEARCH_RESULT_SHOW_COUNT) {
                    ShowResult sr = new ShowResult(MyFile.from(p));
                    res.add(sr);
                } else {
                    boolean hasMore = i != searchTemp0.size() - 1;
                    ShowResult srt = new ShowResult(hasMore ? Constant.SHOW_MORE : Constant.TOO_MUCH_WARN);
                    res.add(srt);
                    tempInd = i + 1;
                    return res.toArray(new ShowResult[0]);
                }
                n++;

            }
            if (tooMuch)
                res.add(new ShowResult(Constant.TOO_MUCH_WARN));
            tempInd = searchTemp0.size();
            return res.toArray(new ShowResult[0]);
        } else {
            List<ShowResult> res = new ArrayList<>();
            //一般搜索
            searchType = "else";
            ArrayList<String> list = searchTextSplit(ss);

            if (page == 0) {
                long t0 = System.currentTimeMillis();
                searchTemp1 = Hymn.search(list, Constant.SEARCH_RESULT_SHOW_MAX_COUNT + 1, bookId);
                t0 = System.currentTimeMillis() - t0;
                if (t0 > 100)
                    Logger.info("查询实体 " + t0 + "ms");
                tempInd = 0;
                tooMuch = searchTemp1.length == Constant.SEARCH_RESULT_SHOW_MAX_COUNT + 1;
            }

            if (searchTemp1 == null || searchTemp1.length == 0 || tempInd >= searchTemp1.length)
                return new ShowResult[0];

            int n = 0;
            long tp = System.currentTimeMillis();
            for (int i = tempInd; i < searchTemp1.length; i++) {
                Hymn h = searchTemp1[i];
                ShowResult f;
                if (n >= Constant.SEARCH_RESULT_SHOW_COUNT) {
                    res.add(new ShowResult(Constant.SHOW_MORE));
                    tempInd = i;
                    Logger.info("tp " + (System.currentTimeMillis() - tp));
                    return res.toArray(new ShowResult[0]);
                }
                f = new ShowResult(Service.getC().searchFile(h), h, new ArrayList<>(list));

                if (f.file != null || f.showStr.length() > 0)
                    res.add(f);
                else
                    Logger.info("找不到文件：" + h.getBook().fullName + " " + h.getIndex1() + " " + (h.getIndex2() > 1 ? (h.getIndex2() + "") : ""));

                n++;
            }
            Logger.info("tp " + (System.currentTimeMillis() - tp));
            if (page == 0 && searchTemp1.length == 0) {
                res.add(new ShowResult(Constant.NO_RESULT));
                return res.toArray(new ShowResult[0]);
            }

            if (tooMuch)
                res.add(new ShowResult(Constant.TOO_MUCH_WARN));
            tempInd = searchTemp1.length;
            return res.toArray(new ShowResult[0]);
        }
    }

    private static ArrayList<String> searchTextSplit(String ss) {
        ArrayList<String> res = new ArrayList<>();
        if (ss.contains(")") && ss.contains("(")) {
            res.add(ss.trim());
            return res;
        }
        for (String s : ss.split(" "))
            res.add(s.trim());
        return res;
    }

    /**
     * 获取资源列表，用于比较更新情况
     */
    public static String[] getResFileList() {
        List<String> s = new ArrayList<>();
        for (MyFile fnn : MyFile.from(getResPath()).listFiles()) {
            String n = fnn.getName();
            if (n.contains(".")) {
                n = n.substring(0, n.lastIndexOf("."));
                if (n.contains(".")) {
                    try {
                        String ns = n.substring(0, n.indexOf("."));
                        if (Integer.parseInt(ns) < UpdateHistory.MIN_RES_INDEX)
                            continue;
                    } catch (Exception e) {
                        Logger.exception(e);
                    }
                }
                s.add(n);
            }
        }
        Collections.sort(s);
        if (s.size() == 0)
            Logger.info("no res file");
        return s.toArray(new String[0]);
    }
    public static String[] getExceptionFolder() {
        List<String> s = new ArrayList<>();
        List<String> c=List.of("res","white","补充本","唱诗人","大本","儿童诗歌","其它","青年诗歌","特会标语诗歌","新歌颂咏");
        for (MyFile fnn : MyFile.from(getLbPath()).listFiles()) {
            if(fnn.getName().length()==1){
                continue;
            }
            if(c.contains(fnn.getName())){
                continue;
            }
            s.add(fnn.getName());
        }
        return s.toArray(new String[0]);
    }

    private static int numStat = 0;

    public static int getNum(Book bk, String extension, boolean isFull) {
        numStat = 0;
        MyFile f = MyFile.from(SdCardTool.getLbPath() + File.separator + (isFull ? bk.fullName : bk.simpleName));
        dfs(f, extension);
        return numStat;
    }

    private static void dfs(MyFile f, String extension) {
        if (f.getName().toUpperCase().contains(extension))
            numStat++;
        else if (f.isDirectory()) {
            for (MyFile fn : f.listFiles()) {
                dfs(fn, extension);
            }
        }
    }

    public static final int FILE_OVERWRITE = 1;
    public static final int FILE_NO_OVERWRITE = 0;
    public static final int FILE_APPEND = 2;

    /**
     * 写文件
     * @param writeType 0 禁止覆盖 1 覆盖 2 追加
     */
    public static boolean writeToFile(String path, String record, int writeType) {
        if (writeType == FILE_NO_OVERWRITE && new File(path).exists())
            return false;
        try (FileWriter fw = new FileWriter(path, writeType == FILE_APPEND); BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(record);
            bw.write("\r\n");
            return true;
        } catch (Exception e) {
            Logger.exception(e);
            return false;
        }
    }

    public static MyFile getOtherFile() {
        String path = getResPath();
        path += "/199.qita.txt";
        if (new File(path).exists())
            return MyFile.from(path);
        return null;
    }
}
