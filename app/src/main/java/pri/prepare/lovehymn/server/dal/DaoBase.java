package pri.prepare.lovehymn.server.dal;

public abstract class DaoBase {
    //public static final String ID_STR="id";

    public int id;

    public abstract int insert(boolean returnId) throws Exception;

    public abstract void update() throws Exception;
}
