package pri.prepare.lovehymn.server.dal;

import pri.prepare.lovehymn.server.function.DBUtil;

public class BookD extends DaoBase {
    public String simpleName;
    public String fullName;
    public int maxLength;
    public String pinYin;
    public int privateId;

    @Override
    public int insert(boolean returnId) throws Exception {
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

    public static BookD[] getAll() throws Exception {
        return DBUtil.getC().getAll(BookD.class).toArray(new BookD[0]);
    }
}
