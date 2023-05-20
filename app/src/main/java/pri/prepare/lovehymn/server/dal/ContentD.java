package pri.prepare.lovehymn.server.dal;

import java.util.ArrayList;

import pri.prepare.lovehymn.server.function.DBUtil;

/**
 * 赏析，背景等内容
 */
public class ContentD extends DaoBase {
    public int typeId;
    public int hymnId;
    public int[] hymnIdArr;
    public String value;

    @Override
    public int insert(boolean returnId) throws IllegalAccessException {
        if(typeId==0 || hymnId==0)
            throw new RuntimeException("typeId==0 || hymnId==0");

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

    public static ContentD[] getByHymnId(int hymnId) throws Exception {
        ContentD temp = new ContentD();
        temp.hymnId = hymnId;
        ArrayList<ContentD> res = DBUtil.getC().getByOtherId(temp);
        return res.toArray(new ContentD[0]);
    }
    public static ContentD[] getByHymnIds(int[] ids) throws Exception {
        ContentD temp = new ContentD();
        temp.hymnIdArr = ids;
        ArrayList<ContentD> res = DBUtil.getC().getByOtherIds(temp);
        return res.toArray(new ContentD[0]);
    }

    public static ContentD[] getByTypeId(int typeId) throws Exception {
        ContentD temp = new ContentD();
        temp.typeId = typeId;
        ArrayList<ContentD> res = DBUtil.getC().getByOtherId(temp);
        return res.toArray(new ContentD[0]);
    }
}
