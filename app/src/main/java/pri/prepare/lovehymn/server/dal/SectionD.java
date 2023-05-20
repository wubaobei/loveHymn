package pri.prepare.lovehymn.server.dal;

import pri.prepare.lovehymn.server.function.DBUtil;

public class SectionD extends DaoBase {

    public int letterId;
    public int chapterIndex;
    public String content;

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
