package pri.prepare.lovehymn.server.dal;

import pri.prepare.lovehymn.server.entity.Logger;
import pri.prepare.lovehymn.server.function.DBUtil;

public class LetterD extends DaoBase {
    public String simpleName;
    public String fullName;

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


    public static LetterD[] getAll() throws Exception {
        return DBUtil.getC().getAll(LetterD.class).toArray(new LetterD[0]);
    }
}
