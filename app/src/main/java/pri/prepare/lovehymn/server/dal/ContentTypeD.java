package pri.prepare.lovehymn.server.dal;

import java.util.ArrayList;

import pri.prepare.lovehymn.server.function.DBUtil;

public class ContentTypeD extends DaoBase {

    public String name;

    /**
     * 获取同谱诗歌类型，用于特殊处理
     *
     * @return
     * @throws Exception
     */
    public static ContentTypeD getSameMusicType() throws Exception {
        for (ContentTypeD ct : getAll())
            if (ct.name.equals(SAME_MUSIC))
                return ct;
        return null;
    }
    public static ContentTypeD getSameSongType() throws Exception {
        for (ContentTypeD ct : getAll())
            if (ct.name.equals(SAME_SONG))
                return ct;
        return null;
    }

    public static ContentTypeD getRelatedBibleType() throws Exception {
        for (ContentTypeD ct : getAll())
            if (ct.name.equals(RELATED_BIBLE))
                return ct;
        return null;
    }

    private static final String SAME_MUSIC = "同谱诗歌";
    private static final String RELATED_BIBLE = "相关经节";
    private static final String SAME_SONG="相同";
    private static final String[] INIT_STR = new String[]{"思路", "背景", SAME_MUSIC, RELATED_BIBLE,SAME_SONG};

    public static void init() throws IllegalAccessException {
        for (String it : INIT_STR) {
            ContentTypeD ct = new ContentTypeD();
            ct.name = it;
            ct.insert(false);
        }
    }

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
    public void update() {
        throw new RuntimeException("不该被修改");
    }

    private static ContentTypeD[] alltemp = null;

    public static ContentTypeD[] getAll() throws Exception {
        if (alltemp == null) {
            ArrayList<ContentTypeD> t = DBUtil.getC().getAll(ContentTypeD.class);
            if (t == null || t.size() == 0) {
                init();
                t = DBUtil.getC().getAll(ContentTypeD.class);
            }
            alltemp = t.toArray(new ContentTypeD[0]);
        }
        return alltemp;
    }

    public static ContentTypeD getById(int id) throws Exception {
        ContentTypeD c = new ContentTypeD();
        c.id = id;
        return DBUtil.getC().getById(c);
    }
}
