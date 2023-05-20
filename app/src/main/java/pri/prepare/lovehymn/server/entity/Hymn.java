package pri.prepare.lovehymn.server.entity;

import java.io.File;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import androidx.annotation.NonNull;

import pri.prepare.lovehymn.client.CatalogDialog;
import pri.prepare.lovehymn.server.function.CommonTool;
import pri.prepare.lovehymn.server.function.Constant;
import pri.prepare.lovehymn.server.function.DBUtil;
import pri.prepare.lovehymn.server.function.SdCardTool;
import pri.prepare.lovehymn.server.Service;
import pri.prepare.lovehymn.server.dal.AuthorRelatedD;
import pri.prepare.lovehymn.server.dal.ContentD;
import pri.prepare.lovehymn.server.dal.ContentTypeD;
import pri.prepare.lovehymn.server.dal.HymnD;
import pri.prepare.lovehymn.server.dal.SectionRelatedD;

public class Hymn {
    private static final String ORDER_SQL = " order by bookid,index1 ";

    public Hymn() {
        dao = new HymnD();
        authorRelateds = new AuthorRelatedD[0];
        sectionRelateds = new SectionRelatedD[0];
        contents = new ContentD[0];
    }

    private Hymn(HymnD d, AuthorRelatedD[] ar, SectionRelatedD[] sr, ContentD[] cd) {
        dao = d;
        authorRelateds = ar;
        sectionRelateds = sr;
        contents = cd;
    }

    private Hymn(HymnD d) throws Exception {
        dao = d;
        authorRelateds = AuthorRelatedD.getByHymnId(d.id);
        sectionRelateds = SectionRelatedD.getByHymnId(d.id);
        contents = ContentD.getByHymnId(d.id);
    }

    public static Hymn fromDao(HymnD h) {
        try {
            if (!hymnMap2.containsKey(h.id)) {
                addCache(new Hymn(h));
            }
            return hymnMap2.get(h.id);
        } catch (Exception e) {
            Logger.exception(e);
            throw new RuntimeException(e.getMessage());
        }
    }

    private final HymnD dao;
    private AuthorRelatedD[] authorRelateds;
    private SectionRelatedD[] sectionRelateds;
    private ContentD[] contents;

    public static String format(String a, int i) {
        if (a.contains("-") || a.contains("附"))
            return a;
        if (a.length() < i + 1) {
            int n = i + 1 - a.length();
            String s = "000";
            if (n == 1)
                s = "0";
            else if (n == 2)
                s = "00";
            return a.substring(0, 1) + s + a.substring(1);
        }
        return a;
    }

    public Book getBook() {
        return Book.getById(dao.bookId);
    }

    public Author getBySimilarName(String s, HashSet<Integer> hs) throws Exception {
        for (Author au : getLyricAuthors())
            if (au.getName().contains(s) && (hs == null || !hs.contains(au.getId())))
                return au;
        for (Author au : getMusicAuthors())
            if (au.getName().contains(s) && (hs == null || !hs.contains(au.getId())))
                return au;
        return null;
    }

    public Author[] getLyricAuthors() {
        try {
            if (authorRelateds == null)
                authorRelateds = AuthorRelatedD.getByHymnId(dao.id);
            ArrayList<Author> res = new ArrayList<>();
            for (AuthorRelatedD r : authorRelateds) {
                if (r.type == AuthorRelatedD.LyricAuthor)
                    res.add(Author.getById(r.authorId));
            }
            return res.toArray(new Author[0]);
        } catch (Exception e) {
            Logger.exception(e);
            return new Author[0];
        }
    }

    public Author[] getMusicAuthors() {
        try {
            if (authorRelateds == null)
                authorRelateds = AuthorRelatedD.getByHymnId(dao.id);
            ArrayList<Author> res = new ArrayList<>();
            for (AuthorRelatedD r : authorRelateds) {
                if (r.type == AuthorRelatedD.MusicAuthor)
                    res.add(Author.getById(r.authorId));
            }
            return res.toArray(new Author[0]);
        } catch (Exception e) {
            Logger.exception(e);
            return new Author[0];
        }
    }

    /**
     * 获取歌词前几句，用于展示
     *
     * @return
     */
    public String getShortLyric() {
        String s = _getShortLyric();
        if (s.length() > 0 && s.length() < 10) {
            int r = Math.min(Constant.LYRIC_SHOW_LENGTH_MAX, dao.lyric.length() - 1);
            return dao.lyric.substring(0, r);
        }

        return s;
    }

    private String _getShortLyric() {
        if (dao.lyric == null || dao.lyric.length() == 0)
            return "";
        int l = 0;
        int r = Math.min(Constant.LYRIC_SHOW_LENGTH_MAX, dao.lyric.length() - 1);
        for (int i = 0; i < dao.lyric.length(); i++) {
            if (Constant.ChineseChar.contains(dao.lyric.charAt(i) + "")) {
                if (i <= Constant.LYRIC_SHOW_LENGTH) {
                    l = i;
                } else {
                    r = i;
                    break;
                }
            }
        }

        if (l == 0)
            return dao.lyric.substring(0, r);

        return dao.lyric.substring(0, l);
    }

    public String getShortShowName() {
        return (getBook().SimpleName + String.format("%03d", dao.index1) + (dao.index2 > 1 ? ("+" + dao.index2) : "")).replace("-", "附").replace("+", "-");
    }

    /**
     * 序号与标题
     */
    public String getShowName() {
        String name = getShortShowName();
        if (dao.title != null && dao.title.length() > 0)
            name += " " + dao.title;
        return name;
    }

    public static Hymn search(int bookId, int ind, int ind2) throws Exception {
        HymnD hymn = HymnD.getByIndex(bookId, ind, ind2);
        if (hymn == null) {
            return null;
        }
        if (!hymnMap2.containsKey(hymn.id)) {
            addCache(new Hymn(hymn));
        }
        return hymnMap2.get(hymn.id);
    }

    public static Hymn search(Book bk, int ind, int ind2) throws Exception {
        Hymn res = search(bk.id, ind, ind2);
        if (res == null)
            Logger.info("search hymn is null:" + bk.FullName + " " + ind + " " + ind2);
        return res;
    }

    /**
     * D001
     */
    public static Hymn search(String bookStr) {
        Hymn h = search2(bookStr);
        if (h == null)
            Logger.info("找不到" + bookStr);
        return h;
    }

    private static Hymn search2(String bookStr) {
        try {
            if (!bookStr.contains("-") || bookStr.charAt(1) == '-')
                return search(Book.getByName(bookStr.substring(0, 1)), Integer.parseInt(bookStr.substring(1)), 1);

            String s1 = bookStr.substring(0, 1);
            String s2 = bookStr.substring(1);
            String[] arr2 = s2.split("-");
            return search(Book.getByName(s1), Integer.parseInt(arr2[0]), Integer.parseInt(arr2[1]));
        } catch (Exception e) {
            Logger.exception(e);
            return null;
        }
    }

    public static Hymn[] search(ArrayList<String> sc, int maxCount, int bookId) throws Exception {
        String condition;
        StringBuilder sb = new StringBuilder();
        boolean isF = true;
        for (String s : sc) {
            if (isF)
                isF = false;
            else
                sb.append(" and ");
            sb.append(" (title like '%").append(s).append("%' or lyric like '%").append(s).append("%'");
            Author[] aus = Author.searchLike(s);
            if (aus.length > 0) {
                sb.append(" or id in (select hymnid from " + AuthorRelatedD.class.getSimpleName() + " where authorId in (");
                boolean isAF = true;
                for (Author a : aus) {
                    if (!isAF)
                        sb.append(",");
                    sb.append(a.getId());
                    isAF = false;
                }
                sb.append("))");
            }
            sb.append(")");
        }
        if (bookId > 0) {
            condition = " where bookid = " + bookId + " and (" + sb.toString() + ")";
        } else
            condition = " where " + sb.toString();

        String sql = DBUtil.getC().getSelectSql(HymnD.class) + condition + ORDER_SQL + " limit " + maxCount;
        //Logger.info("sql " + sql);
        HymnD[] hs = HymnD.getListBySql(sql);
        return orderList(toArr(hs));
    }

    public static Hymn[] orderList(Hymn[] hymns) {
        for (int i = 0; i < hymns.length; i++)
            for (int j = i + 1; j < hymns.length; j++) {
                if (hymns[i].getOrderValue() > hymns[j].getOrderValue()) {
                    Hymn t = hymns[i];
                    hymns[i] = hymns[j];
                    hymns[j] = t;
                }
            }
        return hymns;
    }

    public static String getShortNameById(int id) throws Exception {
        HymnD h = HymnD.getById(id);
        if (h != null) {
            Book bk = Book.getById(h.bookId);
            if (bk == null)
                return "书名异常";
            return bk.SimpleName + String.format("%03d", h.index1) + (h.index2 > 1 ? ("-" + h.index2) : "");
        }
        return null;
    }

    /**
     * 加载足迹
     */
    public static void loadStep(MyFile res) {
        String[] cts = res.getContent();
        for (String s : cts) {
            if (s.length() == 0 || !s.contains(" "))
                continue;
            int ind = s.indexOf(" ");
            String hymnInd = s.substring(0, ind);
            String dt = s.substring(ind + 1);
            try {
                Hymn h = Hymn.search(hymnInd);
                h.addStep(dt);
                h.update();
            } catch (Exception e) {
                Logger.info("加载足迹'" + s + "'失败");
                Logger.exception(e);
            }
        }
    }

    /**
     * 如果只有一段，去除段落序号
     */
    private void dealLyric() {
        if (dao.lyric != null && dao.lyric.length() > 0 && dao.lyric.startsWith("一、") && !dao.lyric.contains("二、"))
            dao.lyric = dao.lyric.substring(2);
    }

    public void addOrUpdateByIndex() throws Exception {
        dealLyric();
        Hymn h = search(dao.bookId, dao.index1, dao.index2);
        int id;
        if (h != null) {
            //update
            if (dao.title != null && dao.title.trim().length() > 0)
                h.dao.title = dao.title.trim();
            if (dao.filePath != null && dao.filePath.length() > 0)
                h.dao.filePath = dao.filePath;
            if (dao.whitePage > 0)
                h.dao.whitePage = dao.whitePage;
            if (dao.whitePdf != null && dao.whitePdf.length() > 0)
                h.dao.whitePdf = dao.whitePdf;
            if (dao.lyric != null && dao.lyric.length() > 0)
                h.dao.lyric = dao.lyric;
            h.update();

            id = h.dao.id;
        } else {
            id = dao.insert(true);
        }
        //add
        for (AuthorRelatedD ar : authorRelateds) {
            if (ar.id == 0) {
                ar.hymnId = id;
                ar.insert(true);
            } else
                ar.update();
        }
        for (SectionRelatedD ar : sectionRelateds) {
            if (ar.id == 0) {
                ar.hymnId = id;
                ar.insert(true);
            } else
                ar.update();
        }

        for (ContentD c : contents) {
            if (c.id == 0) {
                c.hymnId = id;
                c.insert(true);
            } else
                c.update();
        }

    }

    public void update() throws Exception {
        dao.update();
        for (AuthorRelatedD ar : authorRelateds) {
            ar.update();
        }
        for (SectionRelatedD ar : sectionRelateds) {
            ar.update();
        }
    }

    private boolean hasV(String s) {
        return s != null && s.length() > 0;
    }

    public int hasContentCount() {
        int res = 0;
        if (hasV(dao.lyric))
            res++;
        if (authorRelateds.length > 0)
            res++;
        res += getContents().length;
        return res;
    }

    public static Hymn[] toArr(HymnD[] arr) throws Exception {
        if (arr == null || arr.length == 0)
            return new Hymn[0];

        int[] ids = new int[arr.length];
        for (int i = 0; i < arr.length; i++)
            ids[i] = arr[i].id;

        AuthorRelatedD[] arAll = AuthorRelatedD.getByHymnIds(ids);
        SectionRelatedD[] srArr = SectionRelatedD.getByHymnIds(ids);
        ContentD[] cdArr = ContentD.getByHymnIds(ids);

        Hymn[] res = new Hymn[arr.length];
        for (int i = 0; i < arr.length; i++) {
            ArrayList<AuthorRelatedD> ar = new ArrayList<>();
            for (AuthorRelatedD a : arAll)
                if (a.hymnId == arr[i].id)
                    ar.add(a);

            ArrayList<SectionRelatedD> sr = new ArrayList<>();
            for (SectionRelatedD a : srArr)
                if (a.hymnId == arr[i].id)
                    sr.add(a);

            ArrayList<ContentD> cd = new ArrayList<>();
            for (ContentD a : cdArr)
                if (a.hymnId == arr[i].id)
                    cd.add(a);

            res[i] = new Hymn(arr[i], ar.toArray(new AuthorRelatedD[0]), sr.toArray(new SectionRelatedD[0]), cd.toArray(new ContentD[0]));
        }

        return res;
    }

    @NonNull
    @Override
    public String toString() {
        if (dao.index1 < 0)
            return getBook().SimpleName + dao.index1;

        String res = getBook().SimpleName + String.format("%0" + getBook().maxLength + "d", dao.index1);
        if (dao.index2 > 1)
            res += "-" + dao.index2;
        return res;
    }

    //region get set methods

    public int getId() {
        return dao.id;
    }

    public int getBookId() {
        return dao.bookId;
    }

    public void setBookId(int value) {
        dao.bookId = value;
    }

    public int getIndex1() {
        return dao.index1;
    }

    public void setIndex1(int value) {
        dao.index1 = value;
    }

    public int getIndex2() {
        return dao.index2;
    }

    public void setIndex2(int value) {
        dao.index2 = value;
    }

    public void addLyricAuthorId(int value) {
        AuthorRelatedD[] temp = new AuthorRelatedD[authorRelateds.length + 1];
        System.arraycopy(authorRelateds, 0, temp, 0, temp.length - 1);
        AuthorRelatedD ar = new AuthorRelatedD();
        ar.hymnId = dao.id;
        ar.type = AuthorRelatedD.LyricAuthor;
        ar.authorId = value;
        temp[temp.length - 1] = ar;
        authorRelateds = temp;
    }

    public void addMusicAuthorId(int value) {
        AuthorRelatedD[] temp = new AuthorRelatedD[authorRelateds.length + 1];
        System.arraycopy(authorRelateds, 0, temp, 0, temp.length - 1);
        AuthorRelatedD ar = new AuthorRelatedD();
        ar.hymnId = dao.id;
        ar.type = AuthorRelatedD.MusicAuthor;
        ar.authorId = value;
        temp[temp.length - 1] = ar;
        authorRelateds = temp;
    }

    public String getLyric() {
        return CommonTool.getC().lineDeal(dao.lyric);
    }

    public void setLyric(String value) {
        dao.lyric = value;
    }

    /**
     * 标题
     */
    public String getTitle() {
        return dao.title;
    }

    public void setTitle(String value) {
        dao.title = value;
    }

    public int getWhitePage() {
        return dao.whitePage;
    }

    public void setWhitePage(int value) {
        dao.whitePage = value;
    }

    public String getWhitePdf() {
        return dao.whitePdf;
    }

    public void setWhitePdf(String value) {
        dao.whitePdf = value;
    }

    private Content[] get_contents_temp = null;


    public Content[] getContents() {
        try {
            if (get_contents_temp == null) {
                ArrayList<Content> res = new ArrayList<>();

                boolean hasTP = false;
                for (ContentD content : contents) {
                    Content cen = new Content(content);
                    if (cen.isType(ContentTypeD.getSameMusicType())) {
                        hasTP = true;
                        cen.fillSameMusic(toString());
                    }
                    res.add(cen);
                }
                if (!hasTP) {
                    String[] arr = MusicSearch.getSimilar(toString());
                    if (arr.length > 0) {
                        res.add(Content.fromHymnStrings(arr));
                    }
                }
                get_contents_temp = Content.sort(res).toArray(new Content[0]);
            }
            return get_contents_temp;
        } catch (Exception e) {
            Logger.exception(e);
            return new Content[0];
        }
    }

    public void setContent(ContentTypeD ctype, String s) {
        ContentD hs = null;
        for (ContentD content : contents) {
            if (content.typeId == ctype.id) {
                hs = content;
                break;
            }
        }
        if (hs == null) {
            ContentD[] temp = new ContentD[contents.length + 1];
            System.arraycopy(contents, 0, temp, 0, contents.length);
            ContentD t = new ContentD();
            t.value = s;
            t.typeId = ctype.id;
            t.hymnId = dao.id;
            temp[contents.length] = t;
            contents = temp;
        } else
            hs.value = s;
    }

    public void contentAppend(int id, String s) {
        ContentD hs = null;
        for (ContentD content : contents) {
            if (content.typeId == id) {
                hs = content;
                break;
            }
        }

        if (hs == null) {
            throw new RuntimeException("未找到content id=" + id);
        }

        hs.value += s;
    }

    public MyFile getFile() {
        String fp1 = SdCardTool.getLbPath() + File.separator + getBook().FullName + File.separator;
        if (new File(fp1).exists()) {
            String fp2 = "";
            if (getIndex1() > 0) {
                if (new File(fp1 + File.separator + (getIndex1() / 100)).exists()) {
                    fp2 = fp1 + File.separator + (getIndex1() / 100);
                } else {
                    for (MyFile f : MyFile.from(fp1).listFiles())
                        if (Service.isInteger(f.getName()) && Integer.parseInt(f.getName()) == getIndex1() / 100) {
                            fp2 = f.getAbsolutePath();
                            break;
                        }
                }
            } else {
                fp2 = fp1 + File.separator + "-";
            }
            if (fp2.length() > 0) {
                for (MyFile f : MyFile.from(fp2).listFiles()) {
                    String numberName = f.getName().substring(0, f.getName().length() - 4);
                    if (Service.isInteger(numberName) && Integer.parseInt(numberName) == getIndex1())
                        return f;
                }
            }
            return null;
        }
        return null;
    }

    public String getFilePath() {
        return dao.filePath;
    }

    public void setFilePath(String value) {
        dao.filePath = value;
    }

    public String addStep(String now) {
        if (dao.step == null || dao.step.length() == 0)
            dao.step = now;
        else
            dao.step += ";" + now;
        return now;
    }

    public String addStep() {
        String now = Setting.getDefaultTimeFormatter().format(LocalDateTime.now());
        return addStep(now);
    }

    public boolean hasStepToday() {
        String s = dao.step;
        if (s == null || s.length() == 0)
            return false;
        String[] arr = dao.step.split(";");
        DateTimeFormatter df = Setting.getDefaultTimeFormatter();
        for (String a : arr) {
            LocalDateTime ldt = LocalDateTime.parse(a, df);
            long dt = LocalDate.now().toEpochDay() - ldt.toLocalDate().toEpochDay();
            if (dt == 0)
                return true;
        }
        return false;
    }

    public int getOrderValue() {
        return getBookId() * 1000000 + Math.abs(getIndex1()) * 10 + (getIndex1() < 0 ? 10000 : 0) + getIndex2();
    }

    /**
     * 获取格式化的足迹（倒序）
     *
     * @return
     */
    public String[] getSteps() {
        String s = dao.step;
        if (s == null || s.length() == 0)
            return new String[0];
        int fmtType = Setting.getValueI(Setting.STEP_FORMAT_TYPE);
        String[] arr = dao.step.split(";");
        List<String> res = new ArrayList<>();
        HashSet<String> hs = new HashSet<>();
        DateTimeFormatter df = Setting.getDefaultTimeFormatter();

        DateTimeFormatter df0 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        DateTimeFormatter df1 = DateTimeFormatter.ofPattern("yyyy年MM月dd日");
        //for (int i = 0; i < arr.length; i++) {
        for (int i = arr.length - 1; i >= 0; i--) {
            try {
                LocalDateTime ldt = LocalDateTime.parse(arr[i], df);
                String rt;
                if (fmtType == 1 || fmtType == 2) {
                    long dt = LocalDate.now().toEpochDay() - ldt.toLocalDate().toEpochDay();
                    rt = dt == 0 ? "今天" : (dt == 1 ? "昨天" : (dt == 2 ? "前天" : df1.format(ldt)));
                    DayOfWeek dw = ldt.getDayOfWeek();
                    if (dw.getValue() == 7)
                        rt += " 主日";
                    else
                        rt += " 周" + "一二三四五六".charAt(dw.getValue() - 1);

                    if (fmtType == 1) {
                        if (ldt.getHour() < 12)
                            rt += "上午";
                        else if (ldt.getHour() < 18)
                            rt += "下午";
                        else
                            rt += "晚上";
                    }
                } else
                    rt = df0.format(ldt);

                if (!hs.contains(rt)) {
                    res.add(rt);
                    hs.add(rt);
                }
            } catch (Exception e) {
                Logger.info("获取足迹失败：" + e.getMessage() + " " + arr[i]);
            }
        }
        return res.toArray(new String[0]);
    }

    //endregion

    /**
     * 获取有足迹的诗歌，按足迹数量倒序排列
     *
     * @return
     */
    public static Hymn[] getHymnsHasStepSortDesc() {
        try {
            Hymn[] res = toArr(HymnD.getStepHymnNum());

            for (int i = 0; i < res.length; i++)
                for (int j = i + 1; j < res.length; j++)
                    if (res[i].getSteps().length < res[j].getSteps().length) {
                        Hymn t = res[i];
                        res[i] = res[j];
                        res[j] = t;
                    }

            return res;
        } catch (Exception e) {
            Logger.exception(e);
            return new Hymn[0];
        }
    }

    public static Hymn getById(int id) {
        try {
            if (!hymnMap2.containsKey(id)) {
                addCache(new Hymn(HymnD.getById(id)));
            }

            return hymnMap2.get(id);
        } catch (Exception e) {
            Logger.exception(e);
            return null;
        }
    }

    public static List<Hymn> getByIds(List<Integer> ids) {
        try {
            List<HymnD> hymnDS = HymnD.getByIds(ids);
            List<Hymn> res = new ArrayList<>();
            for (HymnD hd : hymnDS) {
                if (!hymnMap2.containsKey(hd.id)) {
                    addCache(new Hymn(hd));
                }
                res.add(hymnMap2.get(hd.id));
            }
            return res;
        } catch (Exception e) {
            Logger.exception(e);
            return null;
        }
    }

    public File getMp3File() {
        if (dao.mp3FilePath != null && new File(dao.mp3FilePath).exists())
            return new File(dao.mp3FilePath);
        MyFile f = getFile().getMp3();
        if (f != null) {
            dao.mp3FilePath = f.getAbsolutePath();
            try {
                dao.update();
            } catch (Exception e) {
                Logger.exception(e);
            }
        }
        return f;
    }

    public static Hymn[] getByBook(Book book) {
        try {
            HymnD[] hymnDS = HymnD.getByBookId(book.id);
            Hymn[] res = new Hymn[hymnDS.length];
            for (int i = 0; i < hymnDS.length; i++) {
                int k = 999;
                int minInd = 99999;
                int minInd2 = 10;
                for (int j = 0; j < hymnDS.length; j++) {
                    if (hymnDS[j] != null) {
                        if (hymnDS[j].index1 < minInd) {
                            minInd = hymnDS[j].index1;
                            minInd2 = hymnDS[j].index2;
                            k = j;
                        } else if (hymnDS[j].index1 == minInd && hymnDS[j].index2 < minInd2) {
                            minInd2 = hymnDS[j].index2;
                            k = j;
                        }
                    }
                }

                if (!hymnMap2.containsKey(hymnDS[k].id)) {
                    addCache(new Hymn(hymnDS[k]));
                }
                res[i] = hymnMap2.get(hymnDS[k].id);
                hymnDS[k] = null;
            }
            return res;
        } catch (Exception e) {
            Logger.exception(e);
            return new Hymn[0];
        }
    }

    //所有诗歌实体只获取一遍，全部缓存起来
    private static HashMap<String, Hymn> hymnMap = new HashMap<>();
    private static HashMap<Integer, Hymn> hymnMap2 = new HashMap<>();

    private static void addCache(Hymn hymn) {
        if (hymn != null)
            hymnMap2.put(hymn.getId(), hymn);
    }

    public static void addCache(String key, Hymn hymn) {
        if (hymn == null) {
            return;
        }
        if (!hymnMap2.containsKey(hymn.getId())) {
            addCache(hymn);
            hymnMap.put(key, hymn);
        } else {
            hymnMap.put(key, hymnMap2.get(hymn.getId()));
        }
    }

    public static boolean existCache(String key) {
        return hymnMap.containsKey(key);
    }

    public static Hymn getCache(String key) {
        return hymnMap.get(key);
    }

    public Label[] getLabels() {
        return Label.getByHymnId(this.getId());
    }
}
