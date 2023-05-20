package pri.prepare.lovehymn.client.tool;

import pri.prepare.lovehymn.server.entity.Section;

public class SpeckBibleStruct {
    public SpeckBibleStruct(String ct, Section sec,int t) {
        content = ct;
        section = sec;
        type=t;
    }

    public String content;
    public Section section;
    public int type;
}
