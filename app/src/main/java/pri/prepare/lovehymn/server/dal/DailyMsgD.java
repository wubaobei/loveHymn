package pri.prepare.lovehymn.server.dal;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import pri.prepare.lovehymn.server.function.DBHelper;
import pri.prepare.lovehymn.server.function.DBUtil;

public class DailyMsgD extends DaoBase {
    public String shortName;
    public String msg;

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

    public static DailyMsgD getByName(String shortName) throws IllegalAccessException {
        String sql = DBUtil.getC().getSelectSql(DailyMsgD.class) + " where shortName='" + shortName + "'";

        SQLiteDatabase db = DBHelper.current.getWritableDB();
        try (Cursor cursor = db.rawQuery(sql, null)) {
            if (cursor.moveToNext()) {
                DailyMsgD a = new DailyMsgD();
                return DBUtil.getC().convert(cursor, a);
            }
            return null;
        }
    }
}
