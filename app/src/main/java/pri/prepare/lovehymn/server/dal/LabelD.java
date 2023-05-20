package pri.prepare.lovehymn.server.dal;

import java.util.ArrayList;

import pri.prepare.lovehymn.server.entity.Logger;
import pri.prepare.lovehymn.server.function.DBUtil;

public class LabelD extends DaoBase {
    public int typeId;
    public int hymnId;

    public static LabelD[] getByHymnId(int hymnId) {
        try {
            LabelD temp = new LabelD();
            temp.hymnId = hymnId;
            ArrayList<LabelD> res = DBUtil.getC().getByOtherId(temp);
            return res.toArray(new LabelD[0]);
        } catch (Exception e) {
            Logger.exception(e);
            return new LabelD[0];
        }
    }

    public static LabelD[] getByTypeId(int typeId) {
        try {
            LabelD temp = new LabelD();
            temp.typeId = typeId;
            ArrayList<LabelD> res = DBUtil.getC().getByOtherId(temp);
            return res.toArray(new LabelD[0]);
        } catch (Exception e) {
            Logger.exception(e);
            return new LabelD[0];
        }
    }

    public static ArrayList<LabelD> getAll() throws Exception {
        return DBUtil.getC().getAll(LabelD.class);
    }

    @Override
    public int insert(boolean returnId) throws Exception {
        DBUtil.getC().insert(this);
        if (returnId) {
            id = DBUtil.getC().getLastId(this.getClass());
            return id;
        }
        return -1;
    }

    @Override
    public void update() throws Exception {
        DBUtil.getC().update(this);
    }

    public void delete() {
        DBUtil.getC().delete(this);
    }
}
