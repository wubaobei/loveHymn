package pri.prepare.lovehymn.server.entity;

import java.util.ArrayList;
import java.util.List;

import pri.prepare.lovehymn.server.dal.LabelD;

public class Label {

    public static final String BAK_FILE = "标签.txt";
    private final LabelD dao;

    public static void mod(Hymn hymn, int typeId, boolean b) {
        Label l = get(hymn, typeId);
        if (l == null)
            insert(hymn.getId(), typeId);
        else {
            l.delete();
        }
        LabelType.clearCache(typeId);
    }

    public Integer getHymnId() {
        return dao.hymnId;
    }

    /**
     * 还原文件
     */
    public static boolean reload(MyFile file) {
        try {
            String[] cts = file.getContent();
            for (String ct : cts) {
                if (ct.length() == 0)
                    continue;
                int ind1 = ct.indexOf(":");
                String lableName = ct.substring(0, ind1).trim();
                LabelType lt = LabelType.getByShowName(lableName);
                if (lt == null) {
                    LabelType.addWithoutBak(lableName);
                    lt = LabelType.getByShowName(lableName);
                }
                String[] hs = ct.substring(ind1 + 1).split(";");
                for (String h : hs) {
                    if (h.length() == 0)
                        continue;
                    Book bk = Book.getByName(h.substring(0, 1));
                    String t = h.substring(1);
                    int i1;
                    int i2 = 1;
                    if (t.contains("-")) {
                        String[] p = t.split("-");
                        if (p[0].length() == 0) {
                            //D-1
                            i1 = Integer.parseInt(t);
                        } else {
                            //D35-2
                            i1 = Integer.parseInt(p[0]);
                            i2 = Integer.parseInt(p[1]);
                        }
                    } else {
                        i1 = Integer.parseInt(t);
                    }
                    Hymn hymn = Hymn.search(bk, i1, i2);
                    if (hymn == null)
                        continue;
                    Label l = Label.get(hymn, lt.getId());
                    if (l == null)
                        Label.insert(hymn.getId(), lt.getId());
                }

            }
            return true;
        } catch (Exception e) {
            Logger.exception(e);
            return false;
        }
    }

    public void delete() {
        dao.delete();
    }

    public static Label get(Hymn hymn, int typeId) {
        LabelD[] ls = LabelD.getByHymnId(hymn.getId());
        for (LabelD l : ls)
            if (l.typeId == typeId)
                return new Label(l);
        return null;
    }

    public static boolean hasLabel(Hymn hymn, int typeId) {
        LabelD[] ls = LabelD.getByHymnId(hymn.getId());
        for (LabelD l : ls)
            if (l.typeId == typeId)
                return true;
        return false;
    }

    public String getValue() throws Exception {
        LabelType rt = LabelType.getById(dao.typeId);
        return rt.getName();
    }

    private Label(LabelD d) {
        dao = d;
    }

    private Label() {
        dao = new LabelD();
    }

    public static void insert(int hymnId, int typeId) {
        try {
            Label r = new Label();
            r.dao.hymnId = hymnId;
            r.dao.typeId = typeId;
            r.dao.insert(false);
        } catch (Exception e) {
            Logger.exception(e);
        }
    }

    public static Label[] getByHymnId(int hymnId) {
        LabelD[] rs = LabelD.getByHymnId(hymnId);
        return toArr(rs);
    }

    private static Label[] toArr(LabelD[] arr) {
        Label[] res = new Label[arr.length];
        for (int i = 0; i < arr.length; i++)
            res[i] = new Label(arr[i]);

        return res;
    }

    public static List<String> getHymnIndexsByTypeId(int typeId) {
        ArrayList<String> res = new ArrayList<>();
        LabelD[] ls = LabelD.getByTypeId(typeId);
        ArrayList<Integer> list = new ArrayList<>();
        for (LabelD l : ls) {
            list.add(l.hymnId);
        }
        list.sort(Integer::compareTo);
        //

        for (int id : list)
            try {
                res.add(Hymn.getShortNameById(id));
            } catch (Exception e) {
                Logger.exception(e);
            }
        //
        return res;
    }

    public static List<Label> getByTypeId(int id) {
        LabelD[] ls = LabelD.getByTypeId(id);
        List<Label> res = new ArrayList<>();
        for (LabelD d : ls)
            res.add(new Label(d));
        return res;
    }

}
