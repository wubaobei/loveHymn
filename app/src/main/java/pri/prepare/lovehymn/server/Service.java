package pri.prepare.lovehymn.server;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.text.ClipboardManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import pri.prepare.lovehymn.R;
import pri.prepare.lovehymn.client.MainActivity;
import pri.prepare.lovehymn.client.SettingDialog;
import pri.prepare.lovehymn.client.tool.DisplayStat;
import pri.prepare.lovehymn.client.tool.LOAD_ENUM;
import pri.prepare.lovehymn.client.tool.LoadProcess;
import pri.prepare.lovehymn.server.dal.AuthorD;
import pri.prepare.lovehymn.server.dal.AuthorRelatedD;
import pri.prepare.lovehymn.server.dal.ContentD;
import pri.prepare.lovehymn.server.dal.ContentTypeD;
import pri.prepare.lovehymn.server.dal.HymnD;
import pri.prepare.lovehymn.server.dal.LetterD;
import pri.prepare.lovehymn.server.dal.SearchIndexD;
import pri.prepare.lovehymn.server.entity.Author;
import pri.prepare.lovehymn.server.entity.Book;
import pri.prepare.lovehymn.server.entity.Content;
import pri.prepare.lovehymn.server.entity.Hymn;
import pri.prepare.lovehymn.server.entity.Label;
import pri.prepare.lovehymn.server.entity.LabelType;
import pri.prepare.lovehymn.server.entity.Letter;
import pri.prepare.lovehymn.server.entity.Logger;
import pri.prepare.lovehymn.server.entity.MyFile;
import pri.prepare.lovehymn.server.entity.SearchIndex;
import pri.prepare.lovehymn.server.entity.Setting;
import pri.prepare.lovehymn.server.entity.TC;
import pri.prepare.lovehymn.server.function.Constant;
import pri.prepare.lovehymn.server.function.DBHelper;
import pri.prepare.lovehymn.server.function.ResFileManager;
import pri.prepare.lovehymn.server.function.SdCardTool;
import pri.prepare.lovehymn.server.function.WebHelper;

import static pri.prepare.lovehymn.server.function.SdCardTool.FILE_NO_OVERWRITE;
import static pri.prepare.lovehymn.server.function.SdCardTool.FILE_OVERWRITE;

public class Service {
    private static Service c;

    public static Service getC() {
        if (c == null)
            c = new Service();
        return c;
    }

    public String[] correctOrders(String s) {
        ArrayList<String> res = new ArrayList<>();

        String[] arr = s.split("[;；.]");
        for (String a : arr) {
            String t;
            if ((t = coFormat(a)) != null) {
                res.add(t);
            }
        }
        if (res.size() == 0)
            return null;
        return res.toArray(new String[0]);
    }

    private String coFormat(String s) {
        if (Book.isShortName(s.charAt(0)) && isInteger(s.substring(1))) {
            return s;
        }
        if (s.startsWith("附")) {
            return "D" + Constant.SUBJOIN_DIR_NAME + s.substring(1);
        }
        return null;
    }

    /**
     * 扫描附加包
     *
     * @return 如果不为空，则显示出来
     */
    public String scanAddedFile() {
        String resPath = SdCardTool.getLbPath();
        if (!new File(resPath).isDirectory()) {
            Logger.info("no added file(0)");
            return "";
        }

        int num = 0;
        String[] otherMsg = new String[1];
        otherMsg[0] = "";
        for (int i = 0; i < 10; i++) {
            String path = SdCardTool.searchAddedFile();
            if (path == null || path.length() == 0) {
                if (num > 0)
                    return "更新了" + num + "个资源文件";
                return "";
            }
            Logger.info("找到附加包：" + path);

            String pathT = path;
            if (new File(path).exists()) {
                pathT = new File(path).getName();
            }
            long t = new File(path).length() / 15000000;
            LoadProcess.UNZIP_SUM = i + 1;
            MainActivity.msgWait = "正在解压 " + pathT + "\r\n预计耗时" + t + "-" + (2 * t) + "秒\r\n请勿退出";
            Logger.info("开始解压 " + path);
            try {
                num += unzip(resPath, path, otherMsg);

                File rn = new File(path);
                if (rn.delete())
                    Logger.info("删除'" + rn.getName() + "'");
                else {
                    Logger.info("删除'" + rn.getName() + "'失败");
                    String p = rn.getAbsolutePath().replace(Constant.ADD_FILE_NAME, Constant.ADD_FILE_NAME + Constant.ADDED_FILE_FLAG);

                    if (rn.renameTo(new File(p))) {
                        Logger.info("删除'" + rn.getName() + "'失败，已重命名");
                    } else {
                        Logger.info("尝试重命名失败");
                    }
                    return "加载附加包出现了问题，已停止";
                }
            } catch (Exception e) {
                MainActivity.msgWait = "解压" + path + "失败,请查看'设置'-'" + SettingDialog.ALL_READ + "'-'常见问题'";
                Logger.exception(e);
            }
        }

        if (otherMsg[0].length() > 0) {
            if (otherMsg[0].length() > 30)
                otherMsg[0] = otherMsg[0].substring(0, 30) + "...";
            otherMsg[0] = "(" + otherMsg[0] + ")";
        }
        return "更新了" + num + "个资源文件" + otherMsg[0];
    }

    /**
     * 扫描所有文件并预处理
     *
     * @param f
     */
    public void predealDir(File f) {
        try {
            if (f.isDirectory()) {
                if (f.getName().equals(Constant.RES_NAME) || f.getName().equals(Constant.WHITE)) {
                    return;
                }
                for (MyFile fn : orderFiles(MyFile.from(f.getAbsolutePath()).listFiles()))
                    predealDir(fn.getfile());
            } else {
                //加入文件名索引，加快搜索速度
                if (isMp3(f))
                    return;
                String sn = f.getName();
                SearchIndex si = SearchIndex.getByName(sn);
                if (si == null) {
                    si = new SearchIndex(sn, f.getAbsolutePath());
                    si.add(false);
                } else {
                    si.setPaths(si.getPaths() + ";" + f.getAbsolutePath());
                    si.update();
                }
            }
        } catch (Exception e) {
            Logger.exception(e);
        }
    }

    public boolean isMp3(File f) {
        return f.isFile() && isMp3(f.getName());
    }

    private boolean isMp3(String name) {
        return name.toUpperCase().endsWith(".MP3");
    }

    private static final String PDF_EXTEND = ".pdf";

    /**
     * 获取当前版本号
     * @param context
     * @return
     */
    public String getVersionStr(Context context) {
        try {
            String res = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
            return res;
        } catch (PackageManager.NameNotFoundException e) {
            return "未知版本";
        }
    }

    public static boolean isInteger(String str) {
        if (str.length() == 0)
            return false;
        Pattern pattern = Pattern.compile("^[-+]?[\\d]*$");
        return pattern.matcher(str).matches();
    }

    private String sameFormatStr(int n, String f) {
        int i = Integer.parseInt(f) + n;
        if (f.length() == 1) {
            return i + "";
        } else if (f.length() == 2) {
            if (i < 10)
                return "0" + i;
            return i + "";
        } else if (f.length() > 2 && (i - n) % 100 == 0) {
            i = Integer.parseInt(f) / 100 + n;
            if (i == 0) {
                if (f.length() == 3)
                    return "000";
                else return "0000";
            } else {
                if (f.length() == 3)
                    return (i * 100) + "";
                else {
                    if (i >= 10)
                        return (i * 100) + "";
                    return "0" + (i * 100);
                }
            }
        }
        return i + "";
    }

    public MyFile previousFile(MyFile f) {
        if (!f.isPdf())
            return null;

        String s = f.getName();
        String ss = s.substring(0, s.length() - 4);
        if (ss.indexOf("-") > 0) {
            String[] sp = ss.split("-");
            if (sp.length == 2) {
                if (sp[1].equals("2")) {
                    String newPath = f.getAbsolutePath().replace("-2", "-1");
                    if ((new File(newPath)).exists())
                        return MyFile.from(newPath);
                    newPath = f.getAbsolutePath().replace("-2", "");
                    if ((new File(newPath)).exists())
                        return MyFile.from(newPath);
                    ss = ss.replace("-2", "");
                }
            }
        }
        if (isInteger(ss)) {
            //
            if (ss.equals("-1")) {
                MyFile fa = f.getParentFile();
                MyFile[] fs = Service.getC().orderFiles(fa.getParentFile().listFiles());
                MyFile aim = fs[fs.length - 2];
                MyFile[] as = Service.getC().orderFiles(aim.listFiles());
                return as[as.length - 1];
            }
            //
            int ssi = Integer.parseInt(ss);
            if (ssi > 1) {
                String newN = (ssi - 1) + PDF_EXTEND;
                //在当前目录查找
                while (newN.length() < 9) {
                    String newN2 = (ssi - 1) + "-2" + PDF_EXTEND;
                    String newPath2 = f.getAbsolutePath().replace(s, newN2);
                    if ((new File(newPath2)).exists())
                        return MyFile.from(newPath2);

                    String newPath = f.getAbsolutePath().replace(s, newN);
                    if ((new File(newPath)).exists())
                        return MyFile.from(newPath);
                    newN = "0" + newN;
                }

                //检查上一级名字是不是数字
                MyFile pf = f.getParentFile();
                if (isInteger(pf.getName())) {
                    //父级相邻文件夹
                    String newP = pf.getAbsolutePath().substring(0, pf.getAbsolutePath().length() - pf.getName().length()) + sameFormatStr(-1, pf.getName());
                    MyFile newF = MyFile.from(newP);
                    if (newF.isDirectory()) {
                        newN = (ssi - 1) + PDF_EXTEND;
                        while (newN.length() < 8) {
                            String newPath = newF.getAbsolutePath() + "/" + newN;
                            if ((new File(newPath)).exists())
                                return MyFile.from(newPath);
                            newN = "0" + newN;
                        }

                        return getMaxPdfFile(newF);
                    }
                }
            } else if (ssi < 0) {
                while (ssi <= -1) {
                    if (ssi == -1) {
                        return getMaxPdfFile(f.getParentFile());
                    } else {
                        String newN = (ssi + 1) + PDF_EXTEND;
                        String newPath = f.getAbsolutePath().replace(s, newN);
                        if ((new File(newPath)).exists())
                            return MyFile.from(newPath);
                        ssi++;
                    }
                }
            }
        }

        return null;
    }

    private MyFile getMaxPdfFile(MyFile newF) {
        int max = -10000;
        MyFile res = null;
        for (MyFile f : newF.listFiles()) {
            if (f.isFile() && f.isPdf()) {
                String sn = f.getName().substring(0, f.getName().length() - 4);
                if (isInteger(sn)) {
                    int in = Integer.parseInt(sn);
                    if (in > max) {
                        max = in;
                        res = f;
                    }
                }
            }
        }
        return res;
    }

    private MyFile getMinPdfFile(MyFile newF) {
        int min = 10000;
        MyFile res = null;
        for (MyFile f : newF.listFiles()) {
            if (f.isPdf()) {
                String sn = f.getName().substring(0, f.getName().length() - 4);
                if (isInteger(sn)) {
                    int in = Integer.parseInt(sn);
                    if (in < min) {
                        min = in;
                        res = f;
                    }
                }
            }
        }
        return res;
    }

    private boolean hasAddedFile(MyFile f) {
        if (f.isDirectory()) {
            for (MyFile fn : f.listFiles())
                if (fn.isFile() && fn.getName().startsWith("-") && fn.isPdf()) {
                    return true;
                }
        }
        return false;
    }

    public MyFile nextFile(MyFile f) {
        if (!f.isPdf())
            return null;
        String s = f.getName();
        String ss = s.substring(0, s.length() - 4);
        if (ss.indexOf("-") > 0) {
            String[] sp = ss.split("-");
            if (sp.length == 2) {
                if (sp[1].equals("2") && isInteger(sp[0])) {
                    int sp0 = Integer.parseInt(sp[0]);
                    ss = (sp0) + "";
                } else if (sp[1].equals("1") && isInteger(sp[0])) {
                    String newPath = f.getAbsolutePath().replace("-1", "-2");
                    if ((new File(newPath)).exists())
                        return MyFile.from(newPath);
                }
            }
        }
        if (isInteger(ss)) {
            {
                String np = f.getAbsolutePath().replace(f.getName(), f.getName().substring(0, f.getName().length() - 4) + "-2") + PDF_EXTEND;
                if ((new File(np)).exists())
                    return MyFile.from(np);
            }
            int ssi = Integer.parseInt(ss);
            if (ssi > 0) {
                String newN = (ssi + 1) + PDF_EXTEND;
                while (newN.length() < 9) {
                    String newPath = f.getAbsolutePath().replace(s, newN);
                    if ((new File(newPath)).exists())
                        return MyFile.from(newPath);
                    newN = "0" + newN;
                }

                newN = (ssi + 1) + "-2" + PDF_EXTEND;
                while (newN.length() < 9 + 2) {
                    String newPath = f.getAbsolutePath().replace(s, newN);
                    if ((new File(newPath)).exists())
                        return MyFile.from(newPath);
                    newN = "0" + newN;
                }

                if (hasAddedFile(f.getParentFile())) {
                    MyFile af = MyFile.from(f.getAbsolutePath().replace(s, "-1.pdf"));
                    if (af.exists())
                        return af;
                }

                //检查上一级名字是不是数字
                MyFile pf = f.getParentFile();
                if (isInteger(pf.getName())) {
                    //父级相邻文件夹
                    String newP = pf.getAbsolutePath().substring(0, pf.getAbsolutePath().length() - pf.getName().length()) + sameFormatStr(1, pf.getName());
                    MyFile newF = MyFile.from(newP);
                    if (newF.isDirectory()) {
                        newN = (ssi + 1) + PDF_EXTEND;
                        while (newN.length() < 8) {
                            String newPath = newF.getAbsolutePath() + "/" + newN;
                            if ((new File(newPath)).exists())
                                return MyFile.from(newPath);
                            newN = "0" + newN;
                        }


                        return getMinPdfFile(newF);
                    }
                    for (MyFile fn : pf.getParentFile().listFiles()) {
                        if (fn.getName().equals(Constant.SUBJOIN_DIR_NAME)) {
                            MyFile[] fs = orderFiles(fn.listFiles());
                            if (fs != null && fs.length > 0)
                                return fs[0];
                        }
                    }
                }
            } else if (ssi < 0) {
                while (ssi > -30) {
                    String newN = (ssi - 1) + PDF_EXTEND;
                    String newPath = f.getAbsolutePath().replace(s, newN);
                    if ((new File(newPath)).exists())
                        return MyFile.from(newPath);
                    ssi--;
                }
            }
        }

        return null;
    }

    private static final String sAuthor = "作者：";
    private static final String sLyricAuthor = "Lyric:";
    private static final String sMusicAuthor = "Music:";

    private String[] getLyricAuthors(String s) {
        if (!s.startsWith(sAuthor))
            return new String[0];
        String st = s.substring(sAuthor.length());
        if (st.contains(sMusicAuthor)) {
            st = st.substring(0, st.indexOf(sMusicAuthor));
        }
        if (st.contains(sLyricAuthor)) {
            ArrayList<String> res = new ArrayList<>();
            while (st.contains(sLyricAuthor)) {
                int ind = st.lastIndexOf(sLyricAuthor);
                res.add(st.substring(ind + sLyricAuthor.length()).trim());
                st = st.substring(0, ind);
            }
            return res.toArray(new String[0]);
        } else {
            if (st.trim().length() == 0)
                return new String[0];

            String[] res = new String[1];
            res[0] = st;
            return res;
        }
    }

    private String[] getMusicAuthors(String s) {
        if (!s.startsWith(sAuthor))
            return new String[0];
        String st = s.substring(sAuthor.length());
        if (st.contains(sMusicAuthor)) {
            String[] res = new String[1];
            res[0] = st.substring(st.indexOf(sMusicAuthor) + sMusicAuthor.length()).trim();
            return res;
        }
        return new String[0];
    }

    public void loadResFile(String[] ct) {
        int stat = -1;
        String sOrder = "序号：";
        String sTitle = "标题：";
        String sWhite = "白版：";
        String sLyric = "歌词：";
        boolean isLyric = false;
        Hymn h = new Hymn();
        try {
            for (String line : ct) {
                line = line.trim();
                if (line.length() == 0)
                    continue;
                if (line.startsWith(sOrder)) {
                    if (h.getBookId() != 0 && h.getIndex1() != 0) {
                        h.addOrUpdateByIndex();
                        isLyric = false;
                        stat = -1;
                    }
                    h = new Hymn();
                    Book bk = null;
                    for (Book b : Book.getAll()) {
                        if (b.SimpleName.equals(line.substring(sOrder.length(), sOrder.length() + 1))) {
                            bk = b;
                            break;
                        }
                    }
                    if (bk == null)
                        throw new RuntimeException("格式异常：" + line);
                    h.setBookId(bk.id);
                    String ind12 = line.substring(sOrder.length() + 1);
                    if (ind12.contains("-") && !ind12.startsWith("-")) {
                        int _ind = ind12.indexOf("-");
                        h.setIndex2(Integer.parseInt(ind12.substring(_ind + 1)));
                        ind12 = ind12.substring(0, _ind);
                    }
                    h.setIndex1(Integer.parseInt(ind12));
                } else if (line.startsWith(sAuthor)) {
                    //作者：AAA
                    //作者：Lyric:AAAMusic:BBB
                    //作者：Lyric:AAALyric:aaaMusic:BBB
                    for (String as : getLyricAuthors(line)) {
                        Author a = Author.search(as);
                        if (a != null) {
                            h.addLyricAuthorId(a.getId());
                        } else {
                            Author na = new Author();
                            na.setName(as);
                            na.addOrUpdateByName();
                            h.addLyricAuthorId(na.getId());
                        }
                    }
                    for (String as : getMusicAuthors(line)) {
                        Author a = Author.search(as);
                        if (a != null) {
                            h.addMusicAuthorId(a.getId());
                        } else {
                            Author na = new Author();
                            na.setName(as);
                            na.addOrUpdateByName();
                            h.addMusicAuthorId(na.getId());
                        }
                    }

                } else if (line.startsWith(sTitle)) {
                    h.setTitle(line.substring(sTitle.length()).trim());
                } else if (line.startsWith(sLyric)) {
                    h.setLyric(line.substring(sLyric.length()).trim());
                    isLyric = true;
                } else if (line.startsWith(sWhite)) {
                    String s = line.substring(sWhite.length()).trim();
                    if (s.contains(".pdf")) {
                        String[] ss = s.split(" ");
                        h.setWhitePdf(ss[0].trim());
                        h.setWhitePage(Integer.parseInt(ss[1]));
                    } else if (isInteger(s))
                        h.setWhitePage(Integer.parseInt(s));
//                } else if (line.startsWith(sBibleSection)) {
//                    String s = line.substring(sBibleSection.length());
//                    h.addSections(s);
                } else {
                    boolean flag = false;
                    for (ContentTypeD ctype : ContentTypeD.getAll()) {
                        if (line.startsWith(ctype.name + "：")) {
                            stat = 10 + ctype.id;
                            h.setContent(ctype, (stat != 10 ? "\t\t" : "") + line.substring(ctype.name.length() + 1).trim());
                            flag = true;
                            break;
                        }
                    }

                    if (!flag && stat >= 9) {
                        h.contentAppend(stat - 10, "\r\n\r\n" + (stat != 10 ? "\t\t" : "") + line.trim());
                    } else if (!flag && isLyric) {
                        h.setLyric(h.getLyric() + "\r\n" + line.trim());
                    }

                }
            }
            if (h.getBookId() != 0 && h.getIndex1() != 0) {
                h.addOrUpdateByIndex();
            }
            Logger.info("加载完成");
        } catch (Exception e) {
            Logger.exception(e);
        }
    }

    public void loadResFileByName(String name) {
        String s = SdCardTool.getResPath() + File.separator + name;
        if (!new File(s).exists())
            s += ".txt";
        if (new File(s).exists()) {
            Logger.info("开始自动加载" + name);
            loadResFile(MyFile.from(s));
        } else {
            Logger.info("未找到" + name);
        }

    }

    /**
     * 加载资源
     *
     * @param res
     * @return 是否需要记录
     */
    private boolean loadResFile(MyFile res) {
        if (ResFileManager.jumpRes(res)) {
            Logger.info("跳过" + res.getName() + "的加载");
            return true;
        }

        if (res.getName().contains(Label.BAK_FILE)) {
            loadLable(res);
            return true;
        } else if (res.getName().contains("足迹")) {
            loadStep(res);
            return true;
        }

        if (res.getName().length() > 4 && res.getName().charAt(3) == '.') {
            String ns = res.getName().substring(0, 3);
            try {
                if (Integer.parseInt(ns) < UpdateHistory.MIN_RES_INDEX) {
                    Logger.info("跳过旧版资源");
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        }
        loadResFile(res.getContent());
        return true;
    }

    public void loadResDir(Activity activity) {
        MyFile qitaFile = SdCardTool.getQitaFile();
        int[] resource = new int[]{R.raw.a101, R.raw.a102, R.raw.a103, R.raw.a104, R.raw.a105, R.raw.a106};
        String[] rn = new String[]{"大本", "补充本", "唱诗人", "新歌颂咏", "青年诗歌", "儿童诗歌"};
        //数量 6本诗歌本+作者+书名+其他(如果有)
        LoadProcess.RES_SUM = resource.length + 2 + (qitaFile != null ? 1 : 0);
        if (qitaFile != null) {
            LoadProcess.RES_COUNT++;
            Logger.info("开始加载 其他诗歌");
            loadResFile(qitaFile);
        }
        for (int i = 0; i < resource.length; i++) {
            LoadProcess.RES_COUNT++;
            Logger.info("开始加载 " + rn[i]);
            loadResFile(MyFile.readStream(activity.getResources().openRawResource(resource[i])));
        }

        LoadProcess.RES_COUNT++;
        loadAuthorRes(activity);
        LoadProcess.RES_COUNT++;
        loadLetterRes(activity);
    }

    private void loadLable(MyFile res) {
        Logger.info("开始加载" + res.getName());
        Label.reload(res);
    }

    private void loadStep(MyFile res) {
        Logger.info("开始加载" + res.getName());
        Hymn.loadStep(res);
    }

    private void loadLetterRes(Activity activity) {
        Logger.info("loadLetterRes");

        String[] cts = MyFile.readStream(activity.getResources().openRawResource(R.raw.letter));
        for (String s : cts) {
            try {
                Letter.insertByStr(s);
            } catch (IllegalAccessException e) {
                Logger.exception(e);
            }
        }
    }

    private void loadAuthorRes(Activity activity) {
        Logger.info("loadAuthorRes");
        //int addA = 0;

        String[] cts = MyFile.readStream(activity.getResources().openRawResource(R.raw.c200));
        String[] hds = new String[]{"名字：", "年龄：", "介绍："};
        Author author = null;
        boolean isAdd = false;
        try {
            for (String line : cts) {
                if (line == null || line.trim().length() == 0)
                    continue;
                if (line.startsWith(hds[0])) {
                    if (author != null) {
                        if (isAdd)
                            author.addOrUpdateByName();
                        else
                            author.update();
                        //addA++;
                    }

                    String ass = line.substring(hds[0].length());
                    author = Author.search(ass);
                    if (author == null) {
                        author = new Author(ass);
                        isAdd = true;
                    }
                } else if (author != null) {
                    if (line.startsWith(hds[1])) {
                        author.setAge(line.substring(hds[1].length()).trim());
                    } else if (line.startsWith(hds[2])) {
                        author.setIntroduction(line.substring(hds[2].length()).trim());
                    } else {
                        author.setIntroduction(author.getIntroduction() + "\r\n" + line.trim());
                    }
                }
            }
            if (author != null) {
                if (isAdd)
                    author.addOrUpdateByName();
                else
                    author.update();
                //addA++;
            }
        } catch (Exception e) {
            Logger.exception(e);
        }
    }

    public MyFile[] orderFiles(MyFile[] fs) {
        if (fs == null || fs.length == 0)
            return new MyFile[0];
        boolean isBookName = false;
        for (MyFile f : fs)
            for (Book bk : Book.getAll()) {
                if (bk.FullName.equals(f.getName())) {
                    isBookName = true;
                    break;
                }
            }
        //is book and order by book id
        if (isBookName) {
            List fileList = Arrays.asList(fs);
            Collections.sort(fileList, (Comparator<MyFile>) (o1, o2) -> {
                if (o1.isDirectory() && o2.isFile())
                    return -1;
                if (o1.isFile() && o2.isDirectory())
                    return 1;
                int id1 = Book.getByName(o1.getName()).id;
                int id2 = Book.getByName(o2.getName()).id;
                return id1 < id2 ? -1 : 1;
            });
            return fs;
        }

        List fileList = Arrays.asList(fs);
        Collections.sort(fileList, (Comparator<MyFile>) (o1, o2) -> {
            if (o1.isDirectory() && o2.isFile())
                return -1;
            if (o1.isFile() && o2.isDirectory())
                return 1;
            if (o1.isPdf() && o2.isPdf()) {
                String n1 = o1.getName().substring(0, o1.getName().indexOf("."));
                String n2 = o2.getName().substring(0, o2.getName().indexOf("."));
                if (isInteger(n1) && isInteger(n2)) {
                    int i1 = Integer.parseInt(n1);
                    int i2 = Integer.parseInt(n2);
                    if (i1 < 0)
                        i1 = -i1 + 10000;
                    if (i2 < 0)
                        i2 = -i2 + 10000;
                    return i1 < i2 ? -1 : 1;
                } else if (isInteger(n1)) {
                    if (n2.contains("-")) {
                        int ind = n2.indexOf("-");
                        if (n1.substring(0, ind).equals(n2.substring(0, ind))) {
                            return -1;
                        }
                        return n1.substring(0, ind).compareTo(n2.substring(0, ind));
                    }
                } else if (isInteger(n2)) {
                    if (n1.contains("-")) {
                        int ind = n1.indexOf("-");
                        if (n1.substring(0, ind).equals(n2.substring(0, ind))) {
                            return 1;
                        }
                        return n1.substring(0, ind).compareTo(n2.substring(0, ind));
                    }
                }
            }

            if (o1.getName().equals(Constant.SUBJOIN_DIR_NAME)) {
                return 1;
            }
            if (o2.getName().equals(Constant.SUBJOIN_DIR_NAME)) {
                return -1;
            }

            return o1.getName().compareTo(o2.getName());
        });
        return fs;
    }

    public MyFile searchFile(Hymn hymn) {
        try {
            if (hymn.getFilePath() != null && hymn.getFilePath().length() > 0) {
                MyFile ft = MyFile.from(hymn.getFilePath());
                if (ft.exists()) {
                    return ft;
                }
            }

            String path = SdCardTool.getLbPath() + File.separator + hymn.getBook().FullName + File.separator;
            File qf = new File(path);
            if (qf.exists()) {
                String p2 = "";
                if (hymn.getIndex1() < 0) {
                    if (new File(path + "-").exists())
                        p2 = path + "-" + File.separator;
                } else if (new File(path + hymn.getIndex1() / 100).exists())
                    p2 = path + hymn.getIndex1() / 100 + File.separator;
                else if (new File(path + "0" + hymn.getIndex1() / 100).exists())
                    p2 = path + "0" + hymn.getIndex1() / 100 + File.separator;
                if (p2.length() > 0) {
                    String p3 = p2 + String.format("%03d", hymn.getIndex1());
                    if (hymn.getIndex2() > 1) {
                        p3 += "-" + hymn.getIndex2();
                    }
                    p3 += ".pdf";
                    if (!new File(p3).exists())
                        p3 = "";
                    if (p3.length() > 0) {
                        hymn.setFilePath(p3);
                        hymn.update();
                        return MyFile.from(p3);
                    }
                }
            }

            Logger.info("slow search " + hymn.toString());
            MyFile df = MyFile.from(SdCardTool.getLbPath());
            Book bk = hymn.getBook();
            for (MyFile f : df.listFiles()) {
                if (f.isDirectory() && bk.FullName.equals(f.getName())) {
                    df = f;
                    break;
                }
            }
            MyFile res = _dfsFile(df, hymn.getIndex1(), hymn.getIndex2());
            if (res != null && res.exists()) {
                hymn.setFilePath(res.getAbsolutePath());
                hymn.update();
            }
            return res;
        } catch (Exception e) {
            Logger.exception(e);
            return null;
        }
    }

    private MyFile _dfsFile(MyFile f, int index, int index2) {
        for (MyFile nf : f.listFiles()) {
            if (nf.isDirectory()) {
                MyFile fd = _dfsFile(nf, index, index2);
                if (fd != null) {
                    return fd;
                }
            } else {
                if (nf.isPdf()) {
                    String name = nf.getName().substring(0, nf.getName().length() - 4);
                    if (index2 == 2) {
                        if (name.contains("-2")) {
                            name = name.substring(0, name.length() - 2);
                        }
                    }

                    try {
                        if (Service.isInteger(name) && Integer.parseInt(name) == index)
                            return nf;
                    } catch (Exception e) {
                        Logger.exception(e);
                    }
                }
            }
        }
        return null;
    }

    public String nearContent(String content, String ss, int len) {
        String s1 = nearContent0(content, ss, len);
        if (s1.length() == 0)
            return s1;
        if (s1.contains("\n")) {
            String[] arr = s1.split("\n");
            if (arr[0].trim().length() < 3)
                arr[0] = "";
            if (arr[arr.length - 1].trim().length() < 3)
                arr[arr.length - 1] = "";
            return String.join("\n", arr).trim();
        }
        return s1;
    }

    private String nearContent0(String content, String ss, int len) {
        int ind = content.indexOf(ss);
        int st = ind;
        int ed = ind + ss.length();
        if (st > len)
            st -= len;
        else
            st = 0;
        if (ed + len >= content.length())
            ed = content.length();
        else
            ed += len;
        return trimChar(content.substring(st, ed));
    }

    private String trimChar(String s) {
        s = s.trim();
        while (Constant.ChineseChar.contains(s.charAt(0) + "")) {
            s = s.substring(1);
        }
        while (Constant.ChineseChar.contains(s.charAt(s.length() - 1) + "")) {
            s = s.substring(0, s.length() - 1);
        }
        return s.trim();
    }

    public boolean copyFile(MyFile oldFile, MyFile newFile) {
        if (newFile.exists())
            return true;

        if (!newFile.getParentFile().exists())
            newFile.getParentFile().getfile().mkdirs();

        try {
            FileInputStream fileInputStream = new FileInputStream(oldFile.getAbsolutePath());
            FileOutputStream fileOutputStream = new FileOutputStream(newFile.getAbsolutePath());
            byte[] buffer = new byte[1024];
            int byteRead;
            while (-1 != (byteRead = fileInputStream.read(buffer))) {
                fileOutputStream.write(buffer, 0, byteRead);
            }
            fileInputStream.close();
            fileOutputStream.flush();
            fileOutputStream.close();
            return true;
        } catch (Exception e) {
            Logger.exception(e);
            return false;
        }
    }

    public String getNameAfterDeal(MyFile file, boolean withExtend) {
        String res = file.getName();
        if (file.getName().startsWith("-"))
            res = res.replace("-", "附");

        String p = file.getAbsolutePath();
        for (Book bk : Book.getAll()) {
            if (p.contains(bk.FullName)) {
                res = bk.SimpleName + res;
                break;
            }
        }

        if (!withExtend) {
            if (file.isPdf())
                res = res.substring(0, res.length() - 4);
        }
        Hymn hymn = file.getHymn();
        if (hymn != null && hymn.getTitle() != null && hymn.getTitle().length() > 0) {
            res = res.toUpperCase().replace(".PDF", "") + " " + hymn.getTitle();
            //+ ".pdf";
            if (withExtend)
                res += ".pdf";
        }

        return res;
    }

    public void CB(String str, Context context) {
        ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        cm.setText(str);
    }

    public String[] getResStatString() {
        TC.begin("getResStatString");
        ArrayList<String> arr = new ArrayList<>();

        for (Book bk : Book.getAll()) {
            try {
                String rt = "";
                String[] sqls = new String[3 + ContentTypeD.getAll().length];
                sqls[0] = "select count(1) from " + HymnD.class.getSimpleName() + " where bookid=" + bk.id + " and (title is not null and length(title)>0)";

                sqls[1] = "select distinct hymnId from " + AuthorRelatedD.class.getSimpleName() + " where hymnId in (select id from " + HymnD.class.getSimpleName()
                        + " where bookid =" + bk.id + ")";
                sqls[1] = "select count(1) from (" + sqls[1] + ")";
                sqls[2] = "select count(1) from " + HymnD.class.getSimpleName() + " where bookid=" + bk.id + " and (lyric is not null and length(lyric)>0)";
                for (int i = 0; i < ContentTypeD.getAll().length; i++)
                    sqls[3 + i] = "select count(1) from " + ContentD.class.getSimpleName()
                            + " where value is not null and length(value)>0 and typeId=" + ContentTypeD.getAll()[i].id + " and hymnId in (select id from " + HymnD.class.getSimpleName()
                            + " where bookid =" + bk.id + ")";

                int n;
                int[] res = DBHelper.execSQL_Is(sqls);
                if (res[0] != 0) {
                    rt += bk.FullName;
                    //
                    TC.begin("pdf");
                    int pdfN = SdCardTool.getNum(bk, ".PDF", true);
                    TC.end("pdf");
                    if (pdfN > 0)
                        rt += " 蓝版:" + pdfN;
                    //
                    //rt += " 标题:" + res[0];
                    //
                    if (res[1] > 0)
                        rt += " 作者:" + res[1];
                    if (res[2] > 0)
                        rt += " 歌词:" + res[2];

                    n = 3;
                    for (int i = 0; i < ContentTypeD.getAll().length; i++) {
                        if (res[n] > 0)
                            rt += " " + ContentTypeD.getAll()[i].name + ":" + res[n];
                        n++;
                    }
                    //
                    TC.begin("mp3");
                    int mp3N = SdCardTool.getNum(bk, ".MP3", false);
                    TC.end("mp3");
                    if (mp3N > 0)
                        rt += " mp3:" + mp3N;
                    //
                    arr.add(rt.trim());
                }
            } catch (Exception e) {
                Logger.exception(e);
            }
        }
        int resA = DBHelper.execSQL_I("select count(1) from " + AuthorD.class.getSimpleName());
        arr.add("作者（详情）:" + resA);


        String[] r = SdCardTool.getResFileList();
        if (r.length > 0) {
            String p = "资源文件列表：" + String.join(";", r);
            arr.add(p);
        }

        TC.end("getResStatString");
        TC.log();
        arr.add("现在大部分资源都收集好了：大本诗歌资源最全，补充本次之，可能暂时不会再完善了。MP3基本都有，有一些MP3仅是伴奏，或是普通录音，若有好的资源，可以提供给作者。");
        return arr.toArray(new String[0]);
    }

    public void openByIE(Context ct, String url) {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        Uri content_url = Uri.parse(url);
        intent.setData(content_url);
        intent.setClassName("com.android.browser", "com.android.browser.BrowserActivity");
        ct.startActivity(intent);
    }

    /**
     * 注意内部文件（夹）的名字不能为中文
     *
     * @param target
     * @param source
     * @return
     */
    public int unzip(String target, String source, String[] msg) throws IOException {
        long t1 = System.currentTimeMillis();
        int res = 0;
        String fn = "";

        File file = new File(source);
        if (!file.exists()) {
            return res;
        }

        ZipFile zipFile = new ZipFile(file);
        ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(file));
        ZipEntry zipEntry;
        while ((zipEntry = zipInputStream.getNextEntry()) != null) {
            String fileName = zipEntry.getName();
            File temp = new File(target + File.separator + fileName);
            if (zipEntry.isDirectory()) {
                File dir = new File(target + File.separator + fileName);
                dir.mkdirs();
                continue;
            }
            if (temp.getParentFile() != null && !temp.getParentFile().exists()) {
                temp.getParentFile().mkdirs();
            }
            if (temp.exists()) {
                msg[0] += "已存在'" + temp.getName() + "'跳过";
                continue;
            }
            fn = fileName;
            byte[] buffer = new byte[1024];
            try (OutputStream os = new FileOutputStream(temp);
                 InputStream is = zipFile.getInputStream(zipEntry)) {
                int len;
                while ((len = is.read(buffer)) != -1) {
                    os.write(buffer, 0, len);
                }
                res++;
            }
        }
        zipInputStream.close();
        MainActivity.msgWait = "解压" + source + "完成\r\n耗时：" + (System.currentTimeMillis() - t1) / 1000 + "秒";
        for (Book bk : Book.getAll())
            bk.renamePinYin();
        return res;
    }

    /**
     * 随机一首有MP3的诗歌pdf
     *
     * @return
     */
    public MyFile getRandomMp3File() {
        try {
            int n = 0;
            while (n++ < 3) {
                MyFile mp3 = getRandomMp3();
                String p = mp3.getAbsolutePath().replace(".mp3", ".pdf");
                for (Book bk : Book.getAll()) {
                    String sp = File.separator + bk.SimpleName.toLowerCase() + File.separator;
                    if (p.contains(sp))
                        p = p.replace(sp, File.separator + bk.FullName + File.separator);
                    if (new File(p).exists())
                        return MyFile.from(p);
                }
            }
            return null;
        } catch (Exception e) {
            Logger.exception(e);
            return null;
        }
    }

    ArrayList<MyFile> mp3List = null;

    private MyFile getRandomMp3() {
        if (mp3List == null) {
            mp3List = new ArrayList<>();
            String path = SdCardTool.getLbPath();
            dfsMp3(MyFile.from(path), mp3List);
        }
        Random rd = new Random();
        return mp3List.get(rd.nextInt(mp3List.size()));
    }

    private void dfsMp3(MyFile f, ArrayList<MyFile> list) {
        if (f.isFile())
            if (f.getName().toLowerCase().endsWith(".mp3"))
                list.add(f);
        for (MyFile fn : f.listFiles())
            dfsMp3(fn, list);
    }

    /**
     * 检查版本服务，无返回值
     *
     * @param ct
     */
    public void checkVersion(Context ct) {
        try {
            String res = ct.getPackageManager().getPackageInfo(ct.getPackageName(), 0).versionName;
            String[] vs = UpdateHistory.VERSION_HISTORY;
            for (String v : vs) {
                String[] va = v.split(" ");
                if (va.length <= 2)
                    throw new RuntimeException("更新历史异常");
                if (!va[1].equals("temp")) {
                    if (va[1].equals(res))
                        return;
                    throw new RuntimeException("版本号异常");
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            Logger.exception(e);
            throw new RuntimeException("获取版本号异常");
        }
    }

    public void forceUpdate(Activity activity) {
        Logger.info("强制同步资源");
        String path = SdCardTool.getLbPath();
        updateChildHymnIndex(path);
        path = SdCardTool.copyIfNotExist(activity, true);
        LoadProcess.process = LOAD_ENUM.SCAN_ADD;
        Logger.info("step 1:扫描附加包");
        try {
            //有手机(荣耀30s 鸿蒙系统)在这一步闪退了，记个日志
            String mp3N = Service.getC().scanAddedFile();
            if (mp3N.length() > 0) {
                Logger.info("新增文件：" + mp3N);
            }
        } catch (Exception e) {
            Logger.exception(e);
            throw new RuntimeException(e.getMessage());
        }
        Logger.info("step 2:" + (path.length() > 0 ? "找到蓝版资源" : "未找到蓝版资源"));
        if (path.length() > 0) {
            LoadProcess.process = LOAD_ENUM.PRE_DEAL_FILE;
            Logger.info("step 3:开始预处理");
            Service.getC().predealDir(new File(path));
            Logger.info("step 4:开始加载资源文件");
            MyFile fa = MyFile.from(path);

            try {
                LoadProcess.process = LOAD_ENUM.LOAD_RES;
                Service.getC().loadResDir(activity);
            } catch (Exception e) {
                Logger.exception(e);
            }
        }
    }

    public String getFirstHymnPath() {
        return SdCardTool.getLbPath() + File.separator + Book.DaBen.FullName + File.separator + "0" + File.separator + "001.pdf";
    }

    /**
     * 自检
     */
    public String checkSelf() {
        try {
            if (!new File(getFirstHymnPath()).exists())
                return "找不到诗歌pdf，请检查'诗歌蓝版'文件夹的位置或内容有无问题。如果你下载的是升级包，请再去下载pdf附加包。如果已下载，但无法自动加载，请将下载的附加包移动到储存卡根目录。";
            //if (SearchIndexD.getCount() < 100)
            //    return "诗歌数量异常，请检查'诗歌蓝版'文件夹的位置或内容有无问题";

            if (LetterD.getAll().length != 66)
                return "未找到圣经各卷书名的资源文件或文件内容异常";
            MyFile f = MyFile.from(SdCardTool.getLbPath());
            if (!f.exists() || !f.isDirectory() || f.listFiles().length == 0)
                return "找不到蓝版诗歌文件夹或者文件夹里没有文件，请确认是否移动了文件夹或者是只下载了升级包";

            updateYisitie();
            Logger.info("自检完成");
            return "";
        } catch (Exception e) {
            Logger.exception(e);
            return "";
        }
    }

    /**
     * 备份标签
     *
     * @param overwrite 是否覆盖
     */
    public boolean bakLabel(boolean overwrite) {
        LabelType[] lts = LabelType.getAll();
        StringBuilder sb = new StringBuilder();
        for (LabelType lt : lts) {
            sb.append(lt.getShowName()).append(":");
            sb.append(String.join(";", Label.getHymnIndexsByTypeId(lt.getId())));
            sb.append("\r\n");
        }

        String path = SdCardTool.getResPath() + File.separator + Label.BAK_FILE;
        return SdCardTool.writeToFile(path, sb.toString(), overwrite ? FILE_OVERWRITE : FILE_NO_OVERWRITE);
    }

    /**
     * 获取文件夹的播放列表
     */
    public List<String> getDirectoryMp3List() {
        List<String> res = new ArrayList<>();
        for (Book book : Book.getAll()) {
            if (book.getMp3Directory() != null)
                res.add(book.id + ";" + book.FullName);//+ "(" + book.getMp3Count() + ")");
        }
        return res;
    }

    /**
     * 获取标签的播放列表
     */
    public List<String> getLableMp3List() {
        List<String> res = new ArrayList<>();

        for (LabelType lt : LabelType.getAll()) {
            //if (lt.getMp3Count() > 0)
            res.add("-" + lt.getId() + ";" + lt.getName());//+ "(" + lt.getMp3Count() + ")");
        }
        return res;
    }

    private String hymnBookSimpleName(HymnD hymnD) {
        for (Book bk : Book.getAll())
            if (bk.id == hymnD.bookId)
                return bk.SimpleName;
        return "U";
    }

    /**
     * @return key=mp3Path, value=hymnName
     */
    public HashMap<String, String> getHymnNameBat(Book book) {
        HashMap<String, String> res = new HashMap<>();
        try {
            HymnD[] hymnDS = HymnD.getByBookId(book.id);
            HashMap<String, HymnD> m = new HashMap<>();
            for (HymnD h : hymnDS) {
                if (h.index2 > 1) {
                    m.put(hymnBookSimpleName(h) + h.index1 + "-" + h.index2, h);
                } else {
                    m.put(hymnBookSimpleName(h) + h.index1 + "", h);
                    if (h.index1 < 10)
                        m.put(hymnBookSimpleName(h) + "00" + h.index1, h);
                    else if (h.index1 < 100)
                        m.put(hymnBookSimpleName(h) + "0" + h.index1, h);
                }
            }

            for (Book b : Book.getAll()) {
                if (b.id == book.id || book.id == Book.ALL.id) {
                    MyFile dir = b.getMp3Directory();
                    if (dir == null) continue;
                    for (MyFile d : dir.listFiles()) {
                        for (MyFile f : d.listFiles()) {
                            if (!f.isMp3())
                                continue;
                            String key = b.SimpleName + f.getName().split("\\.")[0];
                            if (m.containsKey(key)) {
                                res.put(f.getAbsolutePath(), Hymn.fromDao(m.get(key)).getShowName());
                            } else
                                Logger.info("not find key:" + key);
                        }
                    }
                }
            }
            return res;
        } catch (Exception e) {
            Logger.exception(e);
            return res;
        }
    }

    /**
     * 导出所有诗歌信息到目标文件夹下
     */
    public void outputRes(String path) {
        MyFile dirPath = MyFile.from(path);
        dirPath.mkdirs();
        String sp = "\r\n";

        for (Book book : Book.getAll()) {
            Hymn[] hymns = Hymn.getByBook(book);
            Logger.info("获取" + book.FullName + "完成");
            StringBuilder sb = new StringBuilder();
            sb.append(sp);
            for (Hymn hymn : hymns) {
                sb.append("序号：").append(hymn.toString()).append(sp);
                sb.append("标题：").append(hymn.getTitle()).append(sp);
                if (hymn.getLyric() != null && hymn.getLyric().length() > 0)
                    sb.append("歌词：").append(hymn.getLyric()).append(sp);
                if (hymn.getWhitePage() > 0) {
                    if (hymn.getWhitePdf() != null && hymn.getWhitePdf().length() > 0)
                        sb.append("白版：").append(hymn.getWhitePdf()).append(" ").append(hymn.getWhitePage()).append(sp);
                    else
                        sb.append("白版：").append(hymn.getWhitePage()).append(sp);
                }
                Author[] la = hymn.getLyricAuthors();
                Author[] ma = hymn.getMusicAuthors();
                if (la.length > 0 || ma.length > 0) {
                    sb.append("作者：");
                    for (Author a : la) {
                        sb.append("Lyric:").append(a.getName()).append(" ");
                    }
                    for (Author a : ma) {
                        sb.append("Music:").append(a.getName()).append(" ");
                    }
                    sb.append(sp);
                }
                for (Content content : hymn.getContents()) {
                    sb.append(content.getTypeString()).append("：").append(content.getValue()).append(sp);
                }
            }
            SdCardTool.writeToFile(dirPath.getAbsolutePath() + File.separator + book.FullName + ".txt", sb.toString(), SdCardTool.FILE_OVERWRITE);
            Logger.info("写入" + book.FullName + "完成");
        }
    }

    /**
     * 清除缓存及过期文件
     *
     * @return 清理的结果
     */
    public String clearCache() {
        //分享文件
        MyFile share = MyFile.from(SdCardTool.getSharePath());
        long sum = 0;
        int num = 0;
        int otherNum = 0;
        for (MyFile f : share.listFiles()) {
            if (f.isPdf() || f.isMp3()) {
                num++;
                sum += f.length();
                f.delete();
            } else if (f.isFile()) {
                otherNum++;
            }
        }
        String res = "";
        if (num > 0)
            res += "发现分享pdf" + num + "个，大小" + (sum / 1000) + "KB，已清理";
        if (otherNum > 0)
            res += "\r\n发现其他文件" + otherNum + "个，请手动清理";
        //过期日志（超过一周）
        sum = 0;
        num = 0;
        long dt = 7 * 24 * 3600 * 1000;
        for (MyFile f : MyFile.from(SdCardTool.getLogPath()).listFiles()) {
            if (System.currentTimeMillis() - f.lastModified() > dt) {
                num++;
                sum += f.length();
                f.delete();
            }
        }
        if (num > 0)
            res += "\r\n发现过期日志" + num + "个，大小" + (sum / 1000) + "KB，已清理";
        //过期资源文件 删除序号小于MIN_RES_INDEX的资源文件
        sum = 0;
        num = 0;
        for (MyFile f : MyFile.from(SdCardTool.getResPath()).listFiles()) {
            if (f.getName().length() > 4 && f.getName().indexOf(".") == 3) {
                try {
                    String ns = f.getName().substring(0, 3);
                    if (Integer.parseInt(ns) < UpdateHistory.MIN_RES_INDEX) {
                        num++;
                        sum += f.length();
                        f.delete();
                    }
                } catch (Exception e) {
                }
            }
        }
        //删除过期的蓝版pdf（因新资源包）
        String ddir = "";
        for (MyFile f : MyFile.from(SdCardTool.getLbPath()).listFiles()) {
            if (f.isDirectory() && f.getName().toLowerCase().contains("hidein")) {
                if (f.deleteForce())
                    ddir += "删除过期文件夹" + f.getName();
            }
        }
        if (num > 0)
            res += "\r\n发现无用资源文件" + num + "个，大小" + (sum / 1000) + "KB，已清理";
        if (ddir.length() > 0)
            res += "\r\n" + ddir;
        if (res.length() == 0)
            res = "没有需要清理的文件";
        return res.trim();
    }

    public String getNewOtherBookIndex() {
        Hymn[] hymns = Hymn.getByBook(Book.Other);
        HashSet<String> hs = new HashSet<>();
        for (Hymn h : hymns)
            hs.add(String.valueOf(h.getIndex1()));
        for (int i = 1; i < 1000; i++)
            if (!hs.contains(String.valueOf(i)))
                return String.valueOf(i);
        return "";
    }

    public String[] allAuthorNames() {
        Author[] as = Author.getAll();
        int an = 0;
        int in = 0;
        ArrayList<String> ss = new ArrayList<>();
        for (Author a : as) {
            String s = a.getName();
            if (a.getAge() != null && a.getAge().length() > 0) {
                s += " A";
                an++;
            }
            if (a.getIntroduction() != null && a.getIntroduction().length() > 0) {
                s += " I";
                in++;
            }
            ss.add(s);
        }
        Collections.sort(ss);
        ss.add(0, "共" + as.length + "位作者，" + an + "个生平信息，" + in + "个简介");
        return ss.toArray(new String[0]);
    }

    private void updateChildHymnIndex(String path) {
        try {
            Logger.info("检查儿童诗歌(之前的数据有错误)");
            MyFile f = MyFile.from(path);
            if (f.exists()) {
                for (MyFile fn : f.listFiles()) {
                    if (fn.getName().equals("儿童诗歌")) {
                        dfsRN(fn);
                        if (rnCount > 0)
                            Logger.info("处理异常文档：" + rnCount);
                        else
                            Logger.info("文档无异常");
                        return;
                    }
                }
            }
        } catch (Exception e) {
            Logger.exception(e);
        }
    }

    /**
     * 之前错误把‘以斯帖记’写为‘以斯拉记’导致经节和三旧一新错误
     */
    private void updateYisitie() {
        try {
            boolean hasFound = false;
            for (LetterD d : LetterD.getAll()) {
                if (d.fullName.equals("以斯拉记")) {
                    if (!hasFound)
                        hasFound = true;
                    else {
                        Logger.info("发现错误数据：书卷名异常");
                        DBHelper.execSQL("update letterD set fullName='以斯帖记' where id=" + d.id);
                    }
                }
            }
        } catch (Exception e) {
            Logger.exception(e);
        }
    }

    private int rnCount = 0;

    private void dfsRN(MyFile f) {
        if (f.isDirectory()) {
            for (MyFile fn : f.listFiles())
                dfsRN(fn);
        } else {
            if (f.isPdf()) {
                if (f.getName().length() == "0001.pdf".length() && f.getName().startsWith("0")) {
                    String newP = f.getAbsolutePath().replace(f.getName(), f.getName().substring(1));
                    File newF = new File(newP);
                    rnCount++;
                    if (newF.exists())
                        f.delete();
                    else
                        f.renameTo(newF);
                }
            }
        }
    }

    public void setTheme(AppCompatActivity activity) {
        int set = Setting.getValueI(Setting.APP_THEME);
        if (set == 0)
            activity.setTheme(R.style.BigTheme);
        else if (set == 1)
            activity.setTheme(R.style.AlphaTheme);
        else if (set == 2)
            activity.setTheme(R.style.ColorfulTheme);
        else
            activity.setTheme(R.style.SmallTheme);
    }

    public String getDebugMsg(Activity activity) {
        //是否赋予权限
        boolean permission = !noPer(activity);
        //已加载的资源
        MyFile lbDir = MyFile.from(SdCardTool.getLbPath());
        boolean isDir = lbDir.isDirectory();
        boolean hasDir = isDir && lbDir.listFiles().length > 0;
        boolean hasMp3 = isDir && lbDir.dirHasMp3();
        boolean hasPdf = isDir && lbDir.dirHasPdf();

        String sp = "\r\n";
        return "安卓系统:" + android.os.Build.VERSION.RELEASE + sp
                + "手机型号:" + android.os.Build.MODEL + sp
                + "生产厂家:" + android.os.Build.BRAND + sp
                + "读写权限:" + permission + sp
                + "获得诗歌蓝版文件夹:" + isDir + sp
                + "文件夹:" + hasDir + " MP3:" + hasMp3 + " pdf:" + hasPdf + sp
                + "app版本:" + getVersionStr(activity);
    }


    /**
     * 专门用于 Setting.RES_UPDATE_RECORD 的资源数统计
     */
    private int dotNum(String s) {
        int res = 0;
        for (int i = 0; i < s.length() - 2; i++)
            if (s.charAt(i) == 't' && s.charAt(i + 1) == 'x' && s.charAt(i + 2) == 't')
                res++;
        return res;
    }

    /**
     * 没有读写权限（一般是第一次打开）
     */
    public boolean noPer(Activity activity) {
        int permission = ActivityCompat.checkSelfPermission(activity,
                "android.permission.WRITE_EXTERNAL_STORAGE");
        return permission != PackageManager.PERMISSION_GRANTED;
    }

    /**
     * pdf显示异常的解决提示
     */
    public String getFixFunctions() {
        String sp = "\r\n";
        return "1:如果是第一次使用且使用的是升级包，请下载pdf附加包(加百度网盘好友后,作者会在其中分享附加包,下载后无需解压,打开app会自动加载资源)" + sp +
                "2:重启app" + sp +
                "3:检查储存卡下是否有'诗歌蓝版'文件夹，且有文件";
    }

    /**
     * 获取MP3列表（诗歌本，标签）
     *
     * @return
     */
    public List<String> getMp3C() {
        List<String> mp3List = getDirectoryMp3List();
        if (mp3List.size() == 0)
            return new ArrayList<>();
        mp3List.addAll(getLableMp3List());
        mp3List.add(0, Book.ALL.id + ";" + Book.ALL.FullName);
        return mp3List;
    }

    /**
     * 网上版本和当前版本比较
     *
     * @param webVer
     * @param currentVer
     * @return
     */
    public boolean compareVersion(String webVer, String currentVer) {
        try {
            String[] sp1 = webVer.split("\\.");
            String[] sp2 = currentVer.split("\\.");
            for (int i = 0; i < sp1.length && i < sp2.length; i++) {
                if (Integer.parseInt(sp1[i]) > Integer.parseInt(sp2[i])) {
                    return true;
                }
                if (Integer.parseInt(sp1[i]) < Integer.parseInt(sp2[i])) {
                    Logger.info("你拿到的是内测版吧");
                    return false;
                }
            }
        } catch (Exception e) {
            Logger.exception(e);
        }
        return false;
    }
}
