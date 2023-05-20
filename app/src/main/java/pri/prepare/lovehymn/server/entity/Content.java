package pri.prepare.lovehymn.server.entity;

import android.text.style.ForegroundColorSpan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import pri.prepare.lovehymn.server.function.CommonTool;
import pri.prepare.lovehymn.server.dal.ContentD;
import pri.prepare.lovehymn.server.dal.ContentTypeD;

public class Content {
    private Content() {
        dao = new ContentD();
    }

    public Content(ContentD d) {
        dao = d;
    }

    private final ContentD dao;
    private boolean isLyric = false;

    public static Content fromLyric(String lyric) {
        Content content = new Content();
        content.isLyric = true;
        content.dao.value = lyric;
        return content;
    }

    public static ArrayList<Content> sort(ArrayList<Content> values) throws Exception {
        ArrayList<Content> res = new ArrayList<>();
        for (ContentTypeD ct : ContentTypeD.getAll()) {
            for (Content cv : values) {
                if (cv.isType(ct)) {
                    res.add(cv);
                    break;
                }
            }
        }
        return res;
    }

    public static Content fromHymnStrings(String[] arr) throws Exception {
        ContentD d = new ContentD();
        d.typeId = ContentTypeD.getSameMusicType().id;
        d.value = String.join(";", arr);
        return new Content(d);
    }

    public String getTypeString() {
        try {
            if (isLyric)
                return "歌词";
            ContentTypeD ct = ContentTypeD.getById(dao.typeId);
            return ct.name;
        } catch (Exception e) {
            Logger.exception(e);
            return "获取类型异常";
        }
    }

    public boolean isType(ContentTypeD ct) {
        return dao.typeId == ct.id;
    }

    public String getValue() {
        return CommonTool.getC().lineDeal(dao.value.trim());
    }

    private String otherShowString = "";

    public String getOtherShowString() {
        return otherShowString;
    }

    public void fillSameMusic(String hymnStr) throws Exception {
        if (!isType(ContentTypeD.getSameMusicType()))
            return;

        if (!MusicSearch.contains(hymnStr))
            return;

        String[] arr = MusicSearch.getSimilar(hymnStr);
        String[] asource = dao.value.split("[;.；,]");
        HashSet<String> hs = new HashSet<>();
        for (String a : arr) {
            String t = a.replace("-", "附");
            hs.add(t);
        }
        for (String a : asource)
            if (a != null && a.trim().length() > 0) {
                String t = Hymn.format(a.trim(), hymnStr.length() - 1);
                if (!hs.contains(t)) {
                    hs.add(t);

                    Logger.info(Hymn.getById(dao.hymnId).toString() + " asource[" + t + "]");
                }
            }

        if (hs.size() > asource.length) {
            Logger.info("智能添加同谱诗歌" + (hs.size() - asource.length) + "首");
        }
        ArrayList<String> tempList = new ArrayList<>(hs);
        Collections.sort(tempList);
        dao.value = String.join(";", tempList);
        String res = "";
        for (String s : tempList) {
            Hymn h = Hymn.search(s);
            if (h != null) {
                res += s + ":" + h.getTitle() + "\r\n" + h.getShortLyric() + "\r\n";
            }
        }
        Logger.info("set otherShowString " + res);
        otherShowString = res.trim();
    }
}
