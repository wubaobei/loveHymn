package pri.prepare.lovehymn.client.tool;

import pri.prepare.lovehymn.server.entity.Logger;
import pri.prepare.lovehymn.server.entity.Setting;

public class DisplayStat {
    private static DisplayStat c = null;

    public static DisplayStat getC() {
        if (c == null)
            c = new DisplayStat();
        return c;
    }

    public boolean isUpdateRes = false;
    /**
     * 刚更新结束的标记
     */
    public boolean updateResInFirst = false;
    public int updateProcess = 0;

    //刷新频率
    public static final int HZ = 60;
}
