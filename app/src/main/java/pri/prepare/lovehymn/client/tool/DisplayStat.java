package pri.prepare.lovehymn.client.tool;

import pri.prepare.lovehymn.client.SettingDialog;
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

    /**
     * 渐隐耗时
     */
    public static final int DISPEAR_RATE_TIME = 500;

    private static int dispearTime;

    //刷新频率
    public static final int HZ = 60;

    private int ToolBarStat = 0;
    private int ToolBarPercent;

    public void addToolBarPercent() {
        ToolBarPercent++;
        if (ToolBarStat == 0)
            if (dispearTime * HZ / 1000 - ToolBarPercent <= 0) {
                ToolBarStat = 1;
                ToolBarPercent = 0;
            }
    }

    public boolean isClosingToolBar() {
        return ToolBarStat == 1;
    }

    public float getToolBarPercent() {
        if (ToolBarPercent == 0)
            return 0f;
        int n = DISPEAR_RATE_TIME * HZ / 1000;
        if (ToolBarPercent >= n)
            return 1f;
        return ToolBarPercent * 1f / n;
    }

    public boolean resetLast() {
        return ToolBarStat == 0 && ToolBarPercent <= 5;
    }

    /**
     * 重置工具栏状态
     */
    public void resetToolBar() {
        ToolBarStat = 0;
        ToolBarPercent = 0;
        dispearTime = Setting.getValueI(Setting.DISPEAR_TIME);
    }

    /**
     * 隐藏状态栏（滑动时方便查看第一段歌词）
     */
    public void hideToolBarNow() {
        ToolBarStat = 1;
        ToolBarPercent = DISPEAR_RATE_TIME * HZ / 1000;
        dispearTime = Setting.getValueI(Setting.DISPEAR_TIME);
    }

    /**
     * 开始隐藏
     */
    public void beginHide() {
        ToolBarStat = 1;
        ToolBarPercent = 0;
        Logger.info("beginHide");
    }
}
