package pri.prepare.lovehymn.server.dal;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import pri.prepare.lovehymn.server.entity.Book;
import pri.prepare.lovehymn.server.entity.Logger;
import pri.prepare.lovehymn.server.function.DBHelper;
import pri.prepare.lovehymn.server.function.DBUtil;

public class HymnD extends DaoBase {

    public int bookId;
    public int index1;
    public int index2;
    public String title;
    public String lyric;
    public int whitePage;
    public String whitePdf;
    public String filePath;
    public String step;
    public String mp3FilePath;

    public HymnD() {
        index2 = 1;
    }

    @Override
    public int insert(boolean returnId) throws Exception {
        if (index2 == 0)
            index2 = 1;
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

    public static HymnD getById(int id) throws Exception {
        HymnD hymn = new HymnD();
        hymn.id = id;
        return DBUtil.getC().getById(hymn);
    }

    public static List<HymnD> getByIds(List<Integer> ids) {
        try {
            return DBUtil.getC().getByIds(new HymnD(), ids);
        } catch (Exception e) {
            Logger.exception(e);
            return null;
        }
    }

    public static HymnD[] getByRange(int bookId, int minIndex, int maxIndex) throws IllegalAccessException {
        ArrayList<HymnD> res = new ArrayList<>();
        String sql = DBUtil.getC().getSelectSql(HymnD.class) + " where bookId=" + bookId + " and (index1>=" + minIndex + " and index1<=" + maxIndex + ")";
        SQLiteDatabase db = DBHelper.current.getWritableDB();
        try (Cursor cursor = db.rawQuery(sql, null)) {
            while (cursor.moveToNext()) {
                HymnD a = new HymnD();
                res.add(DBUtil.getC().convert(cursor, a));
            }
        }
        return res.toArray(new HymnD[0]);
    }

    public static HymnD[] getByBookId(int bookId) throws IllegalAccessException {

        ArrayList<HymnD> res = new ArrayList<>();
        String sql;
        if(bookId== Book.ALL.id){
            sql= DBUtil.getC().getSelectSql(HymnD.class) ;
        }
        else  sql= DBUtil.getC().getSelectSql(HymnD.class) + " where bookId=" + bookId;
        SQLiteDatabase db = DBHelper.current.getWritableDB();
        try (Cursor cursor = db.rawQuery(sql, null)) {
            while (cursor.moveToNext()) {
                HymnD a = new HymnD();
                res.add(DBUtil.getC().convert(cursor, a));
            }
        }
        return res.toArray(new HymnD[0]);
    }

    public static HymnD getByIndex(int bookId, int index1, int index2) throws IllegalAccessException {
        String sql = DBUtil.getC().getSelectSql(HymnD.class) + " where bookId=" + bookId + " and index1=" + index1 + " and index2=" + index2;
        SQLiteDatabase db = DBHelper.current.getWritableDB();
        try (Cursor cursor = db.rawQuery(sql, null)) {
            if (cursor.moveToNext()) {
                HymnD a = new HymnD();
                HymnD res = DBUtil.getC().convert(cursor, a);
                return res;
            }
            return null;
        }
    }

    public static  HymnD[] getStepHymnNum() throws IllegalAccessException {
        ArrayList<HymnD> res = new ArrayList<>();
        String sql = "select * from " + HymnD.class.getSimpleName() + " where length(step)>0";
        SQLiteDatabase db = DBHelper.current.getWritableDB();
        try (Cursor cursor = db.rawQuery(sql, null)) {
            while (cursor.moveToNext()) {
                HymnD a = new HymnD();
                res.add(DBUtil.getC().convert(cursor, a));
            }
            return res.toArray(new HymnD[0]);
        }
    }

    public static HymnD[] getListBySql(String sql) throws IllegalAccessException {
        ArrayList<HymnD> res = new ArrayList<>();
        SQLiteDatabase db = DBHelper.current.getWritableDB();
        try (Cursor cursor = db.rawQuery(sql, null)) {
            while (cursor.moveToNext()) {
                HymnD a = new HymnD();
                res.add(DBUtil.getC().convert(cursor, a));
            }
            return res.toArray(new HymnD[0]);
        }
    }

}
