package pri.prepare.lovehymn.server.entity;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import pri.prepare.lovehymn.server.Service;
import pri.prepare.lovehymn.server.dal.LabelTypeD;

public class LabelType {

    private final LabelTypeD dao;

    private LabelType() {
        dao = new LabelTypeD();
    }

    private LabelType(LabelTypeD d) {
        dao = d;
    }

    public static void addWithoutBak(String name) throws Exception {
        LabelType en = new LabelType();

        if (name.contains(GROUP_FLAG)) {
            int ind = name.lastIndexOf(GROUP_FLAG);
            en.dao.name = name.substring(0, ind);
            en.dao.groupName = name.substring(ind + GROUP_FLAG.length());
        } else
            en.dao.name = name;
        en.dao.insert(false);
    }

    public static void add(String name, String group) throws Exception {
        LabelType en = new LabelType();
        en.dao.name = name;
        en.dao.groupName = group;
        en.dao.insert(false);
        Service.getC().bakLabel(true);
    }

    public List<Label> getLabels() {
        return Label.getByTypeId(this.getId());
    }

    public static final int MAX_NUM = 5;

    @NonNull
    public static LabelType[] getAll() {
        try {
            LabelType[] types = toArr(LabelTypeD.getAll());
            List<LabelType> temp = new ArrayList<>();
            for (int i = 0; i < MAX_NUM; i++) {
                for (LabelType lt : types) {
                    if (lt.getGroup().equals(i + ""))
                        temp.add(lt);
                }
            }
            return temp.toArray(new LabelType[0]);
        } catch (Exception e) {
            Logger.exception(e);
            return new LabelType[0];
        }
    }

    private static LabelType[] toArr(LabelTypeD[] arr) {
        LabelType[] res = new LabelType[arr.length];
        for (int i = 0; i < arr.length; i++)
            res[i] = new LabelType(arr[i]);
        return res;
    }

    public static LabelType getById(int typeId) throws Exception {
        return new LabelType(LabelTypeD.getById(typeId));
    }

    public static LabelType getByName(String ltName) {
        if (ltName == null || ltName.length() == 0)
            return null;
        LabelType[] lts = getAll();
        for (LabelType lt : lts) {
            if (lt.getName().equals(ltName))
                return lt;
        }
        return null;
    }

    public static LabelType getByShowName(String showName) {
        int ind = showName.lastIndexOf(GROUP_FLAG);
        if (ind == -1)
            return getByName(showName);
        return getByName(showName.substring(0, ind));
    }

    public String getName() {
        return dao.name;
    }

    private static final String GROUP_FLAG = " 所在组=";

    public String getShowName() {
        return getName() + GROUP_FLAG + getGroup();
    }

    /**
     * 获取标签组，默认0
     *
     * @return
     */
    public String getGroup() {
        String t = dao.groupName;
        if (t == null || t.length() == 0)
            return "0";
        return t;
    }

    public int getId() {
        return dao.id;
    }

    public boolean delete(boolean force) {
        List<Label> ls = getLabels();
        if (force)
            Logger.info("强制删除 " + this.getName() + " 有标签：" + ls.size());
        Logger.info("all " + LabelType.getAll().length);
        if (ls.size() > 0 && !force)
            return false;
        for (Label l : ls)
            l.delete();
        boolean res = dao.delete();
        if (res)
            Service.getC().bakLabel(true);
        return res;
    }

    public boolean rename(String newName, String group) {
        dao.name = newName;
        dao.groupName = group;
        try {
            dao.update();
            Service.getC().bakLabel(true);
            return true;
        } catch (Exception e) {
            Logger.exception(e);
            return false;
        }
    }

    /**
     * 获取所有拥有该labelType的诗歌
     */
    public List<Hymn> getHymns() {
        List<Integer> hymnIds = new ArrayList<>();

        for (Label l : this.getLabels())
            hymnIds.add(l.getHymnId());

        return Hymn.getByIds(hymnIds);
    }

    /**
     * 获取所有拥有该labelType的诗歌Id
     */
    public List<Integer> getHymnIds() {
        List<Integer> hymnIds = new ArrayList<>();

        for (Label l : this.getLabels())
            hymnIds.add(l.getHymnId());

        return hymnIds;
    }

    public static void clearCache(int typeId) {
        _mp3CountCache.remove(typeId);
    }

    private static HashMap<Integer, Integer> _mp3CountCache = new HashMap<>();

    /**
     * 获取该标签的mp3数量
     */
    public int getMp3Count() {
        if (!_mp3CountCache.containsKey(getId())) {
            int n = 0;
            List<Hymn> hs = getHymns();
            for (Hymn h : hs)
                if (h.getMp3File() != null)
                    n++;
            _mp3CountCache.put(getId(), n);
        }
        return _mp3CountCache.get(getId());
    }

    public static List<String> getAllGroups() {
        LabelType[] ts = getAll();
        List<String> res = new ArrayList<>();
        for (LabelType lt : ts) {
            if (!res.contains(lt.getGroup()))
                res.add(lt.getGroup());
        }
        res.sort(Comparator.naturalOrder());
        return res;
    }
}
