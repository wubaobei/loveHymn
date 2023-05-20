package pri.prepare.lovehymn.server.dal;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

import pri.prepare.lovehymn.server.function.DBHelper;
import pri.prepare.lovehymn.server.function.DBUtil;

public class AuthorD extends DaoBase {
    public String name;
    public String age;
    public String introduction;

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

    public static AuthorD getById(int id) throws Exception {
        AuthorD a = new AuthorD();
        a.id = id;
        return DBUtil.getC().getById(a);
    }

    public AuthorD[] getAll() throws Exception {
        return DBUtil.getC().getAll(AuthorD.class).toArray(new AuthorD[0]);
    }

    public static AuthorD getLast() throws IllegalAccessException {
        String sql = DBUtil.getC().getSelectSql(AuthorD.class) + " order by id desc limit 1";
        SQLiteDatabase db = DBHelper.current.getWritableDB();
        try (Cursor cursor = db.rawQuery(sql, null)) {
            if (cursor.moveToNext()) {
                AuthorD a = new AuthorD();
                return DBUtil.getC().convert(cursor, a);
            }
            return null;
        }
    }

    public static AuthorD getByName(String name) throws IllegalAccessException {
        String sql = DBUtil.getC().getSelectSql(AuthorD.class) + " where name='" + name + "'";

        SQLiteDatabase db = DBHelper.current.getWritableDB();
        try (Cursor cursor = db.rawQuery(sql, null)) {
            if (cursor.moveToNext()) {
                AuthorD a = new AuthorD();
                return DBUtil.getC().convert(cursor, a);
            }
            return null;
        }
    }

    public static AuthorD[] getBySimilarName(String name) throws IllegalAccessException {
        String sql = DBUtil.getC().getSelectSql(AuthorD.class) + " where name like '%" + name + "%'";

        SQLiteDatabase db = DBHelper.current.getWritableDB();
        ArrayList<AuthorD> res = new ArrayList<>();
        try (Cursor cursor = db.rawQuery(sql, null)) {
            while (cursor.moveToNext()) {
                AuthorD a = new AuthorD();
                res.add(DBUtil.getC().convert(cursor, a));
            }
        }
        return res.toArray(new AuthorD[0]);
    }
}
