package pri.prepare.lovehymn.server.dal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import pri.prepare.lovehymn.server.function.DBUtil;

public class LetterD extends DaoBase {
    public String simpleName;
    public String fullName;

    public static void clearRepeat() throws Exception {
        LetterD[] all = getAll();
        HashSet<String> nameMap = new HashSet<>();
        List<LetterD> deleteList = new ArrayList<>();
        for (LetterD a : all) {
            if (nameMap.contains(a.fullName)) {
                deleteList.add(a);
            } else {
                nameMap.add(a.fullName);
            }
        }
        if (deleteList.size() % 66 == 0) {
            deleteList.forEach(a -> a.delete());
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
    public void update() throws Exception {
        DBUtil.getC().update(this);
    }

    public void delete() {
        DBUtil.getC().delete(this);
    }

    public static LetterD[] getAll() throws Exception {
        return DBUtil.getC().getAll(LetterD.class).toArray(new LetterD[0]);
    }
}
