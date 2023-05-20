package pri.prepare.lovehymn.server.entity;

import pri.prepare.lovehymn.server.dal.SearchIndexD;

/**
 * 序号索引
 */
public class SearchIndex {
    private final SearchIndexD dao;

    public String[] getPathArr() {
        return dao.paths.split(";");
    }

    public SearchIndex(String name, String paths) {
        dao = new SearchIndexD();
        dao.name = name;
        dao.paths = paths;
    }

    private SearchIndex(SearchIndexD d) {
        dao = d;
    }

    public void add(boolean returnId) throws IllegalAccessException {
        dao.insert(returnId);
    }

    public void update() throws Exception {
        dao.update();
    }

    public static SearchIndex[] search(String nameLike, int max) throws IllegalAccessException {
        return toArr(SearchIndexD.getBySimilarName(nameLike, max));
    }

    public static SearchIndex getByName(String name) throws IllegalAccessException {
        SearchIndexD temp = SearchIndexD.getByName(name);
        if (temp == null)
            return null;
        return new SearchIndex(temp);
    }

    private static SearchIndex[] toArr(SearchIndexD[] arr) {
        SearchIndex[] res = new SearchIndex[arr.length];
        for (int i = 0; i < arr.length; i++)
            res[i] = new SearchIndex(arr[i]);

        return res;
    }

    //region
    public String getPaths() {
        return dao.paths;
    }

    public void setPaths(String value) {
        dao.paths = value;
    }
    //endregion
}
