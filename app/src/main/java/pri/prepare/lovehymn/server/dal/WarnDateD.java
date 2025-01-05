package pri.prepare.lovehymn.server.dal;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import pri.prepare.lovehymn.server.function.DBHelper;
import pri.prepare.lovehymn.server.function.DBUtil;

public class WarnDateD extends DaoBase {
    public String name;
    public String lastTime;

    public static void clearAll() {
        String sql = "delete from " + WarnDateD.class.getSimpleName() ;
        DBHelper.execSQL(sql);
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

    public static WarnDateD getByName(String name) throws IllegalAccessException {
        String sql = DBUtil.getC().getSelectSql(WarnDateD.class) +
                " where name='" + name + "'";

        SQLiteDatabase db = DBHelper.current.getWritableDB();
        try (Cursor cursor = db.rawQuery(sql, null)) {
            if (cursor.moveToNext()) {
                WarnDateD a = new WarnDateD();
                return DBUtil.getC().convert(cursor, a);
            }
            return null;
        }
    }
}
