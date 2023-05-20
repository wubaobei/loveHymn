package pri.prepare.lovehymn.server.function;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.atomic.AtomicInteger;

import pri.prepare.lovehymn.client.MainActivity;
import pri.prepare.lovehymn.client.Mp3ListActivity;
import pri.prepare.lovehymn.server.dal.AuthorD;
import pri.prepare.lovehymn.server.dal.AuthorRelatedD;
import pri.prepare.lovehymn.server.dal.ContentD;
import pri.prepare.lovehymn.server.dal.ContentTypeD;
import pri.prepare.lovehymn.server.dal.HymnD;
import pri.prepare.lovehymn.server.dal.LabelD;
import pri.prepare.lovehymn.server.dal.LabelTypeD;
import pri.prepare.lovehymn.server.dal.LetterD;
import pri.prepare.lovehymn.server.dal.SearchIndexD;
import pri.prepare.lovehymn.server.dal.SectionD;
import pri.prepare.lovehymn.server.dal.SectionRelatedD;
import pri.prepare.lovehymn.server.dal.SettingD;
import pri.prepare.lovehymn.server.entity.Logger;

public class DBHelper extends SQLiteOpenHelper {
    public static String getCurrentHistory() {
        if (oldV == 0)
            return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < updateVs.length; i++) {
            int updateV = Integer.parseInt(updateVs[i].split(" ")[0]);
            String vs = updateVs[i].substring(7);
            if (updateV == DB_VERSION)
                sb.append("\r\n").append("当前版本更新：").append(vs);
            else if (updateV > oldV)
                sb.append("\r\nV").append(updateV).append("更新：").append(vs);
        }
        return sb.toString();
    }

    private static String[] updateVs = new String[]{
            "211221 新增了许多作者信息，推荐清空app数据使之重新加载",
            "211223 作者信息终于补完了，摘自诗歌背景或https://hymnary.org/",
            "220106 一本诗歌本支持多个白版pdf",
            "220504 新增标签组字段"};
    private static final int DB_VERSION = 220504;
    private static final String DB_NAME = "msg.db";

    public static DBHelper current;

    public DBHelper(Context context) {
        // 传递数据库名与版本号给父类
        super(context, DB_NAME, null, DB_VERSION);
        getWritableDB();
    }

    public static String updateStr = "";
    public static int oldV = 0;

    public static void init(AppCompatActivity activity) {
        if (current == null) {
            current = new DBHelper(activity);
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for (Class c : cc)
            db.execSQL(DBUtil.getC().getCreateTableSql(c));
        Logger.info("create db in first open");
    }

    private final Class[] cc = new Class[]{AuthorD.class, AuthorRelatedD.class, ContentD.class, ContentTypeD.class, HymnD.class,
            LetterD.class, SearchIndexD.class, SectionD.class, SectionRelatedD.class, SettingD.class, LabelD.class, LabelTypeD.class};
    private final String[] dropTables = new String[]{};

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Logger.info("db update:" + oldVersion + "->" + newVersion);
        updateStr = oldVersion + "->" + newVersion;
        oldV = oldVersion;
        dbCheck(db, cc, dropTables);
    }

    public static void execSQL(String sql) {
        SQLiteDatabase db = DBHelper.current.getWritableDB();
        try {
            db.execSQL(sql);
        } catch (Exception e) {
            Logger.info("sql:" + sql);
            Logger.exception(e);
            throw new RuntimeException(e.getMessage());
        }
    }

    public static int execSQL_I(String sql) {
        SQLiteDatabase db = DBHelper.current.getWritableDB();
        try (Cursor c = db.rawQuery(sql, null)) {
            if (c != null && c.getCount() >= 1) {
                c.moveToNext();
                return c.getInt(0);
            }
            return -1;
        }
    }

    public static int[] execSQL_Is(String[] sql) {
        int[] res = new int[sql.length];
        for (int i = 0; i < sql.length; i++)
            res[i] = execSQL_I(sql[i]);
        return res;
    }

    private final AtomicInteger mOpenCounter = new AtomicInteger();
    private SQLiteDatabase mDatabase;

    public SQLiteDatabase getWritableDB() {
        try {
            int res = mOpenCounter.incrementAndGet();
            if (res == 1) {
                // Opening new database
                mDatabase = this.getWritableDatabase();
            }
            return mDatabase;
        } catch (Exception e) {
            Logger.exception(e);
            throw e;
        }
    }

    public void closeWritableDB(boolean b) {
        if (b && mOpenCounter.decrementAndGet() == 0) {
            // Closing database
            mDatabase.close();
            //mDatabase = null;
        }
    }

    public void dbCheck(SQLiteDatabase db, Class[] cc, String[] dropTables) {
        for (Class c : cc) {
            String tb = "select count(1) from sqlite_master where name='" + c.getSimpleName() + "'";
            Cursor cr = db.rawQuery(tb, new String[0]);
            cr.moveToNext();
            boolean tableExist = cr.getInt(0) == 1;
            //Logger.info(c.getSimpleName() + " " + tableExist);
            cr.close();

            if (!tableExist) {
                //不存再则创建表
                Logger.info("缺少表" + c.getSimpleName() + "，创建之");
                db.execSQL(DBUtil.getC().getCreateTableSql(c));
            } else {
                for (String columnName : DBUtil.getC().getColumnArr(c)) {
                    String sql = "select count(1) from sqlite_master where name='" + c.getSimpleName() + "' and sql like '%" + columnName + "%'";
                    Cursor cursor = db.rawQuery(sql, new String[0]);
                    cursor.moveToNext();
                    boolean columnExist = cursor.getInt(0) == 1;
                    if (!columnExist) {
                        Logger.info("表" + c.getSimpleName() + "缺少字段" + columnName + "，创建之");
                        String tp = DBUtil.getC().getColumnSqlType(c, columnName);
                        if (tp.length() > 0) {
                            String alterSql = "alter table " + c.getSimpleName() + " add column " + columnName + " " + tp;
                            db.execSQL(alterSql);
                        } else
                            Logger.info("添加字段失败：类型异常");
                    }
                    cursor.close();
                }
            }
        }
        for (String table : dropTables) {
            String tb = "select count(1) from sqlite_master where name='" + table + "'";
            Cursor cr = db.rawQuery(tb, new String[0]);
            cr.moveToNext();
            boolean tableExist = cr.getInt(0) == 1;
            if (tableExist) {
                Logger.info("删除" + table);
                String dropSql = "drop table " + table;
                db.execSQL(dropSql);
            }
            cr.close();
        }
    }

    /**
     * 检查错误数据
     */
    public void check() {
        String hymn = HymnD.class.getSimpleName();
        String condition = " where  filePath like '%mp3%'";
        int res1 = execSQL_I("select count(1) from " + hymn + condition);
        if (res1 > 0) {
            execSQL("update " + hymn + " set filePath=''" + condition);
            Logger.info("更新" + res1 + "条错误数据");
        }
    }
}
