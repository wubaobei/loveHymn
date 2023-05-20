package pri.prepare.lovehymn.server.function;

import android.database.Cursor;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import pri.prepare.lovehymn.server.dal.DaoBase;
import pri.prepare.lovehymn.server.entity.Logger;

public class DBUtil {
    public static DBUtil getC() {
        if (c == null) {
            c = new DBUtil();
            c.init();
        }
        return c;
    }

    private static DBUtil c;

    private final HashMap<String, String> map = new HashMap<>();
    private final HashMap<String, Field[]> orderedFieldMap = new HashMap<>();
    private final HashMap<String, String> selectMap = new HashMap<>();
    private final HashMap<String, Field> idFieldMap = new HashMap<>();
    private final HashMap<String, Field[]> otherIdFieldMap = new HashMap<>();

    private void init() {
        map.put(String.class.getName(), "text");
        map.put(int.class.getName(), "int");
    }

    private static final String ID_STR = "id";
    private static final String ID_ARR_STR = "idarr";

    private Field[] getOrderedFields(Class c) {
        String name = c.getSimpleName();
        if (!orderedFieldMap.containsKey(name)) {
            Field[] fs = c.getFields();
            ArrayList<Field> res = new ArrayList<>();
            for (Field f : fs)
                if (f.getName().equals(ID_STR))
                    res.add(f);
            for (Field f : fs)
                if (!f.getName().equals(ID_STR)) {
                    if (!Modifier.isStatic(f.getModifiers()))
                        res.add(f);
                }
            orderedFieldMap.put(name, res.toArray(new Field[0]));
        }
        return orderedFieldMap.get(name);
    }

    private Field getIdField(Class c) throws NoSuchFieldException {
        String name = c.getSimpleName();
        if (!idFieldMap.containsKey(name)) {
            idFieldMap.put(name, c.getField(ID_STR));
        }
        return idFieldMap.get(name);
    }

    private <T> Field getOtherIdsField(T obj) throws IllegalAccessException {
        Class c = obj.getClass();
        String name = c.getSimpleName();
        if (!otherIdFieldMap.containsKey(name)) {
            Field[] fa = getOrderedFields(c);
            ArrayList<Field> res = new ArrayList<>();
            for (Field f : fa) {
                if (f.getName().equals(ID_ARR_STR))
                    continue;
                if (f.getName().toLowerCase().contains(ID_STR))
                    res.add(f);
            }
            otherIdFieldMap.put(name, res.toArray(new Field[0]));
        }

        Field[] fs = otherIdFieldMap.get(name);
        for (Field f : fs) {
            Object o = f.get(obj);
            if (o instanceof int[] && ((int[]) o).length > 0) {
                return f;
            }
        }
        return null;
    }

    private <T> Field getOtherIdField(T obj) throws IllegalAccessException {
        Class c = obj.getClass();
        String name = c.getSimpleName();
        if (!otherIdFieldMap.containsKey(name)) {
            Field[] fa = getOrderedFields(c);
            ArrayList<Field> res = new ArrayList<>();
            for (Field f : fa) {
                if (f.getName().equals(ID_STR))
                    continue;
                if (f.getName().toLowerCase().contains(ID_STR))
                    res.add(f);
            }
            otherIdFieldMap.put(name, res.toArray(new Field[0]));
        }

        Field[] fs = otherIdFieldMap.get(name);
        for (Field f : fs) {
            Object o = f.get(obj);
            if (o instanceof Integer && (int) o > 0) {
                return f;
            }
        }
        return null;
    }

    public String getColumnSqlType(Class c, String columnName) {
        Field[] fs = getOrderedFields(c);
        for (Field f : fs) {
            if (f.getName().equals(columnName))
                return map.get(f.getType().getName());
        }
        return "";
    }

    public String[] getColumnArr(Class c) {
        ArrayList<String> res = new ArrayList<>();
        Field[] fs = getOrderedFields(c);
        for (Field f : fs) {
            if (f.getName().equals(ID_STR) || map.containsKey(f.getType().getName()))
                res.add(f.getName());
        }
        return res.toArray(new String[0]);
    }

    public String getCreateTableSql(Class c) {
        StringBuilder sb = new StringBuilder();
        Field[] fs = getOrderedFields(c);

        sb.append("create table ").append(c.getSimpleName()).append("(");
        for (Field f : fs) {
            if (f.getName().equals(ID_STR))
                sb.append(ID_STR).append(" integer primary key AUTOINCREMENT");
            else {
                if (!f.getType().equals(int[].class))
                    sb.append(",").append(f.getName().toLowerCase());
                if (map.containsKey(f.getType().getName()))
                    sb.append(" ").append(map.get(f.getType().getName()));
                else if (!f.getType().equals(int[].class))
                    throw new RuntimeException(getUpString(f));
            }
        }
        sb.append(")");
        Logger.info("create sql: " + sb.toString());
        return sb.toString();
    }

    public <T> void insert(T obj) throws IllegalAccessException {
        Class c = obj.getClass();
        Field[] fs = getOrderedFields(c);

        StringBuilder sb = new StringBuilder();
        sb.append("insert into ").append(c.getSimpleName()).append("(");
        StringBuilder sb2 = new StringBuilder();
        sb2.append("values(");

        boolean isFirst = true;
        boolean jump = false;

        for (Field f : fs) {
            if (f.getName().equals(ID_STR))
                continue;
            if (isFirst)
                isFirst = false;
            else if (jump) {
                jump = false;
            } else {
                sb.append(",");
                sb2.append(",");
            }

            if (f.getType().equals(String.class)) {
                sb.append(f.getName());
                sb2.append(sf((String) f.get(obj)));
            } else if (f.getType().equals(int.class)) {
                sb.append(f.getName());
                sb2.append(sf((int) f.get(obj)));
            } else if (f.getType().equals(int[].class)) {
                jump = true;
            } else
                throw new RuntimeException(getUpString(f));
        }
        sb.append(")");
        sb2.append(")");

        DBHelper.execSQL(sb.toString() + sb2.toString());
    }

    public <T> T getById(T obj) throws Exception {
        Class c = obj.getClass();
        String sql = getSelectSql(c) + WHERE + ID_STR + "=" + getIdField(c).get(obj);

        try (Cursor cursor = DBHelper.current.getWritableDB().rawQuery(sql, null)) {
            cursor.moveToNext();
            int n = 1;
            for (Field f : getOrderedFields(c)) {
                if (f.getName().equals(ID_STR))
                    continue;
                if (f.getType().equals(String.class))
                    f.set(obj, cursor.getString(n++));
                else if (f.getType().equals(int.class))
                    f.set(obj, cursor.getInt(n++));
                else if (!f.getType().equals(int[].class))
                    throw new RuntimeException(getUpString(f));
            }
        }

        return obj;
    }

    public <T> List<T> getByIds(T obj, List<Integer> ids) throws Exception {
        Class c = obj.getClass();

        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        for (Integer id : ids) {
            if (isFirst) {
                sb.append(id);
                isFirst = false;
            } else
                sb.append(",").append(id);
        }

        String sql = getSelectSql(c) + WHERE + ID_STR + " in (" + sb.toString() + ")";
        List<T> res = new ArrayList<>();

        try (Cursor cursor = DBHelper.current.getWritableDB().rawQuery(sql, null)) {
            while (cursor.moveToNext()) {
                T t = (T) c.newInstance();

                int n = 0;
                for (Field f : getOrderedFields(c)) {
                    if (f.getType().equals(String.class)) {
                        f.set(t, cursor.getString(n++));
                    } else if (f.getType().equals(int.class)) {
                        f.set(t, cursor.getInt(n++));
                    } else if (!f.getType().equals(int[].class))
                        throw new RuntimeException(getUpString(f));
                }
                res.add(t);
            }
        }

        return res;
    }

    public <T extends DaoBase> void delete(T obj) {
        String sql = "delete from " + obj.getClass().getSimpleName() + " where id=" + obj.id;
        DBHelper.execSQL(sql);
    }

    public <T> void update(T obj) throws Exception {
        Class c = obj.getClass();

        StringBuilder sb = new StringBuilder();
        sb.append("update ").append(c.getSimpleName()).append(" set ");

        boolean isFirst = true;
        boolean jump = false;

        for (Field f : getOrderedFields(c)) {
            if (f.getName().equals(ID_STR))
                continue;
            if (isFirst)
                isFirst = false;
            else if (jump)
                jump = false;
            else
                sb.append(",");

            if (!f.getType().equals(int[].class))
                sb.append(f.getName()).append("=");
            if (f.getType().equals(String.class))
                sb.append(sf((String) f.get(obj)));
            else if (f.getType().equals(int.class))
                sb.append(sf((int) f.get(obj)));
            else if (f.getType().equals(int[].class)) {
                jump = true;
            } else
                throw new RuntimeException(getUpString(f));
        }

        sb.append(WHERE).append(ID_STR).append("=").append(getIdField(c).get(obj));

        DBHelper.execSQL(sb.toString());
    }

    public <T> ArrayList<T> getByOtherIds(T obj) throws Exception {
        Class c = obj.getClass();

        Field fc = getOtherIdsField(obj);

        if (fc == null) {
            throw new RuntimeException(CANT_FOUND);
        }

        int[] idVs = (int[]) fc.get(obj);
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (int i = 0; i < idVs.length; i++) {
            sb.append(idVs[i]);
            if (i == idVs.length - 1)
                sb.append(")");
            else
                sb.append(",");
        }

        String sql = getSelectSql(c) + WHERE + fc.getName().replace("Arr", "") + " in " + sb.toString();
        ArrayList<T> res = new ArrayList<>();

        try (Cursor cursor = DBHelper.current.getWritableDB().rawQuery(sql, null)) {
            while (cursor.moveToNext()) {
                T temp = (T) c.getConstructor().newInstance();
                int n = 0;
                for (Field f : getOrderedFields(c)) {
                    if (f.getType().equals(String.class))
                        f.set(temp, cursor.getString(n++));
                    else if (f.getType().equals(int.class))
                        f.set(temp, cursor.getInt(n++));
                    else if (!f.getType().equals(int[].class))
                        throw new RuntimeException(getUpString(f));
                }
                res.add(temp);
            }
        }

        return res;
    }

    public <T> ArrayList<T> getByOtherId(T obj) throws Exception {
        Class c = obj.getClass();

        Field fc = getOtherIdField(obj);

        if (fc == null) {
            throw new RuntimeException(CANT_FOUND);
        }

        String sql = getSelectSql(c) + WHERE + fc.getName() + "=" + fc.get(obj);
        ArrayList<T> res = new ArrayList<>();

        try (Cursor cursor = DBHelper.current.getWritableDB().rawQuery(sql, null)) {
            while (cursor.moveToNext()) {
                T temp = (T) c.getConstructor().newInstance();
                int n = 0;
                for (Field f : getOrderedFields(c)) {
                    if (f.getType().equals(String.class))
                        f.set(temp, cursor.getString(n++));
                    else if (f.getType().equals(int.class))
                        f.set(temp, cursor.getInt(n++));
                    else if (!f.getType().equals(int[].class))
                        throw new RuntimeException(getUpString(f));
                }
                res.add(temp);
            }
        }

        return res;
    }

    public <T> ArrayList<T> getAll(Class c) throws Exception {
        String sql = getSelectSql(c);
        ArrayList<T> res = new ArrayList<>();

        try (Cursor cursor = DBHelper.current.getWritableDB().rawQuery(sql, null)) {
            while (cursor.moveToNext()) {
                T temp = (T) c.getConstructor().newInstance();
                int n = 0;
                for (Field f : getOrderedFields(c)) {
                    if (f.getType().equals(String.class)) {
                        f.set(temp, cursor.getString(n++));
                    } else if (f.getType().equals(int.class)) {
                        f.set(temp, cursor.getInt(n++));
                    } else if (!f.getType().equals(int[].class))
                        throw new RuntimeException(getUpString(f));
                }
                res.add(temp);
            }
        }

        return res;
    }

    private static final String WHERE = " where ";

    /**
     * 获取select的sql
     */
    public String getSelectSql(Class c) {
        if (!selectMap.containsKey(c.getSimpleName())) {
            Field[] orderFields = getOrderedFields(c);
            List<String> sl = new ArrayList<>();
            for (Field field : orderFields)
                if (!field.getName().toLowerCase().contains(ID_ARR_STR))
                    sl.add("[" + field.getName() + "]");
            selectMap.put(c.getSimpleName(), "select " + String.join(",", sl) + " from " + c.getSimpleName()+" ");
        }
        return selectMap.get(c.getSimpleName());
    }

    private String getUpString(Field f) {
        return "不支持的类型：" + f.getType().getSimpleName();
    }

    private static final String CANT_FOUND = "未发现符合要求的外键id";

    private String sf(int i) {
        return String.valueOf(i);
    }

    private String sf(String s) {
        if (s == null)
            return "null";
        return "'" + s.replace("'", "''") + "'";
    }

    public <T> T convert(Cursor cursor, T temp) throws IllegalAccessException {
        int n = 0;
        for (Field f : getOrderedFields(temp.getClass())) {
            if (f.getType().equals(String.class))
                f.set(temp, cursor.getString(n++));
            else if (f.getType().equals(int.class))
                f.set(temp, cursor.getInt(n++));
            else if (!f.getType().equals(int[].class))
                throw new RuntimeException(getUpString(f));
        }
        return temp;
    }

    public int getLastId(Class c) {
        return DBHelper.execSQL_I("select last_insert_rowid() from " + c.getSimpleName());
    }

}
