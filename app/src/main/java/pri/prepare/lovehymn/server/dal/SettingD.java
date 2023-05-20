package pri.prepare.lovehymn.server.dal;

import pri.prepare.lovehymn.server.function.DBUtil;

public class SettingD extends  DaoBase{
    public int KeyI;
    public int ValueInt;
    public int ValueStr;

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
}
