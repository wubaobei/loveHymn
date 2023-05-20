package pri.prepare.lovehymn.server.entity;

import pri.prepare.lovehymn.server.dal.AuthorD;

public class Author {
    private final AuthorD dao;

    public Author() {
        dao = new AuthorD();
    }

    public Author(String name) {
        dao = new AuthorD();
        dao.name = name;
    }

    public Author(AuthorD author) {
        dao = author;
    }

    public static Author[] getAll() {
        try {
            AuthorD[] r = new AuthorD().getAll();
            return toArr(r);
        } catch (Exception e) {
            Logger.exception(e);
            return new Author[0];
        }
    }

    public static Author[] searchLike(String sc) throws IllegalAccessException {
        return toArr(AuthorD.getBySimilarName(sc));
    }

    public static Author search(String name) throws IllegalAccessException {
        AuthorD a = AuthorD.getByName(name);
        if (a == null)
            return null;
        return new Author(a);
    }

    public static Author getById(int authorId) throws Exception {
        return new Author(AuthorD.getById(authorId));
    }

    public void addOrUpdateByName() throws Exception {
        Author same = search(dao.name);
        if (same != null) {
            same.setIntroduction(getIntroduction());
            same.setAge(getAge());
            same.update();
        } else {
            dao.insert(true);
        }
    }

    public void update() throws Exception {
        dao.update();
    }

    private static Author[] toArr(AuthorD[] arr) {
        Author[] res = new Author[arr.length];
        for (int i = 0; i < arr.length; i++)
            res[i] = new Author(arr[i]);

        return res;
    }

    //region
    public int getId() {
        return dao.id;
    }

    public String getName() {
//        boolean en = Setting.getValueB(Setting.AUTHOR_ENGLISH_NAME, Setting.author_english_name_default);
//        if (!en && dao.name.contains("(")) {
//            return removeEnglishName(dao.name);
//        }
        return dao.name;
    }

    public void setName(String name) {
        dao.name = name;
    }

    public String getAge() {
        return dao.age;
    }

    public void setAge(String age) {
        dao.age = age;
    }

    public String getIntroduction() {
        return dao.introduction;
    }

    public void setIntroduction(String introduction) {
        dao.introduction = introduction;
    }
    //endregion
}
