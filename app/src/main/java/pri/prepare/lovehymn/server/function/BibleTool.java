package pri.prepare.lovehymn.server.function;

import android.app.Activity;
import android.content.Intent;
import android.text.style.ForegroundColorSpan;

import androidx.core.app.NotificationCompatSideChannelService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import pri.prepare.lovehymn.R;
import pri.prepare.lovehymn.client.tool.SpeckBibleStruct;
import pri.prepare.lovehymn.server.Service;
import pri.prepare.lovehymn.server.entity.Letter;
import pri.prepare.lovehymn.server.entity.Logger;
import pri.prepare.lovehymn.server.entity.MyFile;
import pri.prepare.lovehymn.server.entity.Section;
import pri.prepare.lovehymn.server.entity.Setting;

public class BibleTool {

    private static boolean hasInit = false;
    public static HashMap<String, String> chineseMap;
    private static HashMap<String, String> englishMap;

    public static void loadRes(Activity activity) {
        if (hasInit)
            return;
        hasInit = true;

        chineseMap = new HashMap<>();
        englishMap = new HashMap<>();

        String c = "中文：";
        String e = "英文：";
        String[] resC = MyFile.readStream(activity.getResources().openRawResource(R.raw.bible));
        String k = "key";
        for (String s : resC) {
            if (s.startsWith(c)) {
                chineseMap.put(k, s);
            } else if (s.startsWith(e)) {
                englishMap.put(k, s);
            } else
                k = s;
        }
    }

    private static ArrayList<Section[]> ctspSection(Section[] arr) {
        ArrayList<Section[]> res = new ArrayList<>();

        ArrayList<Section> tp = new ArrayList<>();

        for (Section sec : arr) {
            if (tp.size() == 0)
                tp.add(sec);
            else if (sec.SectionNum - tp.get(tp.size() - 1).SectionNum == 1)
                tp.add(sec);
            else {
                res.add(tp.toArray(new Section[0]));
                tp.clear();
                tp.add(sec);
            }
        }

        if (tp.size() > 0)
            res.add(tp.toArray(new Section[0]));

        return res;
    }

    public static String dealStr(String s, Activity activity) {
        return dealStr(s, Setting.getValueB(Setting.SECTION_CT), activity);
    }

    private static List<Section[]> getSections(String s, Activity activity) {
        loadRes(activity);

        List<Section[]> res = new ArrayList<>();
        String[] arr = s.split("[;；，]");
        for (String a : arr) {
            Section[] t = ds(a, activity);

            if (t == null)
                Logger.info("cant deal " + a);
            else
                res.add(t);
        }
        return res;
    }

    public static String dealStr(String s, boolean setction_ct, Activity activity) {
        List<Section[]> list = getSections(s, activity);
        int n = Setting.getValueI(Setting.SHOW_CHINESE_ENGLISH);
        StringBuilder sb = new StringBuilder();
        for (Section[] t : list) {
            if (setction_ct) {
                //连续经节显示
                for (Section[] secs : ctspSection(t)) {
                    sb.append("\r\n").append(secs[0].toString());
                    if (secs.length > 1) {
                        sb.append("-").append(secs[secs.length - 1].SectionNum);
                    }

                    if ((n & 1) == 1) {
                        sb.append("\r\n");
                        for (Section le : secs)
                            if (chineseMap.containsKey(le.getKeyString())) {
                                sb.append(chineseMap.get(le.getKeyString()).substring(3));
                            }
                    }
                    if ((n & 2) == 2) {
                        sb.append("\r\n");
                        for (Section le : secs)
                            if (englishMap.containsKey(le.getKeyString()) && ((n & 2) == 2)) {
                                sb.append(englishMap.get(le.getKeyString()).substring(3));
                            }
                    }
                }
            } else {
                for (Section le : t) {
                    sb.append("\r\n").append(le.toString());
                    if (chineseMap.containsKey(le.getKeyString()) && ((n & 1) == 1)) {
                        sb.append("\r\n");
                        sb.append(chineseMap.get(le.getKeyString()).substring(3));
                    }
                    if (englishMap.containsKey(le.getKeyString()) && ((n & 2) == 2)) {
                        sb.append("\r\n");
                        sb.append(englishMap.get(le.getKeyString()).substring(3));
                    }
                }
            }

            sb.append("\r\n");
        }

        return sb.toString().trim();
    }

    private static final String ALNum = "0123456789-,";
    private static final String CHNum = "一二三四五六七八九十百零";

    private static String[] spSec(String s) {
        if (s.endsWith("上") || s.endsWith("下"))
            s = s.substring(0, s.length() - 1);
        char[] cs = s.toCharArray();
        String n3 = "";
        for (int i = s.length() - 1; i >= 0; i--) {
            if ("0123456789".contains(cs[i] + "")) {
                n3 = cs[i] + n3;
            } else
                break;
        }
        String n2 = "";
        for (int i = s.length() - 1 - n3.length(); i >= 0; i--) {
            if ("一二三四五六七八九十百零〇○".contains(cs[i] + "")) {
                n2 = cs[i] + n2;
            } else
                break;
        }
        String n1 = "";
        for (int i = 0; i < s.length() - n3.length() - n2.length(); i++)
            n1 += cs[i];
        if (n1.length() == 0)
            n1 = null;
        if (n2.length() == 0)
            n2 = null;
        if (n3.length() == 0)
            n3 = null;
        return new String[]{n1, n2, n3};
    }

    private static Section[] ds(String s, Activity activity) {
        loadRes(activity);

        try {
            List<Section> res = new ArrayList<>();
            s = s.replace("—", "-").replace("--", "-")
                    .replace("，", ",").replace("～", "-");

            String[] arr = s.split(",");
            Letter lastLetter = null;
            int lastChapter = 0;
            for (String a : arr) {
                String[] arr2;
                if (a.contains("-")) {
                    arr2 = a.split("-");
                } else
                    arr2 = new String[]{a};

                String[] ar = spSec(arr2[0]);
                if (ar[0] == null) {
                    if (lastLetter == null)
                        throw new Exception("未获取书名");
                } else {
                    lastLetter = Letter.search(ar[0]);
                    if (lastLetter == null) {
                        Logger.info("书名 " + ar[0]);
                        throw new Exception("未获取书");
                    }
                }

                if (ar[1] == null) {
                    if (lastChapter == 0)
                        lastChapter = 1;
                } else {
                    if (ChineseNumToArabicNumUtil.isChineseNum(ar[1])) {
                        lastChapter = ChineseNumToArabicNumUtil.chineseNumToArabicNum(ar[1]);
                    }
                    if (lastChapter == 0) {
                        throw new Exception("TODO未处理1 " + ar[1]);
                    }
                }
                if (ar[2] == null)
                    ar[2] = "1";

                if (arr2.length == 2) {
                    int chapterTemp = lastChapter;
                    String[] ar2 = spSec(arr2[1]);
                    if (ar2[1] == null) {
                    } else {
                        if (ChineseNumToArabicNumUtil.isChineseNum(ar2[1])) {
                            lastChapter = ChineseNumToArabicNumUtil.chineseNumToArabicNum(ar2[1]);
                        }
                        if (lastChapter == 0) {
                            throw new Exception("TODO未处理2 " + ar2[1]);
                        }
                    }
                    if (ar2[2] == null) {
                        ar2[2] = Section.getNumber(lastLetter, lastChapter) + "";
                    }

                    //Logger.info("temp "+chapterTemp+" "+ar[2]);
                    //Logger.info("temp2 "+lastChapter+" "+ar2[2]);
                    if (lastChapter != chapterTemp) {
                        for (int i = chapterTemp; i < lastChapter; i++) {
                            int cNumber = Section.getNumber(lastLetter, i);
                            if (i == chapterTemp)
                                for (int j = Integer.parseInt(ar[2]); j <= cNumber; j++)
                                    res.add(new Section(lastLetter, i, j));
                            else
                                for (int j = 1; j <= cNumber; j++)
                                    res.add(new Section(lastLetter, i, j));
                        }
                        for (int j = 1; j <= Integer.parseInt(ar2[2]); j++)
                            res.add(new Section(lastLetter, lastChapter, j));
                    } else {
                        for (int i = Integer.parseInt(ar[2]); i <= Integer.parseInt(ar2[2]); i++)
                            res.add(new Section(lastLetter, lastChapter, i));
                    }
                } else {
                    res.add(new Section(lastLetter, lastChapter, Integer.parseInt(ar[2])));
                }
            }
            return res.toArray(new Section[0]);
        } catch (Exception e) {
            Logger.exception(e);
            return null;
        }
    }

    @Deprecated
    private static Section[] ds0(String s) {
        try {
            if (s.endsWith("下") || s.endsWith("上"))
                s = s.substring(0, s.length() - 1);

            s = s.replace("—", "-").replace("--", "-")
                    .replace("，", ",").replace("～", "-");

            Letter le;
            int cn = -1;
            String[] sa = new String[3];

            sa[2] = "";

            for (int i = s.length() - 1; i >= 0; i--) {
                char c = s.charAt(i);
                if (ALNum.contains(c + ""))
                    sa[2] = c + sa[2];
                else
                    break;
            }
            s = s.substring(0, s.length() - sa[2].length());

            sa[1] = "";
            for (int i = s.length() - 1; i >= 0; i--) {
                char c = s.charAt(i);
                if (CHNum.contains(c + ""))
                    sa[1] = c + sa[1];
                else
                    break;
            }

            if (sa[1].length() == 0)
                sa[1] = "一";

            sa[0] = s.substring(0, s.length() - sa[1].length());

            le = Letter.search(sa[0]);
            if (le == null) {
                Logger.info("le==null " + sa[0] + " " + sa[1] + " " + sa[2]);
                return null;
            }

            if (le.getFullName().contains("诗") && sa[1].length() >= 2) {
                String sat = sa[1].replace("一", "1").replace("二", "2").replace("三", "3").replace("四", "4")
                        .replace("五", "5").replace("六", "6").replace("七", "7")
                        .replace("八", "8").replace("九", "9").replace("零", "0").replace("〇", "0");
                if (Service.isInteger(sat)) {
                    cn = Integer.parseInt(sat);
                    Logger.info(sat + " " + cn + "  lp");
                }
            }

            if (cn == -1) {
                if (ChineseNumToArabicNumUtil.isChineseNum(sa[1])) {
                    cn = ChineseNumToArabicNumUtil.chineseNumToArabicNum(sa[1]);
                    Logger.info(sa[1] + " " + cn + "  ca");
                } else
                    return null;
            }

            if (sa[2].contains(",")) {
                String[] ar = sa[2].split(",");
                ArrayList<Section> res = new ArrayList<>();
                for (String a : ar) {
                    if (a.contains("-")) {
                        String[] asp = a.split("-");
                        if (asp.length != 2)
                            return null;
                        int n1 = Integer.parseInt(asp[0]);
                        int n2 = Integer.parseInt(asp[1]);
                        for (int i = n1; i <= n2; i++)
                            res.add(new Section(le, cn, i));
                    } else {
                        int n = Integer.parseInt(a);
                        res.add(new Section(le, cn, n));
                    }
                }
                return res.toArray(new Section[0]);
            } else if (sa[2].contains("-")) {
                String[] ar = sa[2].split("-");
                if (ar.length != 2)
                    return null;
                int n1 = Integer.parseInt(ar[0]);
                int n2 = Integer.parseInt(ar[1]);

                Section[] res = new Section[n2 - n1 + 1];
                for (int i = n1; i <= n2; i++)
                    res[i - n1] = new Section(le, cn, i);
                return res;
            } else {
                Section[] res = new Section[1];
                int n1 = Integer.parseInt(sa[2]);
                res[0] = new Section(le, cn, n1);
                return res;
            }
        } catch (Exception e) {
            Logger.exception(e);
            return null;
        }
    }

    public static List<SpeckBibleStruct> dailyShow(String str, Activity activity) {
        List<Section[]> list = getSections(str, activity);

        Letter lastL = null;
        int lastC = -1;
        List<SpeckBibleStruct> sb = new ArrayList<>();
        for (Section[] sections : list) {
            for (Section sec : sections) {
                if (lastL == null) {
                    lastL = sec.letter;
                    lastC = sec.ChapterNum;
                    if (lastL.getFullName().equals("诗篇"))
                        sb.add(new SpeckBibleStruct(lastL.getFullName() + " 第" + lastC + "篇", sec, 2));
                    else
                        sb.add(new SpeckBibleStruct(lastL.getFullName() + " 第" + lastC + "章", sec, 2));
                } else if (lastL != sec.letter) {
                    lastL = sec.letter;
                    lastC = sec.ChapterNum;
                    sb.add(new SpeckBibleStruct("", sec, 0));
                    if (lastL.getFullName().equals("诗篇"))
                        sb.add(new SpeckBibleStruct(lastL.getFullName() + " 第" + lastC + "篇", sec, 2));
                    else
                        sb.add(new SpeckBibleStruct(lastL.getFullName() + " 第" + lastC + "章", sec, 2));
                } else if (lastC != sec.ChapterNum) {
                    lastC = sec.ChapterNum;
                    if (lastL.getFullName().equals("诗篇"))
                        sb.add(new SpeckBibleStruct(lastL.getFullName() + " 第" + lastC + "篇", sec, 2));
                    else
                        sb.add(new SpeckBibleStruct("第" + lastC + "章", sec, 1));
                }
                sb.add(new SpeckBibleStruct("[" + sec.SectionNum + "]" + chineseMap.get(sec.getKeyString()).substring(3), sec, 0));
            }
        }
        return sb;
    }
}
