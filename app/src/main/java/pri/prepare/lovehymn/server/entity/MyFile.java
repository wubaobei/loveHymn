package pri.prepare.lovehymn.server.entity;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

import pri.prepare.lovehymn.server.Service;
import pri.prepare.lovehymn.server.dal.ContentTypeD;
import pri.prepare.lovehymn.server.dal.HymnD;
import pri.prepare.lovehymn.server.function.CommonTool;
import pri.prepare.lovehymn.server.function.SdCardTool;

/**
 * 封装一层
 */
public class MyFile extends File {
    private MyFile(String path) {
        super(path);
    }

    public static String[] readStream(InputStream is) {
        try {
            ArrayList<String> contents = new ArrayList<>();
            InputStreamReader reader = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(reader);
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().length() > 0 && (!line.startsWith("//")))
                    contents.add(line.trim());
            }
            return contents.toArray(new String[0]);
        } catch (Exception e) {
            Logger.exception(e);
            return new String[0];
        }
    }

    public String key() {
        return getAbsolutePath();
    }

    public static MyFile from(String path) {
        return new MyFile(path);
    }

    @Override
    @NonNull
    public MyFile[] listFiles() {
        File[] s = super.listFiles();
        if (s == null) {
            return new MyFile[0];
        }
        MyFile[] res = new MyFile[s.length];
        for (int i = 0; i < s.length; i++)
            res[i] = MyFile.from(s[i].getAbsolutePath());
        return res;
    }

    @NonNull
    public MyFile[] listFilesOrderIndexDesc() {
        MyFile[] res = listFiles();
        CommonTool.getC().ArraySort(res, CommonTool.HYMN_INDEX_COMPARE);
        return res;
    }

    @Override
    public MyFile getParentFile() {
        return MyFile.from(super.getParentFile().getAbsolutePath());
    }

    public boolean isPdf() {
        return getName().toUpperCase().endsWith(".PDF");
    }

    public File getfile() {
        return this;
    }

    public String[] getContent() {
        ArrayList<String> contents = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(getfile()))) {
            String tempString;
            while ((tempString = reader.readLine()) != null) {
                if (tempString.trim().length() == 0)
                    continue;
                contents.add(tempString.trim());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return contents.toArray(new String[0]);
    }

    private List<MyFile> mp3List = null;

    public boolean dirHasMp3() {
        for (MyFile f : listFiles()) {
            if (f.isDirectory()) {
                if (f.dirHasMp3())
                    return true;
            } else if (f.isMp3())
                return true;
        }
        return false;
    }

    public boolean dirHasPdf() {
        for (MyFile f : listFiles()) {
            if (f.isDirectory()) {
                if (f.dirHasPdf())
                    return true;
            } else if (f.isPdf())
                return true;
        }
        return false;
    }

    /**
     * 获取MP3列表（降序）
     *
     * @return
     */
    public List<MyFile> getMp3List() {
        if (isFile()) {
            return new ArrayList<>();
        }

        if (mp3List == null) {
            mp3List = new ArrayList<>();

            if (getName().equals(SdCardTool.getLbPath())) {
                Logger.info("lb");
                return mp3List;
            }

            List<MyFile> temp = new ArrayList<>();

            for (MyFile d : listFilesOrderIndexDesc())
                if (!d.isFile()) {
                    if (d.getName().equals("-")) {
                        //附 的诗歌MP3放最后
                        for (MyFile f : d.listFilesOrderIndexDesc())
                            if (f.isMp3()) {
                                temp.add(f);
                            }
                    } else
                        for (MyFile f : d.listFilesOrderIndexDesc())
                            if (f.isMp3()) {
                                mp3List.add(f);
                            }
                }
            for (int i = temp.size() - 1; i >= 0; i--)
                mp3List.add(0, temp.get(i));
        }
        return mp3List;
    }

    public boolean isMp3() {
        return isFile() && getName().endsWith(".mp3");
    }

    public MyFile getPdf() {
        if (!isFile())
            return null;
        if (isPdf())
            return this;
        String p = getAbsolutePath().replace(".mp3", ".pdf");
        for (Book bk : Book.getAll()) {
            if (p.contains(File.separator + bk.SimpleName + File.separator)) {
                String p2 = p.replace(File.separator + bk.SimpleName + File.separator, File.separator + bk.FullName + File.separator);
                if (new File(p2).exists())
                    return MyFile.from(p2);
            } else if (p.contains(File.separator + bk.SimpleName.toLowerCase() + File.separator)) {
                String p2 = p.replace(File.separator + bk.SimpleName.toLowerCase() + File.separator, File.separator + bk.FullName + File.separator);
                if (new File(p2).exists())
                    return MyFile.from(p2);
            } else if (p.contains(bk.FullName + "mp3")) {
                String p1 = p.replace(bk.FullName + "mp3", bk.FullName);
                if (new File(p1).exists())
                    return MyFile.from(p1);
            }
        }

        return null;
    }
    public MyFile getMp3(){
        return getMp3(true);
    }

    public MyFile getMp3(boolean search) {
        if (!isFile())
            return null;
        String p = getAbsolutePath().replace(".pdf", ".mp3");
        for (Book bk : Book.getAll()) {
            if (p.contains(bk.FullName)) {
                String p2 = p.replace(bk.FullName, bk.SimpleName);
                if (new File(p2).exists()) {
                    return MyFile.from(p2);
                }
            }
        }

        Hymn h = getHymn();
        if (search)
            try {
                for (Content c : h.getContents()) {
                    if (c.isType(ContentTypeD.getSameSongType())) {
                        String[] co = Service.getC().correctOrders(c.getValue());
                        for (String otherHymn : co) {
                            Hymn oh = Hymn.search(otherHymn);
                            MyFile mp3 = oh.getFile().getMp3(false);
                            if (mp3 != null)
                                return mp3;
                        }
                    }
                }
            } catch (Exception e) {

            }

        return null;
    }

    public Hymn getHymn() {
        if (isPdf() || isMp3()) {
            if (!Hymn.existCache(key())) {
                Hymn h = getHymn0();
                Hymn.addCache(key(), h);
            }
            Hymn h = Hymn.getCache(key());
            if (h == null)
                return null;
            if (isPdf())
                if (h.getFilePath() == null || h.getFilePath().length() == 0) {
                    h.setFilePath(getAbsolutePath());
                    try {
                        h.update();
                        //Logger.info("更新" + h.toString() + "的路径完成");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            return h;
        }
        return null;
    }

    private Hymn getHymn0() {
        Book bk = getBook(this);
        if (bk == null) {
            Logger.info("no book");
            return null;
        }
        if (isDirectory()) {
            Logger.info("no dir");
            return null;
        }
        try {
            String s = getName().split("\\.")[0];
            int ind2 = 1;
            if (s.indexOf("-") > 0) {
                ind2 = Integer.parseInt(s.substring(s.indexOf("-") + 1));
                s = s.substring(0, s.indexOf("-"));
            }
            int ind = Integer.parseInt(s);
            return Hymn.search(bk, ind, ind2);
        } catch (Exception e) {
            Logger.exception(e);
            return null;
        }
    }

    private static Book getBook(MyFile f) {
        String path = f.getAbsolutePath();
        for (Book bk : Book.getAll()) {
            if (path.contains(bk.FullName + "/") || path.contains(bk.FullName + "\\")) {
                return bk;
            }
        }

        for (Book bk : Book.getAll()) {
            if (path.toUpperCase().contains("/" + bk.SimpleName + "/") || path.toUpperCase().contains("\\" + bk.SimpleName + "\\")) {
                return bk;
            }
        }
        return null;
    }

    public static void loadHymnBat(ArrayList<MyFile> showFileList) {
        ArrayList<HymnD> temp = new ArrayList<>();
        HashMap<Integer, String> idMap = new HashMap<>();
        HashSet<Integer> bookSet = new HashSet<>();

        while (true) {
            int thisId = -1;
            for (MyFile f : showFileList) {
                Book bk = getBook(f);
                if (bk == null)
                    continue;
                if (bookSet.contains(bk.id))
                    continue;
                thisId = bk.id;
                bookSet.add(bk.id);
                break;
            }
            if (thisId == -1)
                break;
            int min = 9999;
            int max = -1;
            for (MyFile f : showFileList) {
                Book bk = getBook(f);
                if (bk.id != thisId)
                    continue;
                if (f.isDirectory())
                    continue;
                String s = f.getName().split("\\.")[0];
                if (s.indexOf("-") > 0) {
                    s = s.substring(0, s.indexOf("-"));
                }
                int ind = Integer.parseInt(s);
                if (ind > max)
                    max = ind;
                if (ind < min)
                    min = ind;
            }
            if (max < 0)
                return;
            HymnD[] hs = new HymnD[0];
            try {
                hs = HymnD.getByRange(thisId, min, max);
            } catch (IllegalAccessException e) {
                Logger.exception(e);
            }
            for (MyFile f : showFileList) {
                Book bk = getBook(f);
                if (bk == null)
                    continue;
                if (f.isDirectory())
                    continue;
                try {
                    String s = f.getName().split("\\.")[0];
                    int ind2 = 1;
                    if (s.indexOf("-") > 0) {
                        ind2 = Integer.parseInt(s.substring(s.indexOf("-") + 1));
                        s = s.substring(0, s.indexOf("-"));
                    }
                    int ind = Integer.parseInt(s);
                    HymnD hymn = null;//HymnD.getByIndex(bk.id, ind, ind2);
                    for (HymnD h : hs) {
                        if (h.index1 == ind && h.bookId == bk.id && h.index2 == ind2) {
                            hymn = h;
                            break;
                        }
                    }
                    if (hymn != null) {
                        temp.add(hymn);
                        idMap.put(hymn.id, f.key());
                    }
                } catch (Exception e) {
                    Logger.exception(e);
                }
            }
        }

        try {
            Hymn[] hymns = Hymn.toArr(temp.toArray(new HymnD[0]));
            for (Hymn hymn : hymns) {
                if (idMap.containsKey(hymn.getId())) {
                    Hymn.addCache(idMap.get(hymn.getId()), hymn);
                }
            }
        } catch (Exception e) {
            Logger.exception(e);
        }
    }

    /**
     * 是安安卓隐藏文件夹
     */
    public boolean isHiddenDirectory() {
        return isDirectory() && getName().startsWith(".");
    }

    /**
     * 所属文件排序
     *
     * @return 隐藏-中文-数字-字母 文件夹-文件
     */
    public MyFile[] listFilesOrders() {
        MyFile[] list = listFiles();

        CommonTool.getC().ArraySort(list, CommonTool.FOLDER_FILE_COMPARE);
        return list;
    }

    public int compareValue() {
        int v = 0;
        if (isDirectory())
            v += 10;
        if (isHiddenDirectory())
            v += 5;

        if (getName().charAt(0) >= '0' && getName().charAt(0) <= '9')
            v += 2;
        else if (getName().charAt(0) >= 'a' && getName().charAt(0) <= 'z')
            v += 1;
        else if (getName().charAt(0) >= 'A' && getName().charAt(0) <= 'Z')
            v += 1;
        else if (getName().charAt(0) > 255)
            v += 3;

        return -v;
    }

    public boolean deleteForce() {
        if (isFile())
            return delete();
        for (MyFile f : listFiles()) {
            if (f.isFile())
                f.delete();
            else
                f.deleteForce();
        }
        return delete();
    }
}


