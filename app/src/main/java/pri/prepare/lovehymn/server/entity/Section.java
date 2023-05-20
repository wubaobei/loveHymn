package pri.prepare.lovehymn.server.entity;

import androidx.annotation.NonNull;

import java.util.Map;

import pri.prepare.lovehymn.server.function.BibleTool;

public class Section {
    public final Letter letter;
    public final int ChapterNum;
    public final int SectionNum;

    public Section(Letter l, int cn, int sn) {
        letter = l;
        ChapterNum = cn;
        SectionNum = sn;
    }

    public static int getNumber(Letter lastLetter, int i) {
        String k = lastLetter.getFullName() + " " + i + ":";
        int max = 0;
        for (Map.Entry<String, String> e : BibleTool.chineseMap.entrySet()) {
            if (e.getKey().startsWith(k)) {
                int n = Integer.parseInt(e.getKey().substring(k.length()));
                if (n > max)
                    max = n;
            }
        }
        return max;
    }

    public String getKeyString() {
        return letter.getFullName() + " " + ChapterNum + ":" + SectionNum;
    }

    @NonNull
    @Override
    public String toString() {
        return letter.getSimpleName() + " " + ChapterNum + ":" + SectionNum;
    }
}
