package pri.prepare.lovehymn.server.entity;

import pri.prepare.lovehymn.server.dal.LetterD;

public class Letter {
    public Letter() {
        dao = new LetterD();
    }

    public Letter(LetterD d) {
        dao = d;
    }

    private final LetterD dao;

    public static void insertByStr(String s) throws IllegalAccessException {
        if (s == null || s.length() == 0)
            return;
        Letter en = new Letter();
        String[] arr = s.split("\t");
        if (arr.length < 2) {
            return;
        }

        en.dao.fullName = arr[0];
        en.dao.simpleName = arr[1];
        en.dao.insert(false);
    }

    private static Letter[] allTemp = null;

    public static Letter[] getAll() throws Exception {
        if (allTemp == null) {
            allTemp = toArr(LetterD.getAll());
        }

        return allTemp;
    }

    private static Letter[] toArr(LetterD[] arr) {
        Letter[] res = new Letter[arr.length];
        for (int i = 0; i < arr.length; i++)
            res[i] = new Letter(arr[i]);

        return res;
    }

    public static Letter search(String s) throws Exception {
        for (Letter l : getAll()) {
            if (l.dao.simpleName.equals(s)) {
                return l;
            }
        }
        for (Letter l : getAll()) {
            if (l.dao.fullName.equals(s)) {
                return l;
            }
        }
        for (Letter l : getAll()) {
            if (l.dao.fullName.contains(s)) {
                return l;
            }
        }
        for (Letter l : getAll()) {
            boolean sp = true;
            for (char c : s.toCharArray()) {
                if (!l.dao.fullName.contains(c + "")) {
                    sp = false;
                    break;
                }
            }
            if (sp) {
                return l;
            }
        }
        return null;
    }

    //region
    public String getFullName(){
        return dao.fullName;
    }
    public String getSimpleName(){
        return dao.simpleName;
    }
    //endregion
}
