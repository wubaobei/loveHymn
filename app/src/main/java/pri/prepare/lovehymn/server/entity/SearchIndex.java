package pri.prepare.lovehymn.server.entity;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
//        if (getPaths().contains("001.pdf")) {
//            Logger.info("add SearchIndex " + getPaths());
//        }
        dao.insert(returnId);
    }

    public void update() throws Exception {
//        if (getPaths().contains("001.pdf")) {
//            Logger.info("update SearchIndex " + getPaths());
//        }
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

    public void addPath(File f) {
//        String[] arr = getPaths().split(";");
//        String aim = f.getAbsolutePath();
//        List<String> list=new ArrayList<>();
//        for (String s : arr) {
//            list.add(s);
//        }
//        list.add(aim);
//        list.sort(new Comparator<String>() {
//            @Override
//            public int compare(String s0, String s1) {
//
//                return 0;
//            }
//        });
//        dao.paths=String.join(";",list);
        dao.paths += ";" + f.getAbsolutePath();
    }
    //endregion
}
