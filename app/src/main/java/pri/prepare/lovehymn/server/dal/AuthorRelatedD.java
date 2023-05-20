package pri.prepare.lovehymn.server.dal;

import pri.prepare.lovehymn.server.function.DBUtil;

public class AuthorRelatedD extends DaoBase {
    public int hymnId;
    public int[] hymnIdArr;
    public int authorId;
    public int type;

    public static final int LyricAuthor = 1;
    public static final int MusicAuthor = 2;

    public static String getTypeShortStr(int type) {
        if (type == LyricAuthor)
            return "词";
        if (type == MusicAuthor)
            return "曲";
        throw new RuntimeException("未知的作者类型：" + type);
    }


    @Override
    public int insert(boolean returnId) throws IllegalAccessException {
        if (hymnId == 0 || authorId == 0)
            throw new RuntimeException("is zero!!!");
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

    public static AuthorRelatedD getById(int id) throws Exception {
        AuthorRelatedD a = new AuthorRelatedD();
        a.id = id;
        return DBUtil.getC().getById(a);
    }

    public static AuthorRelatedD[] getByHymnId(int hymnId) throws Exception {
        AuthorRelatedD a = new AuthorRelatedD();
        a.hymnId = hymnId;
        return DBUtil.getC().getByOtherId(a).toArray(new AuthorRelatedD[0]);
    }

    public static AuthorRelatedD[] getByHymnIds(int[] ids) throws Exception {
        AuthorRelatedD a = new AuthorRelatedD();
        a.hymnIdArr = ids;
        return DBUtil.getC().getByOtherIds(a).toArray(new AuthorRelatedD[0]);
    }

    public static AuthorRelatedD[] getByAuthorId(int authorId) throws Exception {
        AuthorRelatedD a = new AuthorRelatedD();
        a.authorId = authorId;
        return DBUtil.getC().getByOtherId(a).toArray(new AuthorRelatedD[0]);
    }
}
