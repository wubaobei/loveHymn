package pri.prepare.lovehymn.server.dal;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

import pri.prepare.lovehymn.server.function.DBHelper;
import pri.prepare.lovehymn.server.function.DBUtil;

public class SearchIndexD extends DaoBase {

    public String name;
    public String paths;

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

    public static SearchIndexD getByName(String name) throws IllegalAccessException {
        String sql = DBUtil.getC().getSelectSql(SearchIndexD.class) + " where name='" + name + "'";

        SQLiteDatabase db = DBHelper.current.getWritableDB();
        try (Cursor cursor = db.rawQuery(sql, null)) {
            if (cursor.moveToNext()) {
                SearchIndexD a = new SearchIndexD();
                return DBUtil.getC().convert(cursor, a);
            }
            return null;
        }
    }

    public static SearchIndexD[] getBySimilarName(String name, int count) throws IllegalAccessException {
        String sql = DBUtil.getC().getSelectSql(SearchIndexD.class) + " where name like '%" + name + "%' limit " + count;

        SQLiteDatabase db = DBHelper.current.getWritableDB();
        ArrayList<SearchIndexD> res = new ArrayList<>();
        try (Cursor cursor = db.rawQuery(sql, null)) {
            while (cursor.moveToNext()) {
                SearchIndexD a = new SearchIndexD();
                res.add(DBUtil.getC().convert(cursor, a));
            }
        }
        return res.toArray(new SearchIndexD[0]);
    }

    public static int getCount() {
        return DBHelper.execSQL_I("select count(1) from " + SearchIndexD.class.getSimpleName());
    }
}
