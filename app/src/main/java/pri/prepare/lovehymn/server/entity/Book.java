package pri.prepare.lovehymn.server.entity;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import pri.prepare.lovehymn.server.dal.BookD;
import pri.prepare.lovehymn.server.function.Constant;
import pri.prepare.lovehymn.server.function.SdCardTool;

public class Book {
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

    public static final Book[] getAllInLoad() {
        return new Book[]{DaBen, BuChong, Chang, Xin, ErTong, Other};
    }

    public static final Book[] getAll() {
        return new Book[]{DaBen, BuChong, Chang, Xin, ErTong, Other};
    }

    public static Book[] getPrivateBooks() {
        try {
            BookD[] bookDs = BookD.getAll();
            Book[] res = new Book[bookDs.length];
            for (int i = 0; i < bookDs.length; i++) {
                BookD d = bookDs[i];
                res[i] = new Book(d.privateId, d.simpleName, d.fullName, d.maxLength, d.pinYin);
            }
            return res;
        } catch (Exception e) {
            return new Book[0];
        }
    }

    public static Book getById(int bookId) {
        for (Book bk : getAll())
            if (bk.id == bookId)
                return bk;
        if (bookId == ALL.id)
            return ALL;
        return null;
    }

    public static Book getByName(String name) {
        for (Book bk : getAllInLoad())
            if (bk.fullName.equals(name) || bk.simpleName.equals(name.toUpperCase()))
                return bk;
        return new Book(-1, "找不到书", "找不到书", 3, "");
    }

    public static boolean isShortName(char c) {
        for (Book bk : getAll())
            if (bk.simpleName.charAt(0) == c || bk.simpleName.charAt(0) == c - 'a' + 'A')
                return true;
        return false;
    }

    public MyFile getMp3Directory() {
        for (MyFile f : MyFile.from(SdCardTool.getLbPath()).listFiles()) {
            if (f.getName().equalsIgnoreCase(simpleName))
                return f;
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
        if (pdf == null || pdf.length() == 0)
            p = SdCardTool.getLbPath() + File.separator + Constant.WHITE + File.separator + simpleName + ".pdf";
        else {
            p = SdCardTool.getLbPath() + File.separator + Constant.WHITE + File.separator + pdf;
        }
        File f;
        if ((f = new File(p)).exists())
            return f;
        return null;
    }

    private int _mp3Count = -1;

    /**
     * 获取该诗歌本MP3文件数量（缓存）
     */
    public int getMp3Count() {
        if (_mp3Count < 0) {
            if (getMp3Directory() != null)
                _mp3Count = getMp3Directory().getMp3List().size();
            else
                _mp3Count = 0;
        }
        return _mp3Count;
    }

    public void renamePinYin() {
        String pyPath = SdCardTool.getLbPath() + File.separator + pinYin;
        String zwPath = SdCardTool.getLbPath() + File.separator + fullName;
        if (new File(pyPath).exists()) {
            File zw = new File(zwPath);
            if (zw.exists()) {
                String nn = SdCardTool.getLbPath() + File.separator + fullName + "hideIn" + DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now());
                zw.renameTo(new File(nn));
                Logger.info("重命名" + zwPath + "->" + nn);
            }
            Logger.info("重命名" + pinYin + "->" + fullName);
            new File(pyPath).renameTo(new File(zwPath));
        }

    }
}
