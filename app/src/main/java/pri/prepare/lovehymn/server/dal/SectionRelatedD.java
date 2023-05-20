package pri.prepare.lovehymn.server.dal;

import pri.prepare.lovehymn.server.function.DBUtil;

public class SectionRelatedD extends DaoBase {
    public int sectionId;
    public int hymnId;
    public int[] hymnIdArr;
    public String remark;

    @Override
    public int insert(boolean returnId) throws IllegalAccessException {
        DBUtil.getC().insert(this);
        if (returnId) {
            id = DBUtil.getC().getLastId(this.getClass());
            return id;
        }
        return -2;
    }

    @Override
    public void update() throws Exception {
        DBUtil.getC().update(this);
    }


    public static SectionRelatedD[] getByHymnId(int id) throws Exception {
        SectionRelatedD a = new SectionRelatedD();
        a.hymnId = id;
        return DBUtil.getC().getByOtherId(a).toArray(new SectionRelatedD[0]);
    }

    public static SectionRelatedD[] getByHymnIds(int[] ids) throws Exception {
        SectionRelatedD a = new SectionRelatedD();
        a.hymnIdArr = ids;
        return DBUtil.getC().getByOtherIds(a).toArray(new SectionRelatedD[0]);
    }

}
