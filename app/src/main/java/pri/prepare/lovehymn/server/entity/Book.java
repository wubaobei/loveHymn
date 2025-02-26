package pri.prepare.lovehymn.server.entity;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pri.prepare.lovehymn.server.dal.BookD;
import pri.prepare.lovehymn.server.function.Constant;
import pri.prepare.lovehymn.server.function.SdCardTool;

public class Book {
    public int dbId;
    public final int id;
    public final String simpleName;
    public final String fullName;
    public final int maxLength;
    public final String pinYin;

    public Book(int id, String sn, String fn, int ml, String pinyin) {
        this.id = id;
        simpleName = sn.toUpperCase();
        fullName = fn;
        maxLength = ml;
        pinYin = pinyin;
    }

    public static final Book ALL = new Book(99, "A", "全部诗歌", 3, "suoyou");
    public static final Book DaBen = new Book(1, "D", "大本", 3, "daben");
    public static final Book BuChong = new Book(2, "B", "补充本", 4, "buchongben");
    public static final Book Chang = new Book(3, "C", "唱诗人", 3, "changshiren");
    public static final Book Xin = new Book(4, "X", "新歌颂咏", 3, "xingesongyong");
    public static final Book ErTong = new Book(6, "E", "儿童诗歌", 4, "ertongshige");
    public static final Book Other = new Book(7, "O", "其它", 3, "qita");

    public static Book[] getAll() {
        Book[] pbs = Book.getPrivateBooks();
        List<Book> temp = new ArrayList<>();
        temp.add(DaBen);
        temp.add(BuChong);
        temp.add(Chang);
        temp.add(Xin);
        temp.add(ErTong);
        temp.addAll(Arrays.asList(pbs));
        temp.add(Other);
        return temp.toArray(new Book[0]);
    }

    private static Book[] allCache = null;

    public static Book[] getPublicBooks() {
        return new Book[]{DaBen, BuChong, Chang, Xin, ErTong};
    }

    public static String[] getPublicBookNames() {
        return new String[]{DaBen.fullName, BuChong.fullName, Chang.fullName, Xin.fullName, ErTong.fullName};
    }

    public static Book[] getPrivateBooks() {
        if (allCache != null) {
            return allCache;
        }
        try {
            BookD[] bookDs = BookD.getAll();
            Book[] res = new Book[bookDs.length];
            for (int i = 0; i < bookDs.length; i++) {
                BookD d = bookDs[i];
                res[i] = new Book(d.privateId, d.simpleName, d.fullName, d.maxLength, d.pinYin);
                res[i].dbId = d.id;
            }
            Logger.info("自定义书的数量 " + res.length);
            allCache = res;
            return res;
        } catch (Exception e) {
            return new Book[0];
        }
    }

    public static Book getById(int bookId) {
        for (Book bk : getAll()) {
            if (bk.id == bookId) {
                return bk;
            }
        }
        if (bookId == ALL.id) {
            return ALL;
        }
        return null;
    }

    public static boolean addPrivateBook(int privateId, String simpleName, String fullName, int maxLength, String pinYin) {
        allCache = null;
        try {
            BookD d = new BookD();
            d.privateId = privateId;
            d.simpleName = simpleName;
            d.fullName = fullName;
            d.maxLength = maxLength;
            d.pinYin = pinYin;
            d.insert(false);
            return true;
        } catch (Exception e) {
            Logger.exception(e);
            return false;
        }
    }

    public static Book getByName(String name) {
        for (Book bk : getAll()) {
            if (bk.fullName.equals(name) || bk.simpleName.equals(name.toUpperCase())) {
                return bk;
            }
        }
        return new Book(-1, "找不到书", "找不到书", 3, "");
    }

    public static boolean isShortName(char c) {
        for (Book bk : getAll()) {
            if (bk.simpleName.charAt(0) == c || bk.simpleName.charAt(0) == c - 'a' + 'A') {
                return true;
            }
        }
        return false;
    }

    public MyFile getMp3Directory() {
        for (MyFile f : MyFile.from(SdCardTool.getLbPath()).listFiles()) {
            if (f.getName().equalsIgnoreCase(simpleName)) {
                return f;
            }
        }
        return null;
    }

    /**
     * 获取白版pdf
     *
     * @return
     */
    public File getWhiteFile(String pdf) {
        String p;
        if (pdf == null || pdf.length() == 0) {
            p = SdCardTool.getLbPath() + File.separator + Constant.WHITE + File.separator + simpleName + ".pdf";
        } else {
            p = SdCardTool.getLbPath() + File.separator + Constant.WHITE + File.separator + pdf;
        }
        File f;
        if ((f = new File(p)).exists()) {
            return f;
        }
        return null;
    }

    private int _mp3Count = -1;

    /**
     * 获取该诗歌本MP3文件数量（缓存）
     */
    public int getMp3Count() {
        if (_mp3Count < 0) {
            if (getMp3Directory() != null) {
                _mp3Count = getMp3Directory().searchFileByName(".mp3").size();
            } else {
                _mp3Count = 0;
            }
        }
        return _mp3Count;
    }

    public void renamePinYin() {
        String pyPath = SdCardTool.getLbPath() + File.separator + pinYin;
        String zwPath = SdCardTool.getLbPath() + File.separator + fullName;
        MyFile py = MyFile.from(pyPath);
        if (py.exists()) {
            File zw = new File(zwPath);
            if (zw.exists()) {
                Logger.info("合并" + pinYin + "->" + fullName);
                for (MyFile file : py.listFiles()) {
                    file.moveToFolder(zwPath, false);
                }
                py.deleteForce();
            } else {
                Logger.info("重命名" + pinYin + "->" + fullName);
                new File(pyPath).renameTo(new File(zwPath));
            }
        }
    }

    public void delete() {
        allCache = null;
        BookD.delete(dbId);
    }
}
