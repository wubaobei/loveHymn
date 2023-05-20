package pri.prepare.lovehymn.server.dal;

import java.util.ArrayList;

import pri.prepare.lovehymn.server.entity.Logger;
import pri.prepare.lovehymn.server.function.DBUtil;

public class LabelTypeD extends DaoBase {
    public String name;
    public String groupName;

    public static LabelTypeD getById(int typeId) throws Exception {
        LabelTypeD a = new LabelTypeD();
        a.id = typeId;
        return DBUtil.getC().getById(a);
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

    public static LabelTypeD[] getAll() throws Exception {
        ArrayList<LabelTypeD> t = DBUtil.getC().getAll(LabelTypeD.class);
        return t.toArray(new LabelTypeD[0]);
    }
    public boolean delete(){
        try {
            DBUtil.getC().delete(this);
            return true;
        }catch (Exception e){
            Logger.exception(e);
            return false;
        }
    }
}
