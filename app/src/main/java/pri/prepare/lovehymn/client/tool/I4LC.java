package pri.prepare.lovehymn.client.tool;

import pri.prepare.lovehymn.server.entity.MyFile;

public interface I4LC {
    void updateTitle(MyFile file, int modType);

    int MOD_COLLECT = 1;
    int ADD_STEP = 2;
    int CLEAR_COLLECT = 3;
    int CLEAR_HISTORY=4;
}
