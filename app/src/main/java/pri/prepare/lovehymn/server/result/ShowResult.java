package pri.prepare.lovehymn.server.result;

import java.util.ArrayList;
import java.util.HashSet;

import pri.prepare.lovehymn.server.Service;
import pri.prepare.lovehymn.server.entity.Author;
import pri.prepare.lovehymn.server.entity.Hymn;
import pri.prepare.lovehymn.server.entity.MyFile;
import pri.prepare.lovehymn.server.entity.Setting;

/**
 * 目录上显示的按钮的数据
 */
public class ShowResult {
    public ShowResult(String warn) {
        showStr = warn;
        file = null;
        highLight = false;
    }

    public ShowResult(MyFile f) {
        showStr = "";
        file = f;
        highLight = false;
    }

    public ShowResult(MyFile f, boolean highLight) {
        showStr = "";
        file = f;
        this.highLight = highLight;
    }

    public ShowResult(MyFile f, Hymn hymn, ArrayList<String> ls) throws Exception {
        String ss = Setting.SEARCH_RESULT_SPLIT_Arr[Setting.getValueI(Setting.SEARCH_RESULT_SPLIT)];

        lightStr = ls.toArray(new String[0]);
        String ssp = "\r\n" ;
        String sp = "\r\n" + ss + "\r\n";
        file = f;
        StringBuilder sb = new StringBuilder();
        boolean hasT = false;
        for (int i = ls.size() - 1; i >= 0; i--) {
            if (hymn.getTitle().contains(ls.get(i))) {
                if (!hasT) {
                    hasT = true;
                    sb.append("标题：").append(hymn.getTitle()).append(ssp);
                }
                ls.remove(i);
            }
        }
        HashSet<Integer> authorIds = new HashSet<>();
        for (int i = ls.size() - 1; i >= 0; i--) {
            Author h = hymn.getBySimilarName(ls.get(i), authorIds);
            if (h != null) {
                authorIds.add(h.getId());
                sb.append("作者：").append(h.getName()).append(ssp);
                ls.remove(i);
            }
        }
        ArrayList<String> cts = new ArrayList<>();
        if (ls.size() == 0)
            cts.add(hymn.getShortLyric());
        while (ls.size() > 0) {
            String pp = Service.getC().nearContent(hymn.getLyric(), ls.get(0), 14);
            ls.remove(0);
            for (int i = ls.size() - 1; i >= 0; i--)
                if (pp.contains(ls.get(i)))
                    ls.remove(i);
            cts.add(pp);
        }
        cts = connectLyric(hymn.getLyric(), cts);
        showStr = sb.append(String.join(sp, cts)).toString().trim();
        if (showStr.endsWith(ss)) {
            showStr = showStr.substring(0, showStr.length() - ss.length()).trim();
        }
    }

    private ArrayList<String> connectLyric(String lyric, ArrayList<String> cts) {
        if (cts.size() <= 1)
            return cts;
        boolean jump = false;

        ArrayList<String> res = new ArrayList<>();
        while (cts.size() > 0) {
            int min = 99999;
            int ind = -1;
            for (int i = 0; i < cts.size(); i++) {
                int io = lyric.indexOf(cts.get(i));
                if (io < min) {
                    min = io;
                    ind = i;
                }
            }
            res.add(cts.get(ind));
            cts.remove(ind);
        }

        while (!jump) {
            jump = true;
            for (int i = 0; i < res.size(); i++) {
                for (int j = i + 1; j < res.size(); j++) {
                    int ind1 = lyric.indexOf(res.get(i));
                    int len1 = res.get(i).length();
                    int ind2 = lyric.indexOf(res.get(j));
                    if (ind1 + len1 >= ind2) {
                        jump = false;
                        String sp = res.get(i).substring(0, ind2 - ind1) + res.get(j);
                        res.remove(j);
                        res.set(i, sp);
                        break;
                    }
                }
                if (!jump)
                    break;
            }
        }
        return res;
    }

    public final MyFile file;
    public String showStr;
    public String[] lightStr;
    public boolean highLight;
}
